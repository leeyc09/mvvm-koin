package xlab.world.xlab.view.search.fragment

import android.app.Activity
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_combined_search.*
import org.koin.android.architecture.ext.viewModel
import org.koin.android.ext.android.inject
import xlab.world.xlab.R
import xlab.world.xlab.adapter.recyclerView.SearchGoodsAdapter
import xlab.world.xlab.utils.listener.DefaultListener
import xlab.world.xlab.utils.support.SPHelper
import xlab.world.xlab.utils.support.ViewFunction
import xlab.world.xlab.utils.view.button.MatchButtonHelper
import xlab.world.xlab.utils.view.button.ScrollUpButtonHelper
import xlab.world.xlab.utils.view.dialog.DefaultProgressDialog
import xlab.world.xlab.utils.view.hashTag.EditTextTagHelper
import xlab.world.xlab.utils.view.recyclerView.CustomItemDecoration
import xlab.world.xlab.utils.view.toast.DefaultToast
import xlab.world.xlab.view.search.SearchViewModel

class CombinedSearchGoodsFragment: Fragment() {
    private val searchViewModel: SearchViewModel by viewModel()
    private val spHelper: SPHelper by inject()

    private var needInitData
        get() = arguments?.getBoolean("needInitData") ?: false
        set(value) {
            arguments?.putBoolean("needInitData", value)
        }
    private var searchText
        get() = arguments?.getString("searchText") ?: ""
        set(value) {
            arguments?.putString("searchText", value)
        }
    private var matchVisibility
        get() = arguments?.getInt("matchVisibility") ?: View.VISIBLE
        set(value) {
            arguments?.putInt("matchVisibility", value)
        }

    private var defaultToast: DefaultToast? = null
    private var progressDialog: DefaultProgressDialog? = null

    private var searchGoodsAdapter: SearchGoodsAdapter? = null

    private lateinit var matchButtonHelper: MatchButtonHelper
    private lateinit var scrollUpButtonHelper: ScrollUpButtonHelper

    private var defaultListener: DefaultListener? = null
    private val matchButtonListener = object: MatchButtonHelper.Listener {
        override fun matchVisibility(visibility: Int) {
            matchVisibility = visibility
            searchGoodsAdapter?.changeMatchVisible(visibility)
        }
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_combined_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        onSetup()

        onBindEvent()

        observeViewModel()
    }

    private fun onSetup() {
        // Toast, Dialog 초기화
        defaultToast = defaultToast ?: DefaultToast(context = context!!)
        progressDialog = progressDialog ?: DefaultProgressDialog(context = context!!)

        defaultListener = defaultListener ?: DefaultListener(context = context as Activity)

        // match button 초기화
        matchButtonHelper = MatchButtonHelper(
                context = context as Activity,
                rootView = matchBtnLayout,
                isMatchShow = matchVisibility == View.VISIBLE,
                listener = matchButtonListener)

        // scroll up button 초기화
        scrollUpButtonHelper = ScrollUpButtonHelper(
                smoothScroll = true,
                scrollUpBtn = scrollUpBtn)
        scrollUpButtonHelper.handle(recyclerView)

        // search goods adapter & recycler view 초기화
        searchGoodsAdapter = searchGoodsAdapter ?: SearchGoodsAdapter(
                context = context!!,
                sortListener = null,
                goodsListener = defaultListener!!.goodsListener,
                questionListener = defaultListener!!.questionMatchListener,
                contentBottomMargin = 50f,
                matchVisible = View.VISIBLE)
        recyclerView.adapter = searchGoodsAdapter
        recyclerView.layoutManager = GridLayoutManager(context, 3, GridLayoutManager.VERTICAL, false)
        if (recyclerView.itemDecorationCount < 1)
            recyclerView.addItemDecoration(CustomItemDecoration(context = context!!, left = 0.5f, right = 0.5f))

        if (needInitData)
            searchGoodsData(searchText = this.searchText, loadingBar = true)
    }

    private fun onBindEvent() {
        ViewFunction.onRecyclerViewScrolledDown(recyclerView = recyclerView) {
            ViewFunction.isScrolledRecyclerView(layoutManager = it as GridLayoutManager, isLoading = searchGoodsAdapter!!.dataLoading, total = searchGoodsAdapter!!.dataTotal) { _ ->
                searchViewModel.searchGoods(authorization = spHelper.authorization,
                        searchData = arrayListOf(EditTextTagHelper.SearchData(text = searchText, code = "")),
                        page = searchGoodsAdapter!!.dataNextPage, topicColorList = resources.getStringArray(R.array.topicColorStringList),
                        withHeader = false)
            }
        }
    }

    private fun observeViewModel() {
        // UI 이벤트 observe
        searchViewModel.uiData.observe(this, android.arch.lifecycle.Observer { uiData ->
            uiData?.let { _ ->
                uiData.isLoading?.let {
                    if (it && !progressDialog!!.isShowing)
                        progressDialog!!.show()
                    else if (!it && progressDialog!!.isShowing)
                        progressDialog!!.dismiss()
                }
                uiData.toastMessage?.let {
                    defaultToast?.showToast(message = it)
                }
                uiData.searchGoodsData?.let {
                    if (it.nextPage <= 2 ) { // 요청한 page => 첫페이지
                        setBundleVisibilityData(noSearchDataVisibility =
                        if (it.items.isEmpty()) View.VISIBLE
                        else View.GONE)
                        searchGoodsAdapter?.updateData(searchGoodsData = it)
                    }
                    else
                        searchGoodsAdapter?.addData(searchGoodsData = it)
                }
            }
        })

        // search user 이벤트 observe
        searchViewModel.searchGoodsEventData.observe(owner = this, observer = android.arch.lifecycle.Observer { eventData ->
            eventData?.let { _ ->
                eventData.status?.let { isLoading ->
                    searchGoodsAdapter?.dataLoading = isLoading
                    needInitData = false
                }
            }
        })
    }

    fun searchGoodsData(searchText: String, loadingBar: Boolean?) {
        this.searchText = searchText
        context?.let {
            searchViewModel.searchGoods(authorization = spHelper.authorization,
                    searchData = arrayListOf(EditTextTagHelper.SearchData(text = searchText, code = "")),
                    page = 1, topicColorList = resources.getStringArray(R.array.topicColorStringList),
                    withHeader = false, loadingBar = loadingBar)
        } ?:let { needInitData = true }
    }

    private fun setLayoutVisibility() {
        textViewNoSearchData?.visibility = getBundleNoSearchDataVisibility()
    }

    private fun getBundleNoSearchDataVisibility(): Int = arguments?.getInt("noSearchDataVisibility") ?: View.GONE

    private fun setBundleVisibilityData(noSearchDataVisibility: Int) {
        arguments?.putInt("noSearchDataVisibility", noSearchDataVisibility)

        setLayoutVisibility()
    }

    companion object {
        fun newFragment(): CombinedSearchGoodsFragment {
            val fragment = CombinedSearchGoodsFragment()

            val args = Bundle()
            args.putBoolean("needInitData", false)
            args.putInt("noSearchDataVisibility", View.GONE)
            args.putInt("matchVisibility", View.VISIBLE)
            fragment.arguments = args

            return fragment
        }
    }
}