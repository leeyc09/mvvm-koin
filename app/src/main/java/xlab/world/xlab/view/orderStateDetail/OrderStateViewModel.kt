package xlab.world.xlab.view.orderStateDetail

import android.app.Activity
import android.arch.lifecycle.MutableLiveData
import android.content.Context
import xlab.world.xlab.R
import xlab.world.xlab.data.adapter.GoodsOrderData
import xlab.world.xlab.data.adapter.GoodsOrderListData
import xlab.world.xlab.server.provider.ApiGodoProvider
import xlab.world.xlab.utils.rx.SchedulerProvider
import xlab.world.xlab.utils.support.AppConstants
import xlab.world.xlab.utils.support.NetworkCheck
import xlab.world.xlab.utils.support.PrintLog
import xlab.world.xlab.view.AbstractViewModel

class OrderStateViewModel(private val apiGodo: ApiGodoProvider,
                          private val networkCheck: NetworkCheck,
                          private val scheduler: SchedulerProvider): AbstractViewModel() {

    private var resultCode = Activity.RESULT_CANCELED

    private var state: Int = 0
    private var orderGoodsData: GoodsOrderData = GoodsOrderData()

    val uiData = MutableLiveData<UIModel>()

    fun setResultCodeOK() {
        if (this.resultCode == Activity.RESULT_CANCELED)
            this.resultCode = Activity.RESULT_OK
    }

    fun setState(context: Context, state: Int) {
        this.state = state

        uiData.postValue(UIModel(orderStateStr = when (this.state) {
            AppConstants.ORDER_STATE_STAND_BY_PAYMENT -> context.getString(R.string.complete_payment)
            AppConstants.ORDER_STATE_STAND_DELIVERY -> context.getString(R.string.delivery_status)
            AppConstants.ORDER_STATE_STAND_CANCEL -> context.getString(R.string.exchange_cancel)
            AppConstants.ORDER_STATE_STAND_REFUND -> context.getString(R.string.refund)
            else -> ""
        }))
    }

    fun loadOrderStateList(authorization: String) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = networkCheck.networkErrorMsg))
            return
        }

        uiData.value = UIModel(isLoading = true)
        launch {
            apiGodo.requestOrderStateList(scheduler = scheduler, authorization = authorization, state = state,
                    responseData = {
                        PrintLog.d("requestOrderStateList success", it.toString())
                        val newOrderGoodsData = GoodsOrderData(total = it.total)
                        it.goodsList?.forEach { goods->
                            newOrderGoodsData.items.add(GoodsOrderListData(
                                    sno = goods.sno,
                                    orderNo = goods.orderNo,
                                    orderStatus = goods.orderState,
                                    invoiceNo = goods.invoiceNo,
                                    orderYear = goods.orderYear,
                                    orderMonth = goods.orderMonth,
                                    orderDay = goods.orderDay,
                                    code = goods.code,
                                    image = goods.image,
                                    brand = goods.brand,
                                    name = goods.name,
                                    option = null,
                                    count = goods.count,
                                    price = goods.price,
                                    userHandleSno = goods.userHandleSno,
                                    deliveryNo = goods.deliverySno
                            ))
                        }
                        this.orderGoodsData.updateData(goodsOrderData = newOrderGoodsData)

                        uiData.value = UIModel(isLoading = false, orderGoodsData = this.orderGoodsData)
                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(isLoading = false)
                        errorData?.let {
                            PrintLog.d("requestOrderStateList fail", errorData.message)
                        }
                    })
        }
    }

    fun backBtnAction() {
        uiData.postValue(UIModel(resultCode = resultCode))
    }
}

data class UIModel(val isLoading: Boolean? = null, val toastMessage: String? = null, val resultCode: Int? = null,
                   val orderStateStr: String? = null, val orderGoodsData: GoodsOrderData? = null)