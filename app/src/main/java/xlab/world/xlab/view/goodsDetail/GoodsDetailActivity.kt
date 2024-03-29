package xlab.world.xlab.view.goodsDetail

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SimpleItemAnimator
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotlinx.android.synthetic.main.action_bar_my_shop.*
import kotlinx.android.synthetic.main.activity_goods_detail.*
import org.koin.android.architecture.ext.viewModel
import org.koin.android.ext.android.inject
import xlab.world.xlab.R
import xlab.world.xlab.adapter.recyclerView.GoodsDetailRatingAdapter
import xlab.world.xlab.adapter.recyclerView.GoodsDetailTopicMatchAdapter
import xlab.world.xlab.adapter.viewPager.ViewImagePagerAdapter
import xlab.world.xlab.adapter.viewPager.ViewStatePagerAdapter
import xlab.world.xlab.utils.font.FontColorSpan
import xlab.world.xlab.utils.listener.DefaultListener
import xlab.world.xlab.utils.support.*
import xlab.world.xlab.utils.toast.AddCartToast
import xlab.world.xlab.utils.view.dialog.*
import xlab.world.xlab.utils.view.recyclerView.CustomItemDecoration
import xlab.world.xlab.utils.view.tabLayout.TabLayoutHelper
import xlab.world.xlab.utils.view.toast.DefaultToast
import xlab.world.xlab.viewModel.ShareViewModel
import xlab.world.xlab.view.cart.CartViewModel
import xlab.world.xlab.view.goodsDetail.fragment.GoodsDetailInfoFragment
import xlab.world.xlab.view.goodsDetail.fragment.GoodsDetailPostFragment
import xlab.world.xlab.view.goodsDetail.fragment.GoodsDetailStatsFragment

class GoodsDetailActivity : AppCompatActivity(), View.OnClickListener {
    private val goodsDetailViewModel: GoodsDetailViewModel by viewModel()
    private val cartViewModel: CartViewModel by viewModel()
    private val shareViewModel: ShareViewModel by viewModel()
    private val spHelper: SPHelper by inject()
    private val fontColorSpan: FontColorSpan by inject()

    private lateinit var defaultToast: DefaultToast
    private lateinit var addCartToast: AddCartToast

    private lateinit var progressDialog: DefaultProgressDialog
    private lateinit var loginDialog: DefaultDialog
    private lateinit var suggestAddTopicDialog: DefaultDialog
    private lateinit var shareDialog: ShareBottomDialog
    private lateinit var ratingCancelDialog: DefaultDialog
    private lateinit var buyGoodsOptionDialog: BuyGoodsOptionDialog

    private lateinit var tabLayoutHelper: TabLayoutHelper

    private lateinit var goodsDetailTopicMatchAdapter: GoodsDetailTopicMatchAdapter
    private lateinit var goodsDetailRatingAdapter: GoodsDetailRatingAdapter

    private lateinit var viewPagerAdapter: ViewStatePagerAdapter
    private lateinit var goodsDetailInfoFragment: GoodsDetailInfoFragment
    private lateinit var goodsDetailStatsFragment: GoodsDetailStatsFragment
    private lateinit var goodsDetailPostFragment: GoodsDetailPostFragment

