package xlab.world.xlab.utils.view.dialog

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.support.v4.content.res.ResourcesCompat
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.*
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.dialog_order_state.*
import xlab.world.xlab.R
import xlab.world.xlab.data.adapter.GoodsOrderListData
import xlab.world.xlab.utils.support.AppConstants
import xlab.world.xlab.utils.support.RequestCodeData
import xlab.world.xlab.utils.support.RunActivity
import xlab.world.xlab.utils.support.SupportData
import xlab.world.xlab.utils.view.toast.DefaultToast
import xlab.world.xlab.view.webView.DefaultWebViewActivity
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*

class OrderStateDialog(private val context: Activity,
                       private val receiveConfirmListener: DefaultOneDialog.Listener,
                       private val buyDecideListener: DefaultOneDialog.Listener): Dialog(context), View.OnClickListener {

    private var receiveConfirmDialog: DefaultOneDialog? = null
    private var buyDecideDialog: DefaultOneDialog? = null

    private var goods: GoodsOrderListData? = null

    private val imagePlaceHolder = ColorDrawable(ResourcesCompat.getColor(context.resources, R.color.colorE2E2E2, null))
    private val glideOption = RequestOptions()
            .centerCrop()
            .placeholder(imagePlaceHolder)
            .error(imagePlaceHolder)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_order_state)

        onBindEvent()
    }

    override fun onStart() {
        onSetup()
        super.onStart()
    }

    override fun dismiss() {
        this.goods = null
        super.dismiss()
    }

    fun showDialog(goods: GoodsOrderListData) {
        this.goods = goods
        super.show()
    }

    fun onSetup() {
        receiveConfirmDialog = receiveConfirmDialog ?:
                DialogCreator.receiveConfirmDialog(context = context, listener = receiveConfirmListener)
        buyDecideDialog = buyDecideDialog ?:
                DialogCreator.buyDecideDialog(context = context, listener = buyDecideListener)

        goods?.let {
            // 상품 이미지
            Glide.with(context)
                    .load(it.image)
                    .apply(glideOption)
                    .into(imageViewGoods)

            // 상품 브랜드 & 이름
            val titleStr = SpannableString("${it.brand} ${it.name}")
            val grayColor = ResourcesCompat.getColor(context.resources, R.color.color6D6D6D, null)
            val blackColor = ResourcesCompat.getColor(context.resources, R.color.color000000, null)
            titleStr.setSpan(ForegroundColorSpan(grayColor), 0, it.brand.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            titleStr.setSpan(ForegroundColorSpan(blackColor), it.brand.length, titleStr.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            textViewTitle.setText(titleStr, TextView.BufferType.SPANNABLE)

            // 상품 옵션 -> 옵션 있는 경우만 표기
            if (it.option != null) {
                optionLayout.visibility = View.VISIBLE

                var optionStr = ""
                it.option.forEachIndexed { index, option ->
                    optionStr += option
                    if (index < it.option.size - 1)
                        optionStr += " ∙ "
                }
                textViewOption.setText(optionStr, TextView.BufferType.SPANNABLE)
            } else { // no options
                optionLayout.visibility = View.GONE
            }

            // 상품 갯수 & 가격
            textViewGoodsNum.setText(String.format("${it.count}개"), TextView.BufferType.SPANNABLE)
            textViewPrice.setText(SupportData.applyPriceFormat(price = it.price), TextView.BufferType.SPANNABLE)

            // 버튼 활성화
            when (it.orderStatus) {
                "o1", "p1", "g1", "d1", "d2", "s1" -> { // 일반
                    childLayoutVisible(R.id.normalLayout)
                    normalStepLayoutVisible(it.orderStatus)
                }
                "e1", "e2", "e3", "e4", "e5", "er",
                "p1en", "g1en", "d1en", "d2en", "s1en",
                "z1", "z2", "z3", "z4", "z5"-> { // 교환
                    childLayoutVisible(R.id.changeLayout)
                    changeStepLayoutVisible(it.orderStatus)
                }
                "b1", "b2", "b3", "b4", "br",
                "d1bn", "d2bn", "s1bn" -> { // 반품
                    childLayoutVisible(R.id.returnLayout)
                    returnStepLayoutVisible(it.orderStatus)
                }
                "r1", "r2", "r3", "rr",
                "p1rn", "g1rn", "d1rn", "d2rn", "s1rn" -> { // 환불
                    childLayoutVisible(R.id.refundLayout)
                    refundStepLayoutVisible(it.orderStatus)
                }
                "c1", "c2", "c4", "c5" -> { // 취소
                    childLayoutVisible(-1)
                }
                else -> { // etc
                    childLayoutVisible(-1)
                }
            }
        }
    }

    private fun onBindEvent() {
        closeBtn.setOnClickListener(this) // 닫기
        callInquiryBtn.setOnClickListener(this) // 전화문의
        kakaoInquiryBtn.setOnClickListener(this) // 카카오문의

        // 일반
        requestRefundBtn.setOnClickListener(this) // 환불신청
        checkDeliveryBtn.setOnClickListener(this) // 배송조회
        checkReceiptBtn.setOnClickListener(this) // 수취확인
        requestChangeBtn.setOnClickListener(this) // 교환신청
        requestReturnBtn.setOnClickListener(this) // 반품신청
        decideBuyBtn.setOnClickListener(this) // 구매확정
        goodsAssessmentBtn.setOnClickListener(this) // 제품평가

        // 교환
        changeDetailBtn.setOnClickListener(this) // 교환상세
        checkDeliveryBtn2.setOnClickListener(this) // 배송조회
        checkReceiptBtn2.setOnClickListener(this) // 수취확인
        decideBuyBtn2.setOnClickListener(this) // 구매확정
        goodsAssessmentBtn2.setOnClickListener(this) // 제품평가

        // 반품
        returnDetailBtn.setOnClickListener(this) // 반품상세
        checkDeliveryBtn3.setOnClickListener(this) // 배송조회
        checkReceiptBtn3.setOnClickListener(this) // 수취확인
        decideBuyBtn3.setOnClickListener(this) // 구매확정
        goodsAssessmentBtn3.setOnClickListener(this) // 제품평가

        // 환불
        refundDetailBtn.setOnClickListener(this) // 환불상세
        checkDeliveryBtn4.setOnClickListener(this) // 배송조회
        checkReceiptBtn4.setOnClickListener(this) // 수취확인
        decideBuyBtn4.setOnClickListener(this) // 구매확정
        goodsAssessmentBtn4.setOnClickListener(this) // 제품평가
    }

    override fun onClick(v: View?) {
        v?.let {
            when (v.id) {
                R.id.closeBtn -> { // 닫기
                    dismiss()
                }
                R.id.callInquiryBtn -> { // 전화문의
                    val intent = Intent(Intent.ACTION_DIAL)
                    val number = when (goods?.deliveryNo!!) {
                        AppConstants.CS_HOLAPET -> context.getString(R.string.esc_company_phone_num)
                        AppConstants.CS_XLAB -> context.getString(R.string.xlab_cs_num)
                        else -> ""
                    }
                    intent.data = Uri.parse("tel:$number")
                    context.startActivity(intent)
                }
                R.id.kakaoInquiryBtn -> { // 카카오문의
                    val kakoId = when (goods?.deliveryNo!!) {
                        AppConstants.CS_HOLAPET -> context.getString(R.string.holapet_kakao_id)
                        AppConstants.CS_XLAB -> context.getString(R.string.xlab_kakao_id)
                        else -> ""
                    }
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(AppConstants.KAKO_PLUS_CHAT + kakoId))
                    context.startActivity(intent)
                }
                R.id.requestRefundBtn -> { // 환불신청
                    RunActivity.orderCRRActivity(context = context, crrMode = RequestCodeData.ORDER_REFUND, orderNo = goods!!.orderNo, sno = goods!!.sno)
                }
                R.id.checkDeliveryBtn,
                R.id.checkDeliveryBtn2,
                R.id.checkDeliveryBtn3,
                R.id.checkDeliveryBtn4 -> { // 배송조회
                    RunActivity.defaultWebViewActivity(context = context, pageTitle = null,
                            webUrl = AppConstants.TRACKING_DELIVER_URL + goods!!.invoiceNo, zoomControl = true)
                }
                R.id.checkReceiptBtn,
                R.id.checkReceiptBtn2,
                R.id.checkReceiptBtn3,
                R.id.checkReceiptBtn4 -> { // 수취확인
                    receiveConfirmDialog?.showDialog(tag = goods)
                    dismiss()
                }
                R.id.requestChangeBtn -> { // 교환신청
                    RunActivity.orderCRRActivity(context = context, crrMode = RequestCodeData.ORDER_CHANGE, orderNo = goods!!.orderNo, sno = goods!!.sno)
                }
                R.id.requestReturnBtn -> { // 반품신청
                    RunActivity.orderCRRActivity(context = context, crrMode = RequestCodeData.ORDER_RETURN, orderNo = goods!!.orderNo, sno = goods!!.sno)
                }
                R.id.decideBuyBtn,
                R.id.decideBuyBtn2,
                R.id.decideBuyBtn3,
                R.id.decideBuyBtn4 -> { // 구매확정
                    buyDecideDialog?.showDialog(tag = goods)
                    dismiss()
                }
                R.id.goodsAssessmentBtn,
                R.id.goodsAssessmentBtn2,
                R.id.goodsAssessmentBtn3,
                R.id.goodsAssessmentBtn4 -> { // 제품평가
                    RunActivity.goodsRatingActivity(context = context, goodsCode = goods!!.code)
                }
                R.id.returnDetailBtn, // 반품상세
                R.id.changeDetailBtn, // 교환상세
                R.id.refundDetailBtn -> { // 환불상세
                    RunActivity.crrDetailActivity(context = context, handleSno = goods!!.userHandleSno)
                }
            }
        }
    }

    // show specific layout view
    private fun childLayoutVisible(visibleLayoutId: Int) {
        val childCount = orderOptionLayout.childCount
        (0 until childCount).forEach { index ->
            val childLayout = orderOptionLayout.getChildAt(index)
            childLayout.visibility =
                    if (childLayout.id == visibleLayoutId) View.VISIBLE
                    else View.GONE
        }
    }

    // normal step child layout visible set.
    private fun normalStepLayoutVisible(orderStatus: String) {
        when (orderStatus) {
            "o1" -> { // 입금대기
                requestRefundBtn.visibility = View.GONE // 환불신청
                checkDeliveryBtn.visibility = View.GONE // 배송조회
                checkReceiptBtn.visibility = View.GONE // 수취확인
                requestChangeBtn.visibility = View.GONE // 교환신청
                requestReturnBtn.visibility = View.GONE // 반품신청
                decideBuyBtn.visibility = View.GONE // 구매확정
                goodsAssessmentBtn.visibility = View.GONE // 제품평가
            }
            "p1", "g1" -> { // 결제완료, 상품준비중
                requestRefundBtn.visibility = View.VISIBLE // 환불신청
                checkDeliveryBtn.visibility = View.GONE // 배송조회
                checkReceiptBtn.visibility = View.GONE // 수취확인
                requestChangeBtn.visibility = View.VISIBLE // 교환신청
                requestReturnBtn.visibility = View.GONE // 반품신청
                decideBuyBtn.visibility = View.GONE // 구매확정
                goodsAssessmentBtn.visibility = View.GONE // 제품평가
            }
            "d1" -> { // 배송중
                requestRefundBtn.visibility = View.GONE // 환불신청
                checkDeliveryBtn.visibility = View.VISIBLE // 배송조회
                checkReceiptBtn.visibility = View.VISIBLE // 수취확인
                requestChangeBtn.visibility = View.VISIBLE // 교환신청
                requestReturnBtn.visibility = View.VISIBLE // 반품신청
                decideBuyBtn.visibility = View.VISIBLE // 구매확정
                goodsAssessmentBtn.visibility = View.GONE // 제품평가
            }
            "d2" -> { // 배송완료
                requestRefundBtn.visibility = View.GONE // 환불신청
                checkDeliveryBtn.visibility = View.VISIBLE // 배송조회
                checkReceiptBtn.visibility = View.GONE // 수취확인
                requestChangeBtn.visibility = View.VISIBLE // 교환신청
                requestReturnBtn.visibility = View.VISIBLE // 반품신청
                decideBuyBtn.visibility = View.VISIBLE // 구매확정
                goodsAssessmentBtn.visibility = View.GONE // 제품평가
            }
            "s1" -> { // 구매확정
                requestRefundBtn.visibility = View.GONE // 환불신청
                checkDeliveryBtn.visibility = View.GONE // 배송조회
                checkReceiptBtn.visibility = View.GONE // 수취확인
                requestChangeBtn.visibility = View.GONE // 교환신청
                requestReturnBtn.visibility = View.GONE // 반품신청
                decideBuyBtn.visibility = View.GONE // 구매확정
                goodsAssessmentBtn.visibility = View.VISIBLE // 제품평가
            }
            else -> {
                requestRefundBtn.visibility = View.GONE // 환불신청
                checkDeliveryBtn.visibility = View.GONE // 배송조회
                checkReceiptBtn.visibility = View.GONE // 수취확인
                requestChangeBtn.visibility = View.GONE // 교환신청
                requestReturnBtn.visibility = View.GONE // 반품신청
                decideBuyBtn.visibility = View.GONE // 구매확정
                goodsAssessmentBtn.visibility = View.GONE // 제품평가
            }
        }
    }

    // change step child layout visible set.
    private fun changeStepLayoutVisible(orderStatus: String) {
        when (orderStatus) {
            "er", "e1", "e2", "e4", // 교환신청, 교환접수, 교환 보류,
            "z1", "z2", "z5", // 추가입금대기, 추가결제완료, 교환추가완료,
            "p1en", "g1en"-> { // 교환거절 (결제완료, 상품준비중)
                changeDetailBtn.visibility = View.VISIBLE // 교환상세
                checkDeliveryBtn2.visibility = View.GONE // 배송조회
                checkReceiptBtn2.visibility = View.GONE // 수취확인
                decideBuyBtn2.visibility = View.GONE // 구매확정
                goodsAssessmentBtn2.visibility = View.GONE // 제품평가
            }
            "e3", "z3", "z4" -> { // 재배송중, 추가배송중, 추가배송완료
                changeDetailBtn.visibility = View.VISIBLE // 교환상세
                checkDeliveryBtn2.visibility = View.VISIBLE // 배송조회
                checkReceiptBtn2.visibility = View.GONE // 수취확인
                decideBuyBtn2.visibility = View.GONE // 구매확정
                goodsAssessmentBtn2.visibility = View.GONE // 제품평가
            }
            "e5" -> { // 교환완료
                changeDetailBtn.visibility = View.VISIBLE // 교환상세
                checkDeliveryBtn2.visibility = View.GONE // 배송조회
                checkReceiptBtn2.visibility = View.GONE // 수취확인
                decideBuyBtn2.visibility = View.GONE // 구매확정
                goodsAssessmentBtn2.visibility = View.VISIBLE // 제품평가
            }
            "d1en" -> { // 교환거절 (배송중)
                changeDetailBtn.visibility = View.VISIBLE // 교환상세
                checkDeliveryBtn2.visibility = View.VISIBLE // 배송조회
                checkReceiptBtn2.visibility = View.VISIBLE // 수취확인
                decideBuyBtn2.visibility = View.VISIBLE // 구매확정
                goodsAssessmentBtn2.visibility = View.GONE // 제품평가
            }
            "d2en" -> { // 교환거절 (배송완료)
                changeDetailBtn.visibility = View.VISIBLE // 교환상세
                checkDeliveryBtn2.visibility = View.VISIBLE // 배송조회
                checkReceiptBtn2.visibility = View.GONE // 수취확인
                decideBuyBtn2.visibility = View.VISIBLE // 구매확정
                goodsAssessmentBtn2.visibility = View.GONE // 제품평가
            }
            "s1en" -> { // 교환거절 (구매확정)
                changeDetailBtn.visibility = View.VISIBLE // 교환상세
                checkDeliveryBtn2.visibility = View.GONE // 배송조회
                checkReceiptBtn2.visibility = View.GONE // 수취확인
                decideBuyBtn2.visibility = View.GONE // 구매확정
                goodsAssessmentBtn2.visibility = View.VISIBLE // 제품평가
            }
        }
    }

    // return step child layout visible set.
    private fun returnStepLayoutVisible(orderStatus: String) {
        when (orderStatus) {
            "b1", "b2", "b3", "b4", "br" -> { // 반품접수, 반송중, 반품보류, 반품회수완료, 반품신청
                returnDetailBtn.visibility = View.VISIBLE // 반품상세
                checkDeliveryBtn3.visibility = View.GONE // 배송조회
                checkReceiptBtn3.visibility = View.GONE // 수취확인
                decideBuyBtn3.visibility = View.GONE // 구매확정
                goodsAssessmentBtn3.visibility = View.GONE // 제품평가
            }
            "d1bn" -> { // 반품거절 (배송중)
                returnDetailBtn.visibility = View.VISIBLE // 반품상세
                checkDeliveryBtn3.visibility = View.VISIBLE // 배송조회
                checkReceiptBtn3.visibility = View.VISIBLE // 수취확인
                decideBuyBtn3.visibility = View.VISIBLE // 구매확정
                goodsAssessmentBtn3.visibility = View.GONE // 제품평가
            }
            "d2bn" -> { // 반품거절 (배송완료)
                returnDetailBtn.visibility = View.VISIBLE // 반품상세
                checkDeliveryBtn3.visibility = View.VISIBLE // 배송조회
                checkReceiptBtn3.visibility = View.GONE // 수취확인
                decideBuyBtn3.visibility = View.VISIBLE // 구매확정
                goodsAssessmentBtn3.visibility = View.GONE // 제품평가
            }
            "s1bn" -> { // 반품거절 (구매확정)
                returnDetailBtn.visibility = View.VISIBLE // 반품상세
                checkDeliveryBtn3.visibility = View.GONE // 배송조회
                checkReceiptBtn3.visibility = View.GONE // 수취확인
                decideBuyBtn3.visibility = View.GONE // 구매확정
                goodsAssessmentBtn3.visibility = View.VISIBLE // 제품평가
            }
        }
    }

    // refund step child layout visible set.
    private fun refundStepLayoutVisible(orderStatus: String) {
        when (orderStatus) {
            "r1", "r2", "r3", // 환불접수, 환불보류, 환불완료,
            "rr", "p1rn", "g1rn" -> { // 환불신청, 환불거절 (결제완료, 상품준비중)
                refundDetailBtn.visibility = View.VISIBLE // 환불상세
                checkDeliveryBtn4.visibility = View.GONE // 배송조회
                checkReceiptBtn4.visibility = View.GONE // 수취확인
                decideBuyBtn4.visibility = View.GONE // 구매확정
                goodsAssessmentBtn4.visibility = View.GONE // 제품평가
            }
            "d1rn" -> { // 환불거절 (배송중)
                refundDetailBtn.visibility = View.VISIBLE // 환불상세
                checkDeliveryBtn4.visibility = View.VISIBLE // 배송조회
                checkReceiptBtn4.visibility = View.VISIBLE // 수취확인
                decideBuyBtn4.visibility = View.GONE // 구매확정
                goodsAssessmentBtn4.visibility = View.GONE // 제품평가
            }
            "d2rn" -> { // 환불거절 (배송완료)
                refundDetailBtn.visibility = View.VISIBLE // 환불상세
                checkDeliveryBtn4.visibility = View.VISIBLE // 배송조회
                checkReceiptBtn4.visibility = View.GONE // 수취확인
                decideBuyBtn4.visibility = View.VISIBLE // 구매확정
                goodsAssessmentBtn4.visibility = View.GONE // 제품평가
            }
            "s1rn" -> { // 환불거절 (구매확정)
                refundDetailBtn.visibility = View.VISIBLE // 환불상세
                checkDeliveryBtn4.visibility = View.GONE // 배송조회
                checkReceiptBtn4.visibility = View.GONE // 수취확인
                decideBuyBtn4.visibility = View.GONE // 구매확정
                goodsAssessmentBtn4.visibility = View.VISIBLE // 제품평가
            }
        }
    }
}