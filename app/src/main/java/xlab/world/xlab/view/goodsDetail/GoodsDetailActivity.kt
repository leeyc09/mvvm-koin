package xlab.world.xlab.view.goodsDetail

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.kakao.kakaolink.v2.KakaoLinkResponse
import com.kakao.kakaolink.v2.KakaoLinkService
import com.kakao.network.ErrorResult
import com.kakao.network.callback.ResponseCallback
import kotlinx.android.synthetic.main.action_bar_my_shop.*
import kotlinx.android.synthetic.main.activity_goods_detail.*
import org.koin.android.architecture.ext.viewModel
import org.koin.android.ext.android.inject
import xlab.world.xlab.R
import xlab.world.xlab.utils.font.FontColorSpan
import xlab.world.xlab.utils.listener.DefaultListener
import xlab.world.xlab.utils.support.*
import xlab.world.xlab.utils.toast.AddCartToast
import xlab.world.xlab.utils.view.dialog.DefaultDialog
import xlab.world.xlab.utils.view.dialog.DefaultProgressDialog
import xlab.world.xlab.utils.view.dialog.DialogCreator
import xlab.world.xlab.utils.view.dialog.ShareBottomDialog
import xlab.world.xlab.utils.view.tabLayout.TabLayoutHelper
import xlab.world.xlab.utils.view.toast.DefaultToast

class GoodsDetailActivity : AppCompatActivity(), View.OnClickListener {
    private val goodsDetailViewModel: GoodsDetailViewModel by viewModel()
    private val spHelper: SPHelper by inject()
    private val fontColorSpan: FontColorSpan by inject()

    private var resultCode = Activity.RESULT_CANCELED

    private lateinit var defaultToast: DefaultToast
    private lateinit var addCartToast: AddCartToast

    private lateinit var progressDialog: DefaultProgressDialog
    private lateinit var loginDialog: DefaultDialog
    private lateinit var suggestAddTopicDialog: DefaultDialog
    private lateinit var shareDialog: ShareBottomDialog

    private lateinit var tabLayoutHelper: TabLayoutHelper