    private lateinit var defaultListener: DefaultListener
    private val shareDialogListener = object: ShareBottomDialog.Listener {
        override fun onCopyLink(tag: Any?) {
            defaultToast.showToast(resources.getString(R.string.toast_copy_link_success))
        }

        override fun onShareKakao(tag: Any?) {
            goodsDetailViewModel.shareKakao()
        }
    }
    private val ratingListener = View.OnClickListener { view ->
        val ratingTagData = view.tag as GoodsDetailRatingAdapter.RatingTag
        goodsDetailViewModel.goodsRating(selectRatingData = ratingTagData,
                authorization = spHelper.authorization)
    }
    private val ratingCancelListener = object: DefaultDialog.Listener {
        override fun onOkayTouch(tag: Any?) {
            goodsDetailViewModel.ratingCancel(selectIndex = tag as Int, authorization = spHelper.authorization)
        }
    }
    private val buyGoodsListener = object: BuyGoodsOptionDialog.Listener {
        override fun addCart(count: Int) {
            goodsDetailViewModel.addCart(authorization = spHelper.authorization, count = count)
        }

        override fun buyNow(count: Int) {
            goodsDetailViewModel.buyNow(authorization = spHelper.authorization, count = count)
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_goods_detail)

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

        goodsDetailViewModel.setResultCode(resultCode = resultCode)
        when (resultCode) {
            Activity.RESULT_OK -> {
                when (requestCode) {
                    RequestCodeData.TOPIC_ADD -> { // 토픽 추가
                        // 펫 정보
                        goodsDetailViewModel.loadGoodsPetData(authorization = spHelper.authorization)
                        // 통계 정보
                        goodsDetailStatsFragment.loadGoodsStatsData()
                    }
                    RequestCodeData.PROFILE, // 프로필
                    RequestCodeData.POST_DETAIL, // 포스트 상세
                    RequestCodeData.GOODS_BRAND_SEARCH, // 브랜드 검색
                    RequestCodeData.MY_CART -> { // 장바구니
                        // 카트 숫자
                        cartViewModel.loadCartCnt(authorization = spHelper.authorization)
                        // 펫 정보
                        goodsDetailViewModel.loadGoodsPetData(authorization = spHelper.authorization)
                        // 통계 정보
                        goodsDetailStatsFragment.loadGoodsStatsData()
                        // 포스트 정보
                        goodsDetailPostFragment.loadUsedUser(page = 1)
                        goodsDetailPostFragment.loadTaggedPosts(page = 1)
                    }
                    RequestCodeData.GOODS_BUYING -> { // 구매하기
                        // 카트 숫자
                        cartViewModel.loadCartCnt(authorization = spHelper.authorization)
                        goodsDetailViewModel.loadGoodsDetailData(context = this, goodsCode = intent.getStringExtra(IntentPassName.GOODS_CODE), needDescription = false)
                        goodsDetailViewModel.loadGoodsPetData(authorization = spHelper.authorization)
                        goodsDetailInfoFragment.loadGoodsDescription()
                    }
                }
            }
            ResultCodeData.LOGIN_SUCCESS -> { // login -> reload all data
                goodsDetailViewModel.loadGoodsDetailData(context = this, goodsCode = intent.getStringExtra(IntentPassName.GOODS_CODE), needDescription = false)
                goodsDetailViewModel.loadGoodsPetData(authorization = spHelper.authorization)
                goodsDetailInfoFragment.loadGoodsDescription()
                goodsDetailStatsFragment.loadGoodsStatsData()
                goodsDetailPostFragment.loadUsedUser(page = 1)
                goodsDetailPostFragment.loadTaggedPosts(page = 1)
            }
            ResultCodeData.LOGOUT_SUCCESS -> { // logout -> finish activity
                actionBackBtn.performClick()
            }
        }
    }

