package xlab.world.xlab.view.orderDetail

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.TextView
import kotlinx.android.synthetic.main.action_bar_default.*
import kotlinx.android.synthetic.main.activity_order_detail.*
import org.koin.android.architecture.ext.viewModel
import org.koin.android.ext.android.inject
import xlab.world.xlab.R
import xlab.world.xlab.adapter.recyclerView.GoodsOrderAdapter
import xlab.world.xlab.data.adapter.GoodsOrderListData
import xlab.world.xlab.utils.listener.DefaultListener
import xlab.world.xlab.utils.support.IntentPassName
import xlab.world.xlab.utils.support.PrintLog
import xlab.world.xlab.utils.support.RequestCodeData
import xlab.world.xlab.utils.support.SPHelper
import xlab.world.xlab.utils.view.dialog.DefaultOneDialog
import xlab.world.xlab.utils.view.dialog.DefaultProgressDialog
import xlab.world.xlab.utils.view.dialog.DialogCreator
import xlab.world.xlab.utils.view.dialog.OrderStateDialog
import xlab.world.xlab.utils.view.recyclerView.CustomItemDecoration
import xlab.world.xlab.utils.view.toast.DefaultToast
import xlab.world.xlab.view.myShopping.MyShoppingViewModel

class OrderDetailActivity : AppCompatActivity(), View.OnClickListener {
    private val orderDetailViewModel: OrderDetailViewModel by viewModel()
    private val myShoppingViewModel: MyShoppingViewModel by viewModel()
    private val spHelper: SPHelper by inject()

    private lateinit var defaultToast: DefaultToast
    private lateinit var progressDialog: DefaultProgressDialog
    private lateinit var orderCancelDialog: DefaultOneDialog
    private lateinit var orderStateDialog: OrderStateDialog

    private lateinit var goodsOrderAdapter: GoodsOrderAdapter