    private lateinit var defaultListener: DefaultListener
    private val shareDialogListener = object: ShareBottomDialog.Listener {
        override fun onCopyLink(tag: Any?) {
            defaultToast.showToast(resources.getString(R.string.copy_link_success))
        }

        override fun onShareKakao(tag: Any?) {
            PrintLog.d("share", "kakao", goodsDetailViewModel.tag)
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

        when (resultCode) {
            Activity.RESULT_OK -> {
                if (this.resultCode == Activity.RESULT_CANCELED)
                    this.resultCode = Activity.RESULT_OK
                when (requestCode) {
                    RequestCodeData.TOPIC_ADD -> { // 토픽 추가
//                        // 펫 정보
//                        loadGoodsDetailPet(goodsCode, { petMatchData, petRatingData ->
//                            itemDetailMatchAdapter.updateData(petMatchData)
//                            itemDetailRatingAdapter.updateData(petRatingData)
//                        },{
//                        })
//                        // 통계 정보
//                        statsFragment.initStatsData { }
                    }
                    RequestCodeData.PROFILE, // 프로필
                    RequestCodeData.POST_DETAIL, // 포스트 상세
                    RequestCodeData.GOODS_BRAND_SEARCH, // 브랜드 검색
                    RequestCodeData.MY_CART -> { // 장바구니
//                        // 카트 숫자
//                        loadCartCountData({ count ->
//                            textViewCartCnt.setText(count.toString(), TextView.BufferType.SPANNABLE)
//                            textViewCartCnt.visibility =
//                                    if (count > 0) View.VISIBLE
//                                    else View.GONE
//                        }, {
//                        })
//                        // 펫 정보
//                        loadGoodsDetailPet(goodsCode, { petMatchData, petRatingData ->
//                            itemDetailMatchAdapter.updateData(petMatchData)
//                            itemDetailRatingAdapter.updateData(petRatingData)
//                        },{
//                        })
//                        // 통계 정보
//                        statsFragment.initStatsData { }
//                        // 포스트 정보
//                        postsFragment.initAllData { max, current ->
//                        }
                    }
                    RequestCodeData.GOODS_BUYING -> { // 구매하기
//                        data?.let {
//                            val orderNo = data.getStringExtra(BuyGoodsWebViewActivity.orderNoStr)
//                            val intent = CompleteBuyingActivity.newIntent(this, orderNo)
//                            startActivityForResult(intent, RequestCodeData.COMPLETE_BUYING)
//                        }
                    }
                    RequestCodeData.COMPLETE_BUYING -> { // 결제 완료
//                        reloadAllData { max, current -> }
                    }
                }
            }
            ResultCodeData.LOGIN_SUCCESS -> { // login -> reload all data
//                this.resultCode = ResultCodeData.LOGIN_SUCCESS
//                reloadAllData { max, current -> }
            }
            ResultCodeData.LOGOUT_SUCCESS -> { // logout -> finish activity
                setResult(ResultCodeData.LOGOUT_SUCCESS)
                finish()
            }
        }
    }

    private fun onSetup() {
        appBarLayout.stateListAnimator = null

        defaultToast = DefaultToast(context = this)
        addCartToast = AddCartToast(context = this)

        progressDialog = DefaultProgressDialog(context = this)
        loginDialog = DialogCreator.loginDialog(context = this)
        suggestAddTopicDialog = DialogCreator.suggestAddTopicDialog(context = this)
        shareDialog = DialogCreator.shareDialog(context = this, listener = shareDialogListener)

        defaultListener = DefaultListener(this)

        // tab layout 초기화
        tabLayoutHelper = TabLayoutHelper(context = this,
                defaultSelectFont = fontColorSpan.notoBold000000,
                defaultUnSelectFont = fontColorSpan.notoMediumBFBFBF,
                useDefaultEvent = false,
                listener = null)
        tabLayoutHelper.handle(layout = tabLayout, viewPager = detailViewPager)
        tabLayoutHelper.addTab(resources.getString(R.string.item_info), tabLayout = null, fontSize = null, selectFont = null, unSelectFont = null, extraData = null)
        tabLayoutHelper.addTab(resources.getString(R.string.stats), tabLayout = null, fontSize = null, selectFont = null, unSelectFont = null, extraData = null)
        tabLayoutHelper.addTab(resources.getString(R.string.post), tabLayout = null, fontSize = null, selectFont = null, unSelectFont = null, extraData = null)
        tabLayoutHelper.changeSelectedTab(selectIndex = 0)
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

    }

    override fun onClick(v: View?) {
        v?.let {
            when (v.id) {
                R.id.actionBackBtn -> { // 뒤로가기
                    setResult(resultCode)
                    finish()
                }
                R.id.actionShareBtn -> { // 공유하기
//                    shareDialog.show(supportFragmentManager, "shareDialog")
                }
                R.id.actionCartBtn -> { // 장바구니

                }
                R.id.brandBtn -> { // 브랜드 상품 보기
//                    val intent = GoodsBrandActivity.newIntent(this,
//                            brandName = textViewBrand.text.toString(),
//                            brandCode = goodsDetailData.goods.brandCd)
//                    startActivityForResult(intent, RequestCodeData.GOODS_BRAND_SEARCH)
                }
                R.id.ratingOpenCloseBtn -> { // 제품 평가하기 펼치기&접기
                    // login check
//                    if (SPHelper(this).accessToken == "") { // guest mode
//                        loginDialog.show()
//                        return
//                    }
//                    if (matchingRecyclerView.visibility != View.VISIBLE) { // no topic
//                        topicDialog.show()
//                        return
//                    }
//                    if (imageViewRatingArrow.rotation == 0f) {
//                        ratingRecyclerView.visibility = View.VISIBLE
//                        ratingRecyclerViewLine.visibility = View.VISIBLE
//                        imageViewRatingArrow.rotation = 180f
//                    } else {
//                        ratingRecyclerView.visibility = View.GONE
//                        ratingRecyclerViewLine.visibility = View.GONE
//                        imageViewRatingArrow.rotation = 0f
//                    }
                }
                R.id.buyBtn -> { // 구매하기 버튼
                    // login check
//                    if (SPHelper(this).accessToken == "") { // guest mode
//                        loginDialog.show()
//                        return
//                    }
//                    buyGoodsOptionDialog.show(supportFragmentManager, "BuyGoodsOptionDialog")
                }
            }
        }
    }

    companion object {
        fun newIntent(context: Context, goodsCode: String): Intent {
            val intent = Intent(context, GoodsDetailActivity::class.java)
            intent.putExtra(IntentPassName.GOODS_CODE, goodsCode)

            return intent
        }
    }
}
