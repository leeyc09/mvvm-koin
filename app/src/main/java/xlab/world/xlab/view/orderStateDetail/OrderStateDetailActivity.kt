package xlab.world.xlab.view.orderStateDetail

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.TextView
import kotlinx.android.synthetic.main.action_bar_default.*
import kotlinx.android.synthetic.main.activity_order_state_detail.*
import org.koin.android.architecture.ext.viewModel
import org.koin.android.ext.android.inject
import xlab.world.xlab.R
import xlab.world.xlab.adapter.recyclerView.GoodsOrderAdapter
import xlab.world.xlab.data.adapter.GoodsOrderListData
import xlab.world.xlab.utils.listener.DefaultListener
import xlab.world.xlab.utils.support.*
import xlab.world.xlab.utils.view.dialog.DefaultOneDialog
import xlab.world.xlab.utils.view.dialog.DefaultProgressDialog
import xlab.world.xlab.utils.view.dialog.DialogCreator
import xlab.world.xlab.utils.view.dialog.OrderStateDialog
import xlab.world.xlab.utils.view.recyclerView.CustomItemDecoration
import xlab.world.xlab.utils.view.toast.DefaultToast
import xlab.world.xlab.view.myShopping.MyShoppingViewModel

class OrderStateDetailActivity : AppCompatActivity(), View.OnClickListener {
    private val orderStateViewModel: OrderStateViewModel by viewModel()
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
                myShoppingViewModel.orderCancel(context = this@OrderStateDetailActivity,
                        authorization = spHelper.authorization, orderNo = tag)
        }
    }
    private val orderCancelListener = View.OnClickListener { view ->
        orderCancelDialog.showDialog(tag = view.tag as String)
    }
    private val moreListener = View.OnClickListener { view ->
        orderStateDialog.showDialog(goods = view.tag as GoodsOrderListData)
    }
    private val receiveConfirmListener = object : DefaultOneDialog.Listener {
        override fun onOkayTouch(tag: Any?) {
            if (tag is GoodsOrderListData)
                myShoppingViewModel.orderReceiveConfirm(context = this@OrderStateDetailActivity,
                        authorization = spHelper.authorization,
                        orderNo = tag.orderNo,
                        sno = tag.sno)
        }
    }
    private val buyDecideListener = object : DefaultOneDialog.Listener {
        override fun onOkayTouch(tag: Any?) {
            if (tag is GoodsOrderListData)
                myShoppingViewModel.buyDecide(context = this@OrderStateDetailActivity,
                        authorization = spHelper.authorization,
                        goods = tag)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_state_detail)

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

        orderStateViewModel.setResultCode(resultCode = resultCode)
        when (resultCode) {
            Activity.RESULT_OK -> {
                when (requestCode) {
                    RequestCodeData.ORDER_DETAIL -> { // 주문 상세
                        orderStateViewModel.loadOrderStateList(authorization = spHelper.authorization)
                    }
                    RequestCodeData.ORDER_REFUND, // 환불 신청
                    RequestCodeData.ORDER_RETURN, // 반품 신청
                    RequestCodeData.ORDER_CHANGE -> { // 교환 신청
                        orderStateViewModel.loadOrderStateList(authorization = spHelper.authorization)
                        orderStateDialog.dismiss()
                    }
                    RequestCodeData.GOODS_RATING -> {
                        orderStateDialog.dismiss()
                    }
                }
            }
            ResultCodeData.LOGOUT_SUCCESS -> { // logout -> finish activity
                actionBackBtn.performClick()
            }
        }
    }

    private fun onSetup() {
        // 액션버튼 비활성화
        actionBtn.visibility = View.GONE

        // appBarLayout 애니메이션 없애기
        appBarLayout.stateListAnimator = null

        // Toast, Dialog 초기화
        defaultToast = DefaultToast(context = this)
        progressDialog = DefaultProgressDialog(context = this)
        orderCancelDialog = DialogCreator.orderCancelDialog(context = this,
                listener = orderCancelDialogListener)
        orderStateDialog = OrderStateDialog(context = this,
                receiveConfirmListener = receiveConfirmListener,
                buyDecideListener = buyDecideListener)

        // Listener 초기화
        defaultListener = DefaultListener(context = this)

        // goods order adapter & recycler 초기화
        goodsOrderAdapter = GoodsOrderAdapter(context = this,
                orderDetailListener = defaultListener.orderDetailListener,
                deliverTrackingListener = defaultListener.deliverTrackingListener,
                orderCancelListener = orderCancelListener,
                moreListener = moreListener)
        recyclerView.adapter = goodsOrderAdapter
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recyclerView.addItemDecoration(CustomItemDecoration(context = this, bottom = 28f))
        recyclerView.isNestedScrollingEnabled = false

        orderStateViewModel.setState(context = this,
                state = intent.getIntExtra(IntentPassName.ORDER_STATE, AppConstants.ORDER_STATE_STAND_BY_PAYMENT))
        orderStateViewModel.loadOrderStateList(authorization = spHelper.authorization)
    }

    private fun onBindEvent() {
        actionBackBtn.setOnClickListener(this) // 뒤로가기
    }

    private fun observeViewModel() {
        // TODO: orderStateViewModel
        // UI 이벤트 observe
        orderStateViewModel.uiData.observe(this, android.arch.lifecycle.Observer { uiData ->
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
                uiData.orderStateStr?.let {
                    actionBarTitle.setText(it, TextView.BufferType.SPANNABLE)
                }
                uiData.orderGoodsData?.let {
                    goodsOrderAdapter.linkData(goodsOrderData = it)
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
            eventData?.let { isSuccess ->
                if (isSuccess) {
                    orderStateViewModel.setResultCode(resultCode = Activity.RESULT_OK)
                    orderStateViewModel.loadOrderStateList(authorization = spHelper.authorization)
                }
            }
        })

        // order receive confirm 이벤트 observe
        myShoppingViewModel.orderReceiveConfirmEventData.observe(owner = this, observer = android.arch.lifecycle.Observer { eventData ->
            eventData?.let { isSuccess ->
                if (isSuccess) {
                    orderStateViewModel.setResultCode(resultCode = Activity.RESULT_OK)
                    orderStateViewModel.loadOrderStateList(authorization = spHelper.authorization)
                }
            }
        })

        // buy decide confirm 이벤트 observe
        myShoppingViewModel.buyDecideEventData.observe(owner = this, observer = android.arch.lifecycle.Observer { eventData ->
            eventData?.let { goods ->
                orderStateViewModel.setResultCode(resultCode = Activity.RESULT_OK)
                orderStateViewModel.loadOrderStateList(authorization = spHelper.authorization)
                myShoppingViewModel.addUsedGoods(authorization = spHelper.authorization, goods = goods)
            }
        })
    }

    override fun onClick(v: View?) {
        v?.let {
            when (v.id) {
                R.id.actionBackBtn -> { // 뒤로가기
                    orderStateViewModel.backBtnAction()
                }
            }
        }
    }

    companion object {
        fun newIntent(context: Context, state: Int): Intent {
            val intent = Intent(context, OrderStateDetailActivity::class.java)
            intent.putExtra(IntentPassName.ORDER_STATE, state)

            return intent
        }
    }
}