    private lateinit var defaultListener: DefaultListener
    private val orderCancelDialogListener = object : DefaultOneDialog.Listener {
        override fun onOkayTouch(tag: Any?) {
            if (tag is String)
                myShoppingViewModel.orderCancel(context = this@OrderDetailActivity,
                        authorization = spHelper.authorization, orderNo = tag)
        }
    }
    private val orderCancelListener = View.OnClickListener { view ->
        orderCancelDialog.setTag(tag = view.tag as String)
        orderCancelDialog.show()
    }
    private val moreListener = View.OnClickListener { view ->
        orderStateDialog.showDialog(goods = view.tag as GoodsOrderListData)
    }
    private val receiveConfirmListener = object : DefaultOneDialog.Listener {
        override fun onOkayTouch(tag: Any?) {
            if (tag is GoodsOrderListData)
                myShoppingViewModel.orderReceiveConfirm(context = this@OrderDetailActivity,
                        authorization = spHelper.authorization,
                        orderNo = tag.orderNo,
                        sno = tag.sno)
        }
    }
    private val buyDecideListener = object : DefaultOneDialog.Listener {
        override fun onOkayTouch(tag: Any?) {
            if (tag is GoodsOrderListData)
                myShoppingViewModel.buyDecide(context = this@OrderDetailActivity,
                        authorization = spHelper.authorization,
                        orderNo = tag.orderNo,
                        sno = tag.sno)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_detail)

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
                orderDetailViewModel.setResultCodeOK()
                when (requestCode) {
                    RequestCodeData.ORDER_REFUND, // 환불 신청
                    RequestCodeData.ORDER_RETURN, // 반품 신청
                    RequestCodeData.ORDER_CHANGE -> { // 교환 신청
                        orderDetailViewModel.loadOrderDetail(authorization = spHelper.authorization,
                                orderNo = intent.getStringExtra(IntentPassName.ORDER_NO))
                        orderStateDialog.dismiss()
                    }
                    RequestCodeData.GOODS_RATING -> {
//                        defaultToast.showToast(resources.getString(R.string.rating_success))
//                        orderStateDialog.dismiss()
                    }
                }
            }
        }
    }

    private fun onSetup() {
        actionBarTitle.setText(resources.getText(R.string.order_list), TextView.BufferType.SPANNABLE)
        actionBtn.visibility = View.GONE

        defaultToast = DefaultToast(context = this)
        progressDialog = DefaultProgressDialog(context = this)
        orderCancelDialog = DialogCreator.orderCancelDialog(context = this,
                listener = orderCancelDialogListener)
        orderStateDialog = OrderStateDialog(context = this,
                receiveConfirmListener = receiveConfirmListener,
                buyDecideListener = buyDecideListener)

        defaultListener = DefaultListener(context = this)

        // goods order adapter & recycler 초기화
        goodsOrderAdapter = GoodsOrderAdapter(context = this,
                orderDetailListener = null,
                deliverTrackingListener = defaultListener.deliverTrackingListener,
                orderCancelListener = orderCancelListener,
                moreListener = moreListener)
        recyclerView.adapter = goodsOrderAdapter
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recyclerView.addItemDecoration(CustomItemDecoration(context = this, bottom = 28f))
        recyclerView.isNestedScrollingEnabled = false

        orderDetailViewModel.loadOrderDetail(authorization = spHelper.authorization,
                orderNo = intent.getStringExtra(IntentPassName.ORDER_NO))
    }

    private fun onBindEvent() {
        actionBackBtn.setOnClickListener(this) // 뒤로가기
    }

    private fun observeViewModel() {
        // TODO: orderDetailViewModel
        // UI 이벤트 observe
        orderDetailViewModel.uiData.observe(this, android.arch.lifecycle.Observer { uiData ->
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
                uiData.orderGoodsData?.let {
                    goodsOrderAdapter.linkData(goodsOrderData = it)
                }
                uiData.receiverName?.let {
                    textViewReceiverName.setText(it, TextView.BufferType.SPANNABLE)
                }
                uiData.receiverPhoneNum?.let {
                    textViewReceiverPhone.setText(it, TextView.BufferType.SPANNABLE)
                }
                uiData.receiverZoneCode?.let {
                    textViewReceiverZoneCode.setText(it, TextView.BufferType.SPANNABLE)
                }
                uiData.receiverAddress?.let {
                    textViewReceiverAddress.setText(it, TextView.BufferType.SPANNABLE)
                }
                uiData.receiverMemo?.let {
                    textViewReceiverMemo.setText(it, TextView.BufferType.SPANNABLE)
                }
                uiData.receiverMemoVisibility?.let {
                    receiverMemoLayout.visibility = it
                }
                uiData.paymentType?.let {
                    textViewPaymentType.setText(it, TextView.BufferType.SPANNABLE)
                }
                uiData.depositPrice?.let {
                    textViewDepositPrice.setText(it, TextView.BufferType.SPANNABLE)
                }
                uiData.depositPriceVisibility?.let {
                    depositPriceLayout.visibility = it
                }
                uiData.depositBank?.let {
                    textViewDepositBank.setText(it, TextView.BufferType.SPANNABLE)
                }
                uiData.depositBankVisibility?.let {
                    depositBankLayout.visibility = it
                }
                uiData.depositAccount?.let {
                    textViewDepositAccount.setText(it, TextView.BufferType.SPANNABLE)
                }
                uiData.depositAccountVisibility?.let {
                    depositAccountLayout.visibility = it
                }
                uiData.accountHolder?.let {
                    textViewAccountHolder.setText(it, TextView.BufferType.SPANNABLE)
                }
                uiData.accountHolderVisibility?.let {
                    depositAccountHolderLayout.visibility = it
                }
                uiData.depositName?.let {
                    textViewDepositName.setText(it, TextView.BufferType.SPANNABLE)
                }
                uiData.depositNameVisibility?.let {
                    depositNameLayout.visibility = it
                }
                uiData.totalGoodsPrice?.let {
                    textViewTotalGoodsPrice.setText(it, TextView.BufferType.SPANNABLE)
                }
                uiData.totalDeliveryPrice?.let {
                    textViewTotalDeliveryPrice.setText(it, TextView.BufferType.SPANNABLE)
                }
                uiData.totalPaymentPrice?.let {
                    textViewTotalPaymentPrice.setText(it, TextView.BufferType.SPANNABLE)
                }
            }
        })

        // TODO: myShoppingViewModel
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
            }
        })

        // order cancel 이벤트 observe
        myShoppingViewModel.orderCancelEventData.observe(owner = this, observer = android.arch.lifecycle.Observer { eventData ->
            eventData?.let { _ ->
                eventData.status?.let { isSuccess ->
                    if (isSuccess) {
                        orderDetailViewModel.setResultCodeOK()
                        orderDetailViewModel.loadOrderDetail(authorization = spHelper.authorization,
                                orderNo = intent.getStringExtra(IntentPassName.ORDER_NO))
                    }
                }
            }
        })

        // order receive confirm 이벤트 observe
        myShoppingViewModel.orderReceiveConfirmEventData.observe(owner = this, observer = android.arch.lifecycle.Observer { eventData ->
            eventData?.let { _ ->
                eventData.status?.let { isSuccess ->
                    if (isSuccess) {
                        orderDetailViewModel.setResultCodeOK()
                        orderDetailViewModel.loadOrderDetail(authorization = spHelper.authorization,
                                orderNo = intent.getStringExtra(IntentPassName.ORDER_NO))
                    }
                }
            }
        })

        // buy decide confirm 이벤트 observe
        myShoppingViewModel.buyDecideEventData.observe(owner = this, observer = android.arch.lifecycle.Observer { eventData ->
            eventData?.let { _ ->
                eventData.status?.let { isSuccess ->
                    if (isSuccess) {
                        orderDetailViewModel.setResultCodeOK()
                        orderDetailViewModel.loadOrderDetail(authorization = spHelper.authorization,
                                orderNo = intent.getStringExtra(IntentPassName.ORDER_NO))
                    }
                }
            }
        })
    }

    override fun onClick(v: View?) {
        v?.let {
            when (v.id) {
                R.id.actionBackBtn -> { // 뒤로가기
                    orderDetailViewModel.actionBackAction()
                }
            }
        }
    }

    companion object {
        fun newIntent(context: Context, orderNo: String): Intent {
            val intent = Intent(context, OrderDetailActivity::class.java)
            intent.putExtra(IntentPassName.ORDER_NO, orderNo)

            return intent
        }
    }
}
