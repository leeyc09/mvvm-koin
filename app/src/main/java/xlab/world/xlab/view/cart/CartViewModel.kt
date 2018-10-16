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
import xlab.world.xlab.utils.support.SupportData
import xlab.world.xlab.view.AbstractViewModel
import xlab.world.xlab.view.SingleLiveEvent

class CartViewModel(private val apiGodo: ApiGodoProvider,
                    private val networkCheck: NetworkCheck,
                    private val scheduler: SchedulerProvider): AbstractViewModel() {
    val tag = "Cart"
    private var cartData: CartData = CartData()

    val loadCartEvent = SingleLiveEvent<CartEvent>()
    val uiData = MutableLiveData<UIModel>()

    fun selectAllCartData(isSelectAll: Boolean) {
        launch {
            Observable.create<Boolean> {
                // 전체 상품 선택&해제
                cartData.items.forEach { data ->
                    data.isSelect = !isSelectAll
                }

                it.onNext(!isSelectAll)
                it.onComplete()
            }.with(scheduler).subscribe {
                uiData.value = UIModel(selectCnt = (if (it) cartData.total else 0).toString(), cartDataUpdate = true,
                        selectAll = it)
            }
        }
    }

    fun selectCartData(cartListData: CartListData) {
        launch {
            Observable.create<Int> {
                cartListData.isSelect = !cartListData.isSelect
                // 선택 된 상품 갯수 계산
                var selectCnt = 0
                cartData.items.forEach { data ->
                    if (data.isSelect) selectCnt++
                }

                it.onNext(selectCnt)
                it.onComplete()
            }.with(scheduler).subscribe {
                uiData.value = UIModel(selectCnt = it.toString(), cartDataUpdate = true,
                        selectAll = it == cartData.total)
            }
        }
    }

    fun cartGoodsCntPlus(authorization: String, cartListData: CartListData) {
        updateCartData(authorization = authorization, cartListData = cartListData, cnt = cartListData.goodsCnt + 1)
    }

    fun cartGoodsCntMinus(authorization: String, cartListData: CartListData) {
        if (cartListData.goodsCnt > 1)
            updateCartData(authorization = authorization, cartListData = cartListData, cnt = cartListData.goodsCnt - 1)
    }

    private fun updateCartData(authorization: String, cartListData: CartListData, cnt: Int) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = networkCheck.networkErrorMsg))
            return
        }

        uiData.value = UIModel(isLoading = true)
        launch {
            apiGodo.requestUpdateCart(scheduler = scheduler, authorization = authorization, sno = cartListData.sno, count = cnt,
                    responseData = {
                        cartListData.goodsCnt = cnt
                        PrintLog.d("viewModel cartData", cartData.toString(), tag)
                        uiData.value = UIModel(isLoading = false, cartDataUpdate = true)
                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(isLoading = false)
                        errorData?.let {
                            PrintLog.d("requestUpdateCart fail", errorData.message, tag)
                        }
                    })
        }
    }

    fun deleteCartData(authorization: String, cartListData: CartListData) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = networkCheck.networkErrorMsg))
            return
        }

        uiData.value = UIModel(isLoading = true)
        launch {
            apiGodo.requestDeleteCart(scheduler = scheduler, authorization = authorization, sno = cartListData.sno,
                    responseData = {
                        cartData.removeData(cartListData = cartListData)
                        PrintLog.d("viewModel cartData", cartData.toString(), tag)
                        // 선택 된 상품 갯수 계산
                        var selectCnt = 0
                        cartData.items.forEach { data ->
                            if (data.isSelect) selectCnt++
                        }

                        uiData.value = UIModel(isLoading = false, cartDataUpdate = true,
                                selectCnt = selectCnt.toString(), totalCnt = cartData.total.toString(), selectAll = selectCnt == cartData.total)
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
                        val newCartData = CartData(total = it.total, nextPage = page + 1)
                        var selectAll = true
                        it.cartData?.forEachIndexed { index, data ->
                            val isSelect =
                                    if (this.cartData.items.isEmpty()) true
                                    else this.cartData.items[index].isSelect
                            if (selectAll) selectAll = isSelect

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
                                    isSelect = isSelect
                            )
                            newCartData.items.add(newData)
                        }
                        if (page == 1) // 요청한 page => 첫페이지
                            this.cartData.updateData(cartData = newCartData)
                        else
                            this.cartData.addData(cartData = newCartData)

                        uiData.value = UIModel(isLoading = false, cartData = this.cartData,
                                selectCnt = it.total.toString(), totalCnt = it.total.toString(), selectAll = selectAll,
                                cartDataUpdate = true)
                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(isLoading = false)
                        errorData?.let {
                            PrintLog.d("requestGetCart fail", errorData.message, tag)
                        }
                    })
        }
    }

    fun setTotalPrice() {
        launch {
            Observable.create<ArrayList<String>> {
                val priceList = ArrayList<String>()

                var totalGoodsPrice = 0
                val selectedDeliveryType = HashMap<String, Int>()
                cartData.items.forEach { item ->
                    if (item.isSelect) {
                        totalGoodsPrice += item.goodsCnt * item.goodsPrice
                        selectedDeliveryType[item.deliverySno] = 1
                    }
                }
                val totalDeliveryPrice = selectedDeliveryType.size * 2500

                priceList.add(SupportData.applyPriceFormat(totalGoodsPrice)) // 총 제품 금액
                priceList.add(SupportData.applyPriceFormat(totalDeliveryPrice)) // 총 배송비
                priceList.add(SupportData.applyPriceFormat(totalGoodsPrice + totalDeliveryPrice)) // 결제금액

                it.onNext(priceList)
                it.onComplete()
            }.with(scheduler).subscribe {
                if (it.size > 2)
                    uiData.value = UIModel(totalGoodsPrice = it[0], totalDeliveryPrice = it[1],
                            paymentPrice = it[2])
            }
        }
    }
}

data class CartEvent(val status: Boolean? = null)
data class UIModel(val isLoading: Boolean? = null, val toastMessage: String? = null,
                   val cartData: CartData? = null,
                   val cartDataUpdate: Boolean? = null, val cartDataUpdateIndex: Int? = null,
                   val selectCnt: String? = null, val totalCnt: String? = null,
                   val selectAll: Boolean? = null,
                   val totalGoodsPrice: String? = null, val totalDeliveryPrice: String? = null,
                   val paymentPrice: String? = null)
