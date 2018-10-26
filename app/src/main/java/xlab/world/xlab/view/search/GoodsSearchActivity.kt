package xlab.world.xlab.view.search

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.SimpleItemAnimator
import android.support.v7.widget.StaggeredGridLayoutManager
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
import android.widget.TextView
import kotlinx.android.synthetic.main.action_bar_my_shop.*
import kotlinx.android.synthetic.main.activity_goods_search.*
import org.koin.android.architecture.ext.viewModel
import org.koin.android.ext.android.inject
import xlab.world.xlab.R
import xlab.world.xlab.adapter.recyclerView.GoodsKeywordAdapter
import xlab.world.xlab.adapter.recyclerView.SearchGoodsAdapter
import xlab.world.xlab.utils.listener.DefaultListener
import xlab.world.xlab.utils.support.*
import xlab.world.xlab.utils.view.button.MatchButtonHelper
import xlab.world.xlab.utils.view.button.ScrollUpButtonHelper
import xlab.world.xlab.utils.view.dialog.DefaultProgressDialog
import xlab.world.xlab.utils.view.hashTag.EditTextTagHelper
import xlab.world.xlab.utils.view.popupWindow.GoodsSortPopupWindow
import xlab.world.xlab.utils.view.recyclerView.CustomItemDecoration
import xlab.world.xlab.utils.view.toast.DefaultToast
import xlab.world.xlab.view.cart.CartViewModel

class GoodsSearchActivity : AppCompatActivity(), View.OnClickListener {
    private val searchViewModel: SearchViewModel by viewModel()
    private val cartViewModel: CartViewModel by viewModel()
    private val spHelper: SPHelper by inject()

    private lateinit var defaultToast: DefaultToast
    private lateinit var progressDialog: DefaultProgressDialog

    private lateinit var editTextTagHelper: EditTextTagHelper
    private lateinit var matchButtonHelper: MatchButtonHelper
    private lateinit var scrollUpButtonHelper: ScrollUpButtonHelper
    private lateinit var goodsSortPopup: GoodsSortPopupWindow

    private lateinit var goodsKeywordAdapter: GoodsKeywordAdapter
    private lateinit var searchGoodsAdapter: SearchGoodsAdapter

    private lateinit var defaultListener: DefaultListener
    private val sortPopupListener = object: GoodsSortPopupWindow.Listener {
        override fun onChangeSort(sortType: Int) {
            searchViewModel.changeSearchSortType(sortType = sortType)
        }
    }

    private val tagDeleteClickListener = View.OnClickListener { view ->
        editTextTagHelper.removeTag(view.tag as View)
    }
    private val editorActionListener = TextView.OnEditorActionListener { _, actionId: Int, _ ->
        var isHandle = false
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            isHandle = editTextTagHelper.addTag()
            ViewFunction.hideKeyboard(this, searchLayout)
        }
        isHandle
    }
    private val keyListener = View.OnKeyListener { _, keyCode: Int, keyEvent: KeyEvent? ->
        var isHandle = false
        keyEvent?.let {
            // press delete key
            if (keyCode == KeyEvent.KEYCODE_DEL && keyEvent.action == KeyEvent.ACTION_DOWN)
                isHandle = editTextTagHelper.removeLastTag()
        }
        isHandle
    }
    private val tagChangeCallBack = object : EditTextTagHelper.TagChangeCallBack {
        override fun onTagChanged(status: Int) {
            if (status == editTextTagHelper.statusRemove) {
            } else if (status == editTextTagHelper.statusAdd) {
            }

            if (editTextTagHelper.getTagList().size > 0) {
                searchViewModel.searchGoods(authorization = spHelper.authorization, searchData = editTextTagHelper.getTagList(),
                        page = 1, topicColorList = resources.getStringArray(R.array.topicColorStringList))
            }
        }
    }

    private val goodsKeywordSelectListener = View.OnClickListener { view ->
        val keywordData = view.tag as EditTextTagHelper.SearchData
        editTextTagHelper.addTag(text = keywordData.text, code = keywordData.code)
    }
    private val goodsKeywordMoreListener = View.OnClickListener {
        goodsKeywordAdapter.showMoreData()
    }

    private val sortListener = View.OnClickListener {
        goodsSortPopup.showAsDropDown(guideViewOfPopup, 0, 0, Gravity.END)
        dimLayout.visibility = View.VISIBLE
    }

    private val matchButtonListener = object: MatchButtonHelper.Listener {
        override fun matchVisibility(visibility: Int) {
            searchGoodsAdapter.changeMatchVisible(visibility)
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_goods_search)

        onSetup()

        onBindEvent()

        observeViewModel()
    }

    override fun onBackPressed() {
        actionBackBtn.performClick()
    }

    override fun onPause() {
        super.onPause()
        ViewFunction.hideKeyboard(this, searchLayout)
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
                    RequestCodeData.MY_CART, // 장바구니
                    RequestCodeData.TOPIC_SETTING, // 토픽 설정
                    RequestCodeData.GOODS_DETAIL -> { // 상품 상세
                        cartViewModel.loadCartCnt(authorization = spHelper.authorization)
                        searchViewModel.searchGoods(authorization = spHelper.authorization, searchData = editTextTagHelper.getTagList(),
                                page = 1, topicColorList = resources.getStringArray(R.array.topicColorStringList))
                    }
                }
            }
            ResultCodeData.LOGIN_SUCCESS -> { // login -> reload all data
                searchViewModel.searchGoods(authorization = spHelper.authorization, searchData = editTextTagHelper.getTagList(),
                        page = 1, topicColorList = resources.getStringArray(R.array.topicColorStringList))
            }
            ResultCodeData.LOGOUT_SUCCESS -> { // logout -> finish activity
                actionBackBtn.performClick()
            }
        }
    }

    private fun onSetup() {
        // 타이틀 설정, 공유 버튼 비활성화
        actionBarTitle.setText(getText(R.string.goods), TextView.BufferType.SPANNABLE)
        actionShareBtn.visibility = View.GONE

        // appBarLayout 애니메이션 없애기
        appBarLayout.stateListAnimator = null

        // Toast, Dialog 초기화
        defaultToast = DefaultToast(context = this)
        progressDialog = DefaultProgressDialog(context = this)

        // listener 초기화
        defaultListener = DefaultListener(context = this)

        // sort popup 초기화
        val popupView = layoutInflater.inflate(R.layout.popup_goods_sort, null)
        goodsSortPopup = GoodsSortPopupWindow(popupView, sortPopupListener)

        // edit text tag 초기화
        editTextTagHelper = EditTextTagHelper(
                context = this,
                orientation = LinearLayout.VERTICAL,
                tagDeleteClickListener = tagDeleteClickListener,
                editorActionListener = editorActionListener,
                keyListener = keyListener,
                tagChangeCallBack = tagChangeCallBack)
        editTextTagHelper.handle(editTagSearchView)

        // match button 초기화
        matchButtonHelper = MatchButtonHelper(
                context = this,
                rootView = matchBtnLayout,
                isMatchShow = true,
                listener = matchButtonListener)

        // scroll up button 초기화
        scrollUpButtonHelper = ScrollUpButtonHelper(
                smoothScroll = true,
                scrollUpBtn = scrollUpBtn)
        scrollUpButtonHelper.handle(goodsRecyclerView)

        // keyword adapter & recycler view 초기화
        goodsKeywordAdapter = GoodsKeywordAdapter(context = this,
                selectListener = goodsKeywordSelectListener,
                moreListener = goodsKeywordMoreListener)
        keywordRecyclerView.adapter = goodsKeywordAdapter
        keywordRecyclerView.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.HORIZONTAL)
        keywordRecyclerView.addItemDecoration(CustomItemDecoration(context = this, left = 6f, right = 6f, top = 4f, bottom = 4f))

        // goods adapter & recycler view 초기화
        searchGoodsAdapter = SearchGoodsAdapter(context = this,
                sortListener = sortListener,
                goodsListener = defaultListener.goodsListener,
                questionListener = defaultListener.questionMatchListener,
                contentBottomMargin = 50f,
                matchVisible = View.VISIBLE
        )
        goodsRecyclerView.adapter = searchGoodsAdapter
        val gridLayoutManager = GridLayoutManager(this, 3, GridLayoutManager.VERTICAL, false)
        gridLayoutManager.spanSizeLookup = object: GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return when(searchGoodsAdapter.getItemViewType(position = position)) {
                    AppConstants.ADAPTER_HEADER -> 3
                    AppConstants.ADAPTER_CONTENT -> 1
                    else -> 1
                }
            }
        }
        goodsRecyclerView.layoutManager = gridLayoutManager
        goodsRecyclerView.addItemDecoration(CustomItemDecoration(context = this, left = 0.5f, right = 0.5f))
        (goodsRecyclerView.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false

        cartViewModel.loadCartCnt(authorization = spHelper.authorization)
        editTextTagHelper.addTag(text = intent.getStringExtra(IntentPassName.SEARCH_TEXT),
                code = intent.getStringExtra(IntentPassName.SEARCH_CODE))
    }

    private fun onBindEvent() {
        actionBackBtn.setOnClickListener(this) // 뒤로가기
        cartLayout.setOnClickListener(this) // 장바구니
        keywordBtn.setOnClickListener(this) // 추천 키워드 보기&숨기기
        editTagSearchView.setOnClickListener(this) // 검색 레이아웃

        // 키보드 보일때 매칭 버튼 안보이게
        ViewFunction.showUpKeyboardLayout(view = mainLayout) { visibility ->
            matchBtnLayout.visibility =
                    if (visibility == View.VISIBLE) View.GONE
                    else View.VISIBLE
        }

        goodsSwipeRefreshLayout.setOnRefreshListener {
            searchViewModel.searchGoods(authorization = spHelper.authorization, searchData = editTextTagHelper.getTagList(),
                    page = 1, topicColorList = resources.getStringArray(R.array.topicColorStringList), loadingBar = false)
        }

        ViewFunction.onRecyclerViewScrolledDown(recyclerView = goodsRecyclerView) {
            ViewFunction.isScrolledRecyclerView(layoutManager = it as GridLayoutManager, total = goodsKeywordAdapter.dataTotal, isLoading = goodsKeywordAdapter.dataLoading) { isScrolled ->
                if (isScrolled)
                    searchViewModel.searchGoods(authorization = spHelper.authorization, searchData = editTextTagHelper.getTagList(),
                            page = goodsKeywordAdapter.dataNextPage, topicColorList = resources.getStringArray(R.array.topicColorStringList))
            }
        }

        // sort popup 사라자면 dim 처리 지우기
        goodsSortPopup.setOnDismissListener {
            dimLayout.visibility = View.GONE
        }
    }

    private fun observeViewModel() {
        // TODO: Search View Model
        // UI 이벤트 observe
        searchViewModel.uiData.observe(this, android.arch.lifecycle.Observer { uiData ->
            uiData?.let { _ ->
                uiData.isLoading?.let {
                    if (it && !progressDialog.isShowing)
                        progressDialog.show()
                    else if (!it && progressDialog.isShowing)
                        progressDialog.dismiss()
                }
                uiData.toastMessage?.let {
                    defaultToast.showToast(message = it)
                }
                uiData.resultCode?.let {
                    setResult(it)
                    finish()
                }
                uiData.searchGoodsData?.let {
                    searchGoodsAdapter.linkData(searchGoodsData = it)
                    goodsSwipeRefreshLayout.isRefreshing = false
                }
                uiData.searchGoodsDataUpdate?.let {
                    searchGoodsAdapter.notifyDataSetChanged()
                }
                uiData.searchGoodsUpdateIndex?.let {
                    searchGoodsAdapter.notifyItemChanged(it)
                }
                uiData.keywordData?.let {
                    goodsKeywordAdapter.linkData(goodsKeywordData = it)
                }
                uiData.keywordVisibility?.let {
                    keywordBtn.visibility = it
                    keywordListLayout.visibility = it
                }
                uiData.keywordArrowRotation?.let {
                    imageViewKeywordArrow.rotation = it
                }
            }
        })

        // search goods 이벤트 observe
        searchViewModel.searchGoodsEventData.observe(owner = this, observer = android.arch.lifecycle.Observer { eventData ->
            eventData?.let { isLoading ->
                searchGoodsAdapter.dataLoading = isLoading
            }
        })

        // change search sort type 이벤트 observe
        searchViewModel.changeSearchSortTypeData.observe(owner = this, observer = android.arch.lifecycle.Observer { eventData ->
            eventData?.let { isSuccess ->
                if (isSuccess)
                    searchViewModel.searchGoods(authorization = spHelper.authorization, searchData = editTextTagHelper.getTagList(),
                            page = 1, topicColorList = resources.getStringArray(R.array.topicColorStringList))
            }
        })
        // TODO: Cart View Model
        // UI 이벤트 observe
        cartViewModel.uiData.observe(this, android.arch.lifecycle.Observer { uiData ->
            uiData?.let { _ ->
                uiData.isLoading?.let {
                    if (it && !progressDialog.isShowing)
                        progressDialog.show()
                    else if (!it && progressDialog.isShowing)
                        progressDialog.dismiss()
                }
                uiData.cartCnt?.let {
                    textViewCartCnt.setText(it, TextView.BufferType.SPANNABLE)
                }
                uiData.cartCntVisibility?.let {
                    textViewCartCnt.visibility = it
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
                R.id.cartLayout -> { // 장바구니
                    RunActivity.cartActivity(context = this)
                }
                R.id.keywordBtn -> { // 추천 키워드 보기&숨기기
                    searchViewModel.keywordVisibilityChnage()
                }
                R.id.editTagSearchView -> {
                    editTextTagHelper.requestInputFieldFocus()
                    ViewFunction.showKeyboard(context = this)
                }
            }
        }
    }

    companion object {
        fun newIntent(context: Context, searchText: String, searchCode: String): Intent {
            val intent = Intent(context, GoodsSearchActivity::class.java)
            intent.putExtra(IntentPassName.SEARCH_TEXT, searchText)
            intent.putExtra(IntentPassName.SEARCH_CODE, searchCode)

            return intent
        }
    }
}
