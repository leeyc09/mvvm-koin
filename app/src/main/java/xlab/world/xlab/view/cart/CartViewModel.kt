package xlab.world.xlab.view.cart

import android.arch.lifecycle.MutableLiveData
import io.reactivex.Observable
import xlab.world.xlab.data.adapter.CartData
import xlab.world.xlab.data.adapter.CartListData
import xlab.world.xlab.server.provider.ApiGodoProvider
import xlab.world.xlab.utils.rx.SchedulerProvider
import xlab.world.xlab.utils.rx.with
import xlab.world.xlab.utils.support.NetworkCheck
import xlab.world.xlab.utils.support.PrintLog
import xlab.world.xlab.view.AbstractViewModel
import xlab.world.xlab.view.SingleLiveEvent

class CartViewModel(private val apiGodo: ApiGodoProvider,
                    private val networkCheck: NetworkCheck,
                    private val scheduler: SchedulerProvider): AbstractViewModel() {
    val tag = "Cart"
    private var cartTotal: Int = 0

    val loadCartEvent = SingleLiveEvent<CartEvent>()
    val deleteCartEvent = SingleLiveEvent<CartEvent>()
    val uiData = MutableLiveData<UIModel>()

    fun selectCartData(cartData: CartListData, selectCnt: Int) {
        launch {
            Observable.create<Int> {
                cartData.isSelect = !cartData.isSelect

                it.onNext(selectCnt + if (cartData.isSelect) 1 else -1)
                it.onComplete()
            }.with(scheduler).subscribe {
                uiData.value = UIModel(selectCnt = it.toString(), cartDataUpdate = true,
                        selectAll = it == cartTotal)
            }
        }
    }

    fun deleteCartData(authorization: String, sno: String) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = networkCheck.networkErrorMsg))
            return
        }

        uiData.value = UIModel(isLoading = true)
        launch {
            apiGodo.requestDeleteCart(scheduler = scheduler, authorization = authorization, sno = sno,
                    responseData = {
                        deleteCartEvent.value = CartEvent(status = true)
                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(isLoading = false)
                        errorData?.let {
                            PrintLog.d("requestDeleteCart fail", errorData.message, tag)
                        }
                    })
        }
    }

    fun loadCartData(authorization: String, page: Int) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = networkCheck.networkErrorMsg))
            return
        }

        uiData.value = UIModel(isLoading = true)
        loadCartEvent.value = CartEvent(status = true)
        launch {
            apiGodo.requestGetCart(scheduler = scheduler, authorization = authorization,
                    responseData = {
                        PrintLog.d("requestGetCart success", it.toString(), tag)
                        cartTotal = it.total
                        val cartData = CartData(total = it.total, nextPage = page + 1)
                        it.cartData?.forEach { data ->
                            val newData = CartListData(
                                    sno = data.sno,
                                    goodsCode = data.code,
                                    goodsImage = data.image,
                                    goodsBrand = data.brand,
                                    goodsName = data.name,
                                    goodsOption = null,
                                    goodsCnt = data.count,
                                    goodsPrice = data.price,
                                    goodsInitCnt = data.price,
                                    deliverySno = data.deliverySno,
                                    isSelect = true
                            )
                            cartData.items.add(newData)
                        }
                        uiData.value = UIModel(isLoading = false, cartData = cartData,
                                selectCnt = it.total.toString(), totalCnt = it.total.toString(), selectAll = true)
                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(isLoading = false)
                        errorData?.let {
                            PrintLog.d("requestGetCart fail", errorData.message, tag)
                        }
                    })
        }
    }
}

data class CartEvent(val status: Boolean? = null)
data class UIModel(val isLoading: Boolean? = null, val toastMessage: String? = null,
                   val cartData: CartData? = null,
                   val cartDataUpdate: Boolean? = null, val cartDataUpdateIndex: Int? = null,
                   val selectCnt: String? = null, val totalCnt: String? = null,
                   val selectAll: Boolean? = null)
