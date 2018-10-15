package xlab.world.xlab.view.myShopping

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import kotlinx.android.synthetic.main.action_bar_my_shop.*
import kotlinx.android.synthetic.main.activity_my_shopping.*
import org.koin.android.architecture.ext.viewModel
import org.koin.android.ext.android.inject
import xlab.world.xlab.R
import xlab.world.xlab.utils.support.*
import xlab.world.xlab.utils.view.dialog.DefaultProgressDialog
import xlab.world.xlab.utils.view.toast.DefaultToast

class MyShoppingActivity : AppCompatActivity(), View.OnClickListener {
    private val myShoppingViewModel: MyShoppingViewModel by viewModel()
    private val spHelper: SPHelper by inject()

    private var resultCode = Activity.RESULT_CANCELED

    private lateinit var defaultToast: DefaultToast
    private lateinit var progressDialog: DefaultProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_shopping)

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
                    RequestCodeData.RECENT_VIEW_GOODS, // 최근 본 제품
                    RequestCodeData.MY_CART -> { // 장바구니
//                        progressDialog.show()
//                        reloadAllData { max, current ->
//                            if (max == current)
//                                progressDialog.dismiss()
//                        }
                    }
                    RequestCodeData.MY_SHOP_PROFILE_EDIT -> { // 쇼핑 정보 수정
                        myShoppingViewModel.loadShopProfile(authorization = spHelper.authorization)
                    }
                    RequestCodeData.STATUS_HISTORY, // 상태에 따른 리스트
                    RequestCodeData.ORDER_DETAIL -> { // 주문 상세
//                        progressDialog.show()
//                        reloadAllData { max, current ->
//                            if (max == current)
//                                progressDialog.dismiss()
//                        }
                    }
                    RequestCodeData.ORDER_REFUND -> { // 환불 신청
//                        orderStateDialog.dismiss()
//                        progressDialog.show()
//                        reloadAllData { max, current ->
//                            if (max == current) {
//                                progressDialog.dismiss()
//                                defaultToast.showToast("환불 신청되었습니다")
//                            }
//                        }
                    }
                    RequestCodeData.ORDER_RETURN -> { // 반품 신청
//                        orderStateDialog.dismiss()
//                        progressDialog.show()
//                        reloadAllData { max, current ->
//                            if (max == current) {
//                                progressDialog.dismiss()
//                                defaultToast.showToast("반품 신청되었습니다")
//                            }
//                        }
                    }
                    RequestCodeData.ORDER_CHANGE -> { // 교환 신청
//                        orderStateDialog.dismiss()
//                        progressDialog.show()
//                        reloadAllData { max, current ->
//                            if (max == current) {
//                                progressDialog.dismiss()
//                                defaultToast.showToast("교환 신청되었습니다")
//                            }
//                        }
                    }
                    RequestCodeData.GOODS_RATING -> {
//                        defaultToast.showToast(resources.getString(R.string.rating_success))
//                        orderStateDialog.dismiss()
                    }
                }
            }
            ResultCodeData.LOGOUT_SUCCESS -> { // logout -> finish activity
                setResult(ResultCodeData.LOGOUT_SUCCESS)
                finish()
            }
        }
    }

    private fun onSetup() {
        actionShareBtn.visibility = View.GONE
        actionBarTitle.setText(getText(R.string.my_shopping), TextView.BufferType.SPANNABLE)

        // appBarLayout 애니메이션 없애기
        appBarLayout.stateListAnimator = null

        defaultToast = DefaultToast(context = this)
        progressDialog = DefaultProgressDialog(context = this)

        myShoppingViewModel.loadShopProfile(authorization = spHelper.authorization)
        myShoppingViewModel.loadOrderStateCnt(authorization = spHelper.authorization)
    }

    private fun onBindEvent() {
        actionBackBtn.setOnClickListener(this) // 뒤로가기
        actionCartBtn.setOnClickListener(this) // 카트
        recentShowGoodsBtn.setOnClickListener(this) // 최근 본 상품
        editShopInfoBtn.setOnClickListener(this) // 쇼핑 정보 수정
        completePaymentLayout.setOnClickListener(this) // 결제완료 리스트 보기
        deliveryStatusLayout.setOnClickListener(this) // 배송중/완료 리스트 보기
        exchangeCancelLayout.setOnClickListener(this) // 취소/교환/환불 리스트 보기
        refundLayout.setOnClickListener(this) // 환불 리스트 보기
    }

    private fun observeViewModel() {
        // UI 이벤트 observe
        myShoppingViewModel.uiData.observe(this, android.arch.lifecycle.Observer { uiData ->
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
                uiData.shopName?.let {
                    textViewName.setText(it, TextView.BufferType.SPANNABLE)
                }
                uiData.completePaymentCnt?.let {
                    textViewCompletePaymentCnt.setText(it, TextView.BufferType.SPANNABLE)
                }
                uiData.completePaymentEnable?.let {
                    completePaymentLayout.isEnabled = it
                    textViewCompletePaymentCnt.isEnabled = it
                }
                uiData.deliveryCnt?.let {
                    textViewDeliveringCnt.setText(it, TextView.BufferType.SPANNABLE)
                }
                uiData.deliveryEnable?.let {
                    deliveryStatusLayout.isEnabled = it
                    textViewDeliveringCnt.isEnabled = it
                }
                uiData.cancelCnt?.let {
                    textViewCancelCnt.setText(it, TextView.BufferType.SPANNABLE)
                }
                uiData.cancelEnable?.let {
                    exchangeCancelLayout.isEnabled = it
                    textViewCancelCnt.isEnabled = it
                }
                uiData.refundCnt?.let {
                    textViewRefundCnt.setText(it, TextView.BufferType.SPANNABLE)
                }
                uiData.refundEnable?.let {
                    refundLayout.isEnabled = it
                    textViewRefundCnt.isEnabled = it
                }
            }
        })
    }

    override fun onClick(v: View?) {
        v?.let {
            when (v.id) {
                R.id.actionBackBtn -> { // 뒤로가기
                    finish()
                }
                R.id.actionCartBtn -> { // 카트

                }
                R.id.recentShowGoodsBtn -> { // 최근 본 상품

                }
                R.id.editShopInfoBtn -> { // 쇼핑 정보 수정
                    RunActivity.shopProfileEditActivity(context = this)
                }
                R.id.completePaymentLayout,
                R.id.deliveryStatusLayout,
                R.id.exchangeCancelLayout,
                R.id.refundLayout -> { // 상태에 따른 리스트 보기
//                    val intent = OrderStatusHistoryActivity.newIntent(this, (v.tag as String).toInt())
//                    startActivityForResult(intent, RequestCodeData.STATUS_HISTORY)
                }
            }
        }
    }

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, MyShoppingActivity::class.java)
        }
    }
}
