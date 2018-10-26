package xlab.world.xlab.view.search

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.view.inputmethod.EditorInfo
import kotlinx.android.synthetic.main.action_bar_search.*
import kotlinx.android.synthetic.main.activity_combined_search.*
import org.koin.android.architecture.ext.viewModel
import org.koin.android.ext.android.inject
import xlab.world.xlab.R
import xlab.world.xlab.adapter.recyclerView.RecentCombinedSearchAdapter
import xlab.world.xlab.adapter.viewPager.ViewStatePagerAdapter
import xlab.world.xlab.utils.font.FontColorSpan
import xlab.world.xlab.utils.support.*
import xlab.world.xlab.utils.view.tabLayout.TabLayoutHelper
import xlab.world.xlab.view.search.fragment.CombinedSearchGoodsFragment
import xlab.world.xlab.view.search.fragment.CombinedSearchPostFragment
import xlab.world.xlab.view.search.fragment.CombinedSearchUserFragment

class CombinedSearchActivity : AppCompatActivity(), View.OnClickListener {
    private val searchViewModel: SearchViewModel by viewModel()
    private val spHelper: SPHelper by inject()
    private val fontColorSpan: FontColorSpan by inject()

    private lateinit var tabLayoutHelper: TabLayoutHelper

    private lateinit var viewPagerAdapter: ViewStatePagerAdapter
    private lateinit var searchPostsFragment: CombinedSearchPostFragment
    private lateinit var searchUserFragment: CombinedSearchUserFragment
    private lateinit var searchGoodsFragment: CombinedSearchGoodsFragment

    private lateinit var recentCombinedSearchAdapter: RecentCombinedSearchAdapter

    private val recentSearchListener = View.OnClickListener { view ->
        editTextSearch.setText(view.tag as String)
        requestSearch(searchText = view.tag as String)
    }
    private val recentDeleteListener = View.OnClickListener { view ->
        val item = recentCombinedSearchAdapter.getItem(position = (view.tag as Int))

        val searchList = spHelper.recentSearch
        searchList.remove(item.searchText)
        spHelper.recentSearch = searchList

        recentCombinedSearchAdapter.removeData(position = (view.tag as Int))
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_combined_search)

        onSetup()

        onBindEvent()

