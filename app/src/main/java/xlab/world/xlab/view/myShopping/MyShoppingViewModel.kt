package xlab.world.xlab.view.myShopping

import android.arch.lifecycle.MutableLiveData
import android.content.Context
import io.reactivex.Observable
import xlab.world.xlab.R
import xlab.world.xlab.data.response.ResShopProfileData
import xlab.world.xlab.server.provider.ApiGodoProvider
import xlab.world.xlab.utils.rx.SchedulerProvider
import xlab.world.xlab.utils.rx.with
import xlab.world.xlab.utils.support.NetworkCheck
import xlab.world.xlab.utils.support.PrintLog
import xlab.world.xlab.view.AbstractViewModel
import xlab.world.xlab.view.SingleLiveEvent

class MyShoppingViewModel(private val apiGodo: ApiGodoProvider,
                          private val networkCheck: NetworkCheck,
                          private val scheduler: SchedulerProvider): AbstractViewModel() {
    val tag = "MyShopping"

    private var initProfileData = ResShopProfileData()
    private var recentProfileData = ResShopProfileData()

    val updateProfileEventData = SingleLiveEvent<MyShopEvent>()
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
                        PrintLog.d("requestShopProfile success", it.toString(), tag)
                        initProfileData.name = it.name
                        initProfileData.email = it.email
                        uiData.value = UIModel(isLoading = false,
                                shopName = it.name,
                                shopEmail = it.email)
                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(isLoading = false)
                        errorData?.let {
                            PrintLog.d("requestShopProfile fail", errorData.message, tag)
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
                        PrintLog.d("requestUpdateShopProfile success", it.toString(), tag)
                        uiData.value = UIModel(isLoading = false, toastMessage = context.getString(R.string.toast_shop_profile_update_success))
                        updateProfileEventData.value = MyShopEvent(status = true)
                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(isLoading = false)
                        errorData?.let {
                            PrintLog.d("requestUpdateShopProfile fail", errorData.message, tag)
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
            apiGodo.requestOrderStatusCnt(scheduler = scheduler, authorization = authorization,
                    responseData = {
                        PrintLog.d("requestOrderStatusCnt success", it.toString(), tag)
                        uiData.value = UIModel(isLoading = false,
                                completePaymentCnt = it.orderStatusCnt[0].toString(), completePaymentEnable = it.orderStatusCnt[0] > 0,
                                deliveryCnt = it.orderStatusCnt[1].toString(), deliveryEnable = it.orderStatusCnt[1] > 0,
                                cancelCnt = it.orderStatusCnt[2].toString(), cancelEnable = it.orderStatusCnt[2] > 0,
                                refundCnt = it.orderStatusCnt[3].toString(), refundEnable = it.orderStatusCnt[3] > 0)
                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(isLoading = false)
                        errorData?.let {
                            PrintLog.d("requestOrderStatusCnt fail", errorData.message, tag)
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
}

data class MyShopEvent(val status: Boolean? = null)
data class UIModel(val isLoading: Boolean? = null, val toastMessage: String? = null,
                   val actionBtnEnable: Boolean? = null, val cancelDialogShow: Boolean? = null,
                   val shopName: String? = null, val shopEmail: String? = null,
                   val completePaymentCnt: String? = null, val completePaymentEnable: Boolean? = null,
                   val deliveryCnt: String? = null, val deliveryEnable: Boolean? = null,
                   val cancelCnt: String? = null, val cancelEnable: Boolean? = null,
                   val refundCnt: String? = null, val refundEnable: Boolean? = null)
