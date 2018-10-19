package xlab.world.xlab.view.myShopping

import android.arch.lifecycle.MutableLiveData
import android.content.Context
import io.reactivex.Observable
import xlab.world.xlab.R
import xlab.world.xlab.data.adapter.GoodsOrderData
import xlab.world.xlab.data.adapter.GoodsOrderListData
import xlab.world.xlab.data.request.ReqUsedGoodsData
import xlab.world.xlab.data.response.ResShopProfileData
import xlab.world.xlab.server.provider.ApiGodoProvider
import xlab.world.xlab.server.provider.ApiUserActivityProvider
import xlab.world.xlab.utils.rx.SchedulerProvider
import xlab.world.xlab.utils.rx.with
import xlab.world.xlab.utils.support.AppConstants
import xlab.world.xlab.utils.support.NetworkCheck
import xlab.world.xlab.utils.support.PrintLog
import xlab.world.xlab.view.AbstractViewModel
import xlab.world.xlab.view.SingleLiveEvent

class MyShoppingViewModel(private val apiGodo: ApiGodoProvider,
                          private val apiUserActivity: ApiUserActivityProvider,
                          private val networkCheck: NetworkCheck,
                          private val scheduler: SchedulerProvider): AbstractViewModel() {
    val tag = "MyShopping"
    private var goodsOrderData: GoodsOrderData = GoodsOrderData()

    private var initProfileData = ResShopProfileData()
    private var recentProfileData = ResShopProfileData()

    val updateProfileEventData = SingleLiveEvent<MyShopEvent>()
    val orderCancelEventData = SingleLiveEvent<MyShopEvent>()
    val orderReceiveConfirmEventData = SingleLiveEvent<MyShopEvent>()
    val buyDecideEventData = SingleLiveEvent<BuyDecideEvent>()
    val addUsedGoodsEventData = SingleLiveEvent<MyShopEvent>()
    val uiData = MutableLiveData<UIModel>()

    fun loadShopProfile(authorization: String) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = networkCheck.networkErrorMsg))
            return
        }

        uiData.value = UIModel(isLoading = true)
        launch {
            apiGodo.requestShopProfile(scheduler = scheduler, authorization = authorization,
                    responseData = {
                        PrintLog.d("requestShopProfile success", it.toString())
                        initProfileData.name = it.name
                        initProfileData.email = it.email
                        uiData.value = UIModel(isLoading = false,
                                shopName = it.name,
                                shopEmail = it.email)
                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(isLoading = false)
                        errorData?.let {
                            PrintLog.d("requestShopProfile fail", errorData.message)
                        }
                    })
        }
    }

    fun updateShopProfile(context: Context, authorization: String) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = networkCheck.networkErrorMsg))
            return
        }

        uiData.value = UIModel(isLoading = true)
        launch {
            apiGodo.requestUpdateShopProfile(scheduler = scheduler, authorization = authorization, name = recentProfileData.name, email = recentProfileData.email,
                    responseData = {
                        PrintLog.d("requestUpdateShopProfile success", it.toString())
                        uiData.value = UIModel(isLoading = false, toastMessage = context.getString(R.string.toast_shop_profile_update_success))
                        updateProfileEventData.value = MyShopEvent(status = true)
                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(isLoading = false)
                        errorData?.let {
                            PrintLog.d("requestUpdateShopProfile fail", errorData.message)
                        }
                    })
        }
    }

    fun loadOrderStateCnt(authorization: String) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = networkCheck.networkErrorMsg))
            return
        }

        uiData.value = UIModel(isLoading = true)
        launch {
            apiGodo.requestOrderStateCnt(scheduler = scheduler, authorization = authorization,
                    responseData = {
                        PrintLog.d("requestOrderStateCnt success", it.toString())
                        uiData.value = UIModel(isLoading = false,
                                completePaymentCnt = it.orderStateCnt[0].toString(), completePaymentEnable = it.orderStateCnt[0] > 0,
                                deliveryCnt = it.orderStateCnt[1].toString(), deliveryEnable = it.orderStateCnt[1] > 0,
                                cancelCnt = it.orderStateCnt[2].toString(), cancelEnable = it.orderStateCnt[2] > 0,
                                refundCnt = it.orderStateCnt[3].toString(), refundEnable = it.orderStateCnt[3] > 0)
                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(isLoading = false)
                        errorData?.let {
                            PrintLog.d("requestOrderStateCnt fail", errorData.message)
                        }
                    })
        }
    }

    fun loadOrderList(authorization: String) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = networkCheck.networkErrorMsg))
            return
        }

        uiData.value = UIModel(isLoading = true)
        launch {
            apiGodo.requestOrderList(scheduler = scheduler, authorization = authorization,
                    responseData = {
                        PrintLog.d("requestOrderList success", it.toString())
                        val newGoodsOrderData = GoodsOrderData(total = it.total)
                        it.goodsList?.forEach { goods->
                            newGoodsOrderData.items.add(GoodsOrderListData(
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
                        this.goodsOrderData.updateData(goodsOrderData = newGoodsOrderData)

                        uiData.value = UIModel(isLoading = false, goodsOrderData = this.goodsOrderData,
                                goodsOrderDataCnt = this.goodsOrderData.total.toString())
                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(isLoading = false)
                        errorData?.let {
                            PrintLog.d("requestOrderList fail", errorData.message)
                        }
                    })
        }
    }

    fun actionBackBtnAction() {
        launch {
            Observable.create<Boolean> {
                val result = initProfileData != recentProfileData

                it.onNext(result)
                it.onComplete()
            }.with(scheduler).subscribe {
                uiData.value = UIModel(cancelDialogShow = it)
            }
        }
    }

    // 변경된 데이터 있는지 확인
    fun enableSaveData(name: String? = null, email: String? = null) {
        launch {
            Observable.create<Boolean> {
                name?.let {_->
                    recentProfileData.name = name
                }
                email?.let {_->
                    recentProfileData.email = email
                }

                val result = initProfileData != recentProfileData

                it.onNext(result)
                it.onComplete()
            }.with(scheduler).subscribe {
                uiData.value = UIModel(actionBtnEnable = it)
            }
        }
    }

    fun orderCancel(context: Context, authorization: String, orderNo: String) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = networkCheck.networkErrorMsg))
            return
        }

        uiData.value = UIModel(isLoading = true)
        launch {
            apiGodo.requestOrderCancel(scheduler = scheduler, authorization = authorization, orderNo = orderNo,
                    responseData = {
                        uiData.value = UIModel(isLoading = false, toastMessage = context.getString(R.string.toast_order_cancel_success))
                        orderCancelEventData.value = MyShopEvent(status = true)
                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(isLoading = false)
                        errorData?.let {
                            PrintLog.d("requestOrderCancel fail", errorData.message)
                        }
                    })
        }
    }

    fun orderReceiveConfirm(context: Context, authorization: String, orderNo: String, sno: String) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = networkCheck.networkErrorMsg))
            return
        }

        uiData.value = UIModel(isLoading = true)
        launch {
            apiGodo.requestOrderReceiveConfirm(scheduler = scheduler, authorization = authorization, orderNo = orderNo, sno = sno,
                    responseData = {
                        uiData.value = UIModel(isLoading = false, toastMessage = context.getString(R.string.toast_receive_confirm_success))
                        orderReceiveConfirmEventData.value = MyShopEvent(status = true)
                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(isLoading = false)
                        errorData?.let {
                            PrintLog.d("requestOrderReceiveConfirm fail", errorData.message)
                        }
                    })
        }
    }

    fun buyDecide(context: Context, authorization: String, goods: GoodsOrderListData) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = networkCheck.networkErrorMsg))
            return
        }

        uiData.value = UIModel(isLoading = true)
        launch {
            apiGodo.requestBuyDecide(scheduler = scheduler, authorization = authorization, orderNo = goods.orderNo, sno = goods.sno,
                    responseData = {
                        uiData.value = UIModel(isLoading = false, toastMessage = context.getString(R.string.toast_buy_decide_success))
                        buyDecideEventData.value = BuyDecideEvent(goods = goods)
                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(isLoading = false)
                        errorData?.let {
                            PrintLog.d("requestBuyDecide fail", errorData.message)
                        }
                    })
        }
    }

    fun addUsedGoods(authorization: String, goods: GoodsOrderListData) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = networkCheck.networkErrorMsg))
            return
        }

        launch {
            val reqUsedGoodsData = ReqUsedGoodsData(
                    usedType = AppConstants.FROM_BUY,
                    goodsCode = goods.code,
                    goodsName = goods.name,
                    goodsBrand = goods.brand,
                    goodsImage = goods.image,
                    goodsType = AppConstants.GOODS_PET,
                    topic = ReqUsedGoodsData.Topic())
            apiUserActivity.requestPostUsedGoods(scheduler = scheduler, authorization = authorization, reqUsedGoodsData = reqUsedGoodsData,
                    responseData = {
                    },
                    errorData = { errorData ->
                        errorData?.let {
                            PrintLog.d("requestPostUsedGoods fail", errorData.message)
                        }
                    })
        }
    }
}

data class BuyDecideEvent(val goods: GoodsOrderListData? = null)
data class MyShopEvent(val status: Boolean? = null)
data class UIModel(val isLoading: Boolean? = null, val toastMessage: String? = null,
                   val actionBtnEnable: Boolean? = null, val cancelDialogShow: Boolean? = null,
                   val shopName: String? = null, val shopEmail: String? = null,
                   val completePaymentCnt: String? = null, val completePaymentEnable: Boolean? = null,
                   val deliveryCnt: String? = null, val deliveryEnable: Boolean? = null,
                   val cancelCnt: String? = null, val cancelEnable: Boolean? = null,
                   val refundCnt: String? = null, val refundEnable: Boolean? = null,
                   val goodsOrderData: GoodsOrderData? = null, val goodsOrderDataCnt: String? = null)