        observeViewModel()
    }

    override fun onBackPressed() {
        actionBackBtn.performClick()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        PrintLog.d("resultCode", resultCode.toString(), this::class.java.name)
        PrintLog.d("requestCode", requestCode.toString(), this::class.java.name)

        searchViewModel.setResultCode(resultCode = resultCode)
        when (resultCode) {
            Activity.RESULT_OK -> {
                when (requestCode) {
                    RequestCodeData.TOPIC_ADD, // 토픽 추가
                    RequestCodeData.TOPIC_SETTING, // 토픽 설정
                    RequestCodeData.PROFILE, // 프로필
                    RequestCodeData.POST_DETAIL, // 포스트 상세
                    RequestCodeData.GOODS_DETAIL-> { // 상품 상세
                        requestSearch(searchText = getSearchText())
                    }
                }
            }
            ResultCodeData.LOGIN_SUCCESS -> { // login -> reload all data
                requestSearch(searchText = getSearchText())
            }
            ResultCodeData.LOGOUT_SUCCESS -> { // logout -> finish activity
                actionBackBtn.performClick()
            }
        }
    }

    private fun onSetup() {
        // 프래그먼트 초기화
        searchPostsFragment = CombinedSearchPostFragment.newFragment()
        searchUserFragment = CombinedSearchUserFragment.newFragment()
        searchGoodsFragment = CombinedSearchGoodsFragment.newFragment()

        viewPagerAdapter = ViewStatePagerAdapter(manager = supportFragmentManager)
        viewPagerAdapter.addFragment(fragment = searchPostsFragment, title = getString(R.string.post))
        viewPagerAdapter.addFragment(fragment = searchUserFragment, title = getString(R.string.user))
        viewPagerAdapter.addFragment(fragment = searchGoodsFragment, title = getString(R.string.goods))
        searchViewPager.adapter = viewPagerAdapter

        // tab layout 초기화
        tabLayoutHelper = TabLayoutHelper(context = this,
                defaultSelectFont = fontColorSpan.notoBold303030,
                defaultUnSelectFont = fontColorSpan.notoMediumA4A4A4,
                useDefaultEvent = true,
                listener = null)
        tabLayoutHelper.handle(layout = tabLayout, viewPager = searchViewPager)
        tabLayoutHelper.addTab(tabName = getString(R.string.post), tabLayout = R.layout.tab_layout_number, fontSize = null, selectFont = null, unSelectFont = null, extraData = 0)
        tabLayoutHelper.addTab(tabName = getString(R.string.user), tabLayout = R.layout.tab_layout_number, fontSize = null, selectFont = null, unSelectFont = null, extraData = 0)
        tabLayoutHelper.addTab(tabName = getString(R.string.goods), tabLayout = R.layout.tab_layout_number, fontSize = null, selectFont = null, unSelectFont = null, extraData = 0)
        tabLayoutHelper.changeSelectedTab(0)

        // 최근 검색 목록 adapter & recycler 초기화
        recentCombinedSearchAdapter = RecentCombinedSearchAdapter(context = this,
                selectListener = recentSearchListener,
                deleteListener = recentDeleteListener)
        recentSearchRecyclerView.adapter = recentCombinedSearchAdapter
        recentSearchRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
    }

    private fun onBindEvent() {
        actionBackBtn.setOnClickListener(this) // 뒤로가기
        searchDeleteBtn.setOnClickListener(this) // 검색창 지우기

        ViewFunction.onKeyboardActionTouch(editText = editTextSearch, putActionID = EditorInfo.IME_ACTION_DONE) { isTouch: Boolean ->
            if (isTouch && getSearchText().isNotEmpty()){
                requestSearch(searchText = getSearchText())
            }
        }

        ViewFunction.onTextChange(editText = editTextSearch) { searchText ->
            searchDeleteBtn.visibility =
                    if (searchText.isEmpty()) View.INVISIBLE
                    else View.VISIBLE
        }
    }

    private fun observeViewModel() {
        // UI 이벤트 observe
        searchViewModel.uiData.observe(this, android.arch.lifecycle.Observer { uiData ->
            uiData?.let { _ ->
                uiData.resultCode?.let {
                    setResult(it)
                    finish()
                }
                uiData.searchPostsTotal?.let {
                    tabLayoutHelper.getTabData(0).extraData = it
                    tabLayoutHelper.updateTabView(0)
                }
                uiData.searchUserTotal?.let {
                    tabLayoutHelper.getTabData(1).extraData = it
                    tabLayoutHelper.updateTabView(1)
                }
                uiData.searchGoodsTotal?.let {
                    tabLayoutHelper.getTabData(2).extraData = it
                    tabLayoutHelper.updateTabView(2)
                }
            }
        })
    }

    override fun onClick(v: View?) {
        v?.let {
            when (v.id) {
                R.id.actionBackBtn -> { // 뒤로가기
                    searchViewModel.backBtnAction()
                }
                R.id.searchDeleteBtn -> { // 검색창 지우기
                    editTextSearch.setText("")
                }
            }
        }
    }

    private fun requestSearch(searchText: String) {
        val searchList = spHelper.recentSearch
        searchList.remove(searchText)
        searchList.add(searchText)
        spHelper.recentSearch = searchList

        if (recentSearchLayout.visibility != View.GONE)
            recentSearchLayout.visibility = View.GONE

        searchViewModel.requestCombinedSearchTotal(authorization = spHelper.authorization, searchText = searchText)

        // need request data on fragment
        searchPostsFragment.searchPostsData(searchText = searchText, loadingBar = true)
        searchUserFragment.searchUserData(searchText = searchText, loadingBar = true)
        searchGoodsFragment.searchGoodsData(searchText = searchText, loadingBar = true)
    }

    private fun getSearchText(): String = editTextSearch.text.toString()

    fun setResultCodeFromFragment(resultCode: Int) {
        searchViewModel.setResultCode(resultCode = resultCode)
    }

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, CombinedSearchActivity::class.java)
        }
    }
}