    private fun onSetup() {
        // appBarLayout 애니메이션 없애기
        appBarLayout.stateListAnimator = null

        // Toast, Dialog 초기화
        defaultToast = DefaultToast(context = this)
        addCartToast = AddCartToast(context = this)
        progressDialog = DefaultProgressDialog(context = this)
        loginDialog = DialogCreator.loginDialog(context = this)
        suggestAddTopicDialog = DialogCreator.suggestAddTopicDialog(context = this)
        shareDialog = DialogCreator.shareDialog(context = this, listener = shareDialogListener)
        ratingCancelDialog = DialogCreator.ratingCancelDialog(context = this, listener = ratingCancelListener)
        buyGoodsOptionDialog = DialogCreator.buyGoodsOptionDialog(listener = buyGoodsListener)

        // listener 초기화
        defaultListener = DefaultListener(context = this)

        // 프래그먼트 초기화
        goodsDetailInfoFragment = GoodsDetailInfoFragment.newFragment()
        goodsDetailStatsFragment = GoodsDetailStatsFragment.newFragment()
        goodsDetailPostFragment = GoodsDetailPostFragment.newFragment()

        viewPagerAdapter = ViewStatePagerAdapter(manager = supportFragmentManager)
        viewPagerAdapter.addFragment(fragment = goodsDetailInfoFragment, title = getString((R.string.item_info)))
        viewPagerAdapter.addFragment(fragment = goodsDetailStatsFragment, title = getString((R.string.stats)))
        viewPagerAdapter.addFragment(fragment = goodsDetailPostFragment, title = getString((R.string.post)))
        detailViewPager.adapter = viewPagerAdapter

        // tab layout 초기화
        tabLayoutHelper = TabLayoutHelper(context = this,
                defaultSelectFont = fontColorSpan.notoBold000000,
                defaultUnSelectFont = fontColorSpan.notoMediumBFBFBF,
                useDefaultEvent = true,
                listener = null)
        tabLayoutHelper.handle(layout = tabLayout, viewPager = detailViewPager)
        tabLayoutHelper.addTab(getString(R.string.item_info), tabLayout = null, fontSize = null, selectFont = null, unSelectFont = null, extraData = null)
        tabLayoutHelper.addTab(getString(R.string.stats), tabLayout = null, fontSize = null, selectFont = null, unSelectFont = null, extraData = null)
        tabLayoutHelper.addTab(getString(R.string.post), tabLayout = null, fontSize = null, selectFont = null, unSelectFont = null, extraData = null)
        tabLayoutHelper.changeSelectedTab(selectIndex = 0)

        // goodsDetailTopicMatchAdapter & recycler 초기화
        goodsDetailTopicMatchAdapter = GoodsDetailTopicMatchAdapter(context = this)
        matchingRecyclerView.adapter = goodsDetailTopicMatchAdapter
        matchingRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        matchingRecyclerView.addItemDecoration(CustomItemDecoration(context = this, right = 16f))

        // goodsDetailRatingAdapter & recycler 초기화
        goodsDetailRatingAdapter = GoodsDetailRatingAdapter(context = this, ratingListener = ratingListener)
        ratingRecyclerView.adapter = goodsDetailRatingAdapter
        ratingRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        ratingRecyclerView.addItemDecoration(CustomItemDecoration(context = this, bottom = 30f))
        (ratingRecyclerView.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        ratingRecyclerView.isNestedScrollingEnabled = false

        cartViewModel.loadCartCnt(authorization = spHelper.authorization)
        goodsDetailViewModel.loadGoodsDetailData(context = this, goodsCode = intent.getStringExtra(IntentPassName.GOODS_CODE), needDescription = false)
        goodsDetailViewModel.loadGoodsPetData(authorization = spHelper.authorization)
    }

    private fun onBindEvent() {
        actionBackBtn.setOnClickListener(this) // 뒤로가기
        actionShareBtn.setOnClickListener(this) // 공유하기
        actionCartBtn.setOnClickListener(this) // 장바구니 확인
        brandBtn.setOnClickListener(this) // 브랜드 상품 보기
        ratingOpenCloseBtn.setOnClickListener(this) // 제품 평가하기 펼치기&접기
        buyBtn.setOnClickListener(this) // 구매하기 버튼
    }

    private fun observeViewModel() {
        // TODO: Goods Detail View Model
        // UI 이벤트 observe
        goodsDetailViewModel.uiData.observe(this, android.arch.lifecycle.Observer { uiData ->
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
                uiData.cartToastShow?.let {
                    cartViewModel.loadCartCnt(authorization = spHelper.authorization)
                    addCartToast.showToast()
                }
                uiData.isGuest?.let {
                    if (it) loginDialog.showDialog(tag = null)
                }
                uiData.buyOptionDialogShow?.let {
                    buyGoodsOptionDialog.showDialog(manager = supportFragmentManager, dialogTag = "buyGoodsOptionDialog",
                            price = it)
                }
                uiData.topicMatchData?.let {
                    goodsDetailTopicMatchAdapter.linkData(goodsDetailTopicMatchData = it)
                }
                uiData.topicMatchVisibility?.let {
                    matchingRecyclerView.visibility = it
                }
                uiData.goodsRatingData?.let {
                    goodsDetailRatingAdapter.linkData(goodsDetailRatingData = it)
                }
                uiData.goodsRatingUpdateIndex?.let {
                    goodsDetailRatingAdapter.notifyItemChanged(it)
                }
                uiData.ratingViewVisibility?.let {
                    ratingRecyclerView.visibility = it
                    ratingRecyclerViewLine.visibility = it
                }
                uiData.ratingArrowRotation?.let {
                    imageViewRatingArrow.rotation = it
                }
                uiData.buyBtnStr?.let {
                    buyBtn.setText(it, TextView.BufferType.SPANNABLE)
                }
                uiData.buyBtnEnable?.let {
                    buyBtn.isEnabled = it
                    buyBtn.visibility = View.VISIBLE
                }
                uiData.brandName?.let {
                    textViewBrand.setText(it, TextView.BufferType.SPANNABLE)
                }
                uiData.brandCode?.let {
                    textViewBrand.tag = it
                }
                uiData.goodsMainImages?.let {
                    imageViewPager.adapter = ViewImagePagerAdapter(context = this, imageUrlList = it)
                    setTabLayoutDot()
                }
                uiData.goodsName?.let {
                    textViewGoodsName.setText(it, TextView.BufferType.SPANNABLE)
                }
                uiData.goodsPrice?.let {
                    textViewGoodsPrice.setText(it, TextView.BufferType.SPANNABLE)
                }
                uiData.goodsPriceUnitVisibility?.let {
                    textViewPriceUnit.visibility = it
                }
            }
        })

        // goods rating 이벤트 observe
        goodsDetailViewModel.goodsRatingData.observe(owner = this, observer = android.arch.lifecycle.Observer { eventData ->
            eventData?.let { cancelIndex ->
                ratingCancelDialog.showDialog(tag = cancelIndex)
            }
        })

        // rating open & close 이벤트 observe
        goodsDetailViewModel.ratingOpenCloseData.observe(owner = this, observer = android.arch.lifecycle.Observer { eventData ->
            eventData?.let { isNoTopic ->
                suggestAddTopicDialog.showDialog(tag = null)
            }
        })

        // goods buy now 이벤트 observe
        goodsDetailViewModel.buyNowEventData.observe(owner = this, observer = android.arch.lifecycle.Observer { eventData ->
            eventData?.let { sno ->
                RunActivity.buyGoodsWebViewActivity(context = this, snoList = arrayListOf(sno), from = AppConstants.FROM_GOODS_DETAIL)
            }
        })

        // recent view goods 이벤트 observe
        goodsDetailViewModel.recentViewGoodsData.observe(owner = this, observer = android.arch.lifecycle.Observer { eventData ->
            eventData?.let { recentViewGoods ->
                goodsDetailViewModel.postRecentViewGoods(authorization = spHelper.authorization,
                        recentViewGoods = recentViewGoods)
            }
        })

        // goods share kakao 이벤트 observe
        goodsDetailViewModel.shareKakaoData.observe(owner = this, observer = android.arch.lifecycle.Observer { eventData ->
            eventData?.let { params ->
                shareViewModel.shareKakao(context = this, shareParams = params)
            }
        })

        // button Action 이벤트 observe
        goodsDetailViewModel.btnActionData.observe(owner = this, observer = android.arch.lifecycle.Observer { eventData ->
            eventData?.let {_->
                eventData.cart?.let {
                    RunActivity.cartActivity(context = this)
                }
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
                    goodsDetailViewModel.backBtnAction()
                }
                R.id.actionShareBtn -> { // 공유하기
                    shareDialog.showDialog(manager = supportFragmentManager, dialogTag = "shareDialog",
                            tagData = null)
                }
                R.id.actionCartBtn -> { // 장바구니
                    goodsDetailViewModel.cartBtnAction(authorization = spHelper.authorization)
                }
                R.id.brandBtn -> { // 브랜드 상품 보기
                    RunActivity.searchBrandGoodsActivity(context = this,
                            brandName = textViewBrand.text.toString(),
                            brandCode = textViewBrand.tag as String)
                }
                R.id.ratingOpenCloseBtn -> { // 제품 평가하기 펼치기&접기
                    goodsDetailViewModel.ratingOpenClose(authorization = spHelper.authorization,
                            currentArrowRotation = imageViewRatingArrow.rotation)
                }
                R.id.buyBtn -> { // 구매하기 버튼
                    goodsDetailViewModel.buyButtonAction(authorization = spHelper.authorization)
                }
            }
        }
    }

    private fun setTabLayoutDot() {
        tabLayoutDot.setupWithViewPager(imageViewPager, true)
        for (i in 0 until tabLayoutDot.tabCount) {
            val tab: View = (tabLayoutDot.getChildAt(0) as ViewGroup).getChildAt(i)
            val params = tab.layoutParams as ViewGroup.MarginLayoutParams
            val marginDp = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3f, resources.displayMetrics).toInt()
            params.setMargins(marginDp, 0, marginDp, 0)
            tab.requestLayout()
        }
        tabLayoutDot.visibility =
                if (tabLayoutDot.tabCount > 1) View.VISIBLE
                else View.GONE
    }

    companion object {
        fun newIntent(context: Context, goodsCode: String): Intent {
            val intent = Intent(context, GoodsDetailActivity::class.java)
            intent.putExtra(IntentPassName.GOODS_CODE, goodsCode)

            return intent
        }
    }
}
