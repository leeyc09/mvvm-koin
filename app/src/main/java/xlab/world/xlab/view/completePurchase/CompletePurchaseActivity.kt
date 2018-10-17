package xlab.world.xlab.view.completePurchase

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_complete_purchase.*
import org.koin.android.architecture.ext.viewModel
import org.koin.android.ext.android.inject
import xlab.world.xlab.R
import xlab.world.xlab.utils.support.IntentPassName
import xlab.world.xlab.utils.support.SPHelper
import xlab.world.xlab.utils.view.dialog.DefaultProgressDialog
import xlab.world.xlab.utils.view.toast.DefaultToast

class CompletePurchaseActivity : AppCompatActivity(), View.OnClickListener {
    private val completePurchaseViewModel: CompletePurchaseViewModel by viewModel()
    private val spHelper: SPHelper by inject()

    private lateinit var defaultToast: DefaultToast
    private lateinit var progressDialog: DefaultProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_complete_purchase)

        onSetup()

        onBindEvent()

        observeViewModel()
    }

    override fun onBackPressed() {
        continueBtn.performClick()
    }

    private fun onSetup() {
        // Toast, Dialog 초기화
        defaultToast = DefaultToast(context = this)
        progressDialog = DefaultProgressDialog(context = this)

        completePurchaseViewModel.loadCompleteOrderData(context = this,
                authorization = spHelper.authorization,
                orderNo = intent.getStringExtra(IntentPassName.ORDER_NO))
    }

    private fun onBindEvent() {
        continueBtn.setOnClickListener(this) // 쇼핑계속하기
    }

    private fun observeViewModel() {
        // UI 이벤트 observe
        completePurchaseViewModel.uiData.observe(this, android.arch.lifecycle.Observer { uiData ->
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
                uiData.titleStr?.let { // 타이틀 이름
                    textViewTitle.setText(it, TextView.BufferType.SPANNABLE)
                }
                uiData.subTitleVisibility?.let { // 서브 타이틀 보이기 & 숨기기
                    textViewSubTitle.visibility = it
                }
                uiData.paymentPriceVisibility?.let { // 결제 가격 보이기 & 숨기기
                    paymentPriceLayout.visibility = it
                }
                uiData.paymentPrice?.let { // 결제 가격
                    textViewPaymentPrice.setText(it, TextView.BufferType.SPANNABLE)
                }
                uiData.bankInfoVisibility?.let { // 입금 은행 정보 보이기 & 숨기기
                    bankInfoLayout.visibility = it
                }
                uiData.depositPrice?.let { // 입금 금액
                    textViewDepositPrice.setText(it, TextView.BufferType.SPANNABLE)
                }
                uiData.depositBank?.let { // 입금 은행
                    textViewDepositBank.setText(it, TextView.BufferType.SPANNABLE)
                }
                uiData.depositBankVisibility?.let { // 입금 은행 보이기 & 숨기기
                    depositBanklayout.visibility = it
                }
                uiData.depositAccount?.let { // 입금 계좌
                    textViewDepositAccount.setText(it, TextView.BufferType.SPANNABLE)
                }
                uiData.depositAccountVisibility?.let { // 입금 계좌 보이기 & 숨기기
                    depositAccountLayout.visibility = it
                }
                uiData.accountHolder?.let { // 예금주
                    textViewAccountHolder.setText(it, TextView.BufferType.SPANNABLE)
                }
                uiData.accountHolderVisibility?.let { // 예금주 보이기 & 숨기기
                    accountHolderLayout.visibility = it
                }
                uiData.depositName?.let { // 입금자명
                    textViewDepositName.setText(it, TextView.BufferType.SPANNABLE)
                }
                uiData.depositNameVisibility?.let { // 입금자명 보이기 & 숨기기
                    depositNameLayout.visibility = it
                }
                uiData.orderNo?.let { // 주문 번호
                    textViewOrderNo.setText(it, TextView.BufferType.SPANNABLE)
                }
                uiData.orderGoodsName?.let { // 상품 이름
                    textViewOrderGoodsName.setText(it, TextView.BufferType.SPANNABLE)
                }
                uiData.receiverName?.let { // 수령인
                    textViewReceiverName.setText(it, TextView.BufferType.SPANNABLE)
                }
                uiData.receiverPhone?.let { // 수령인 연락처
                    textViewReceiverPhone.setText(it, TextView.BufferType.SPANNABLE)
                }
                uiData.receiverZoneCode?.let { // 수령인 우편주소
                    textViewReceiverZoneCode.setText(it, TextView.BufferType.SPANNABLE)
                }
                uiData.receiverAddress?.let { // 수령인 주소
                    textViewReceiverAddress.setText(it, TextView.BufferType.SPANNABLE)
                }
                uiData.receiverSubAddress?.let { // 수령인 상세 주소
                    textViewReceiverSubAddress.setText(it, TextView.BufferType.SPANNABLE)
                }
                uiData.receiverSubAddressVisibility?.let { // 수령인 상세 주소 보이기 & 숨기기
                    textViewReceiverSubAddress.visibility = it
                }
                uiData.paymentType?.let { // 결제 방식
                    textViewPaymentPlan.setText(it, TextView.BufferType.SPANNABLE)
                }
            }
        })
    }

    override fun onClick(v: View?) {
        v?.let {
            when (v.id) {
                R.id.continueBtn -> { // 쇼핑계속하기
                    setResult(Activity.RESULT_OK)
                    finish()
                }
            }
        }
    }

    companion object {
        fun newIntent(context: Context, orderNo: String): Intent {
            val intent = Intent(context, CompletePurchaseActivity::class.java)
            intent.putExtra(IntentPassName.ORDER_NO, orderNo)

            return intent
        }
    }
}
