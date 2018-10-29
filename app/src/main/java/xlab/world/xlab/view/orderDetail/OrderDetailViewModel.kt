package xlab.world.xlab.view.orderDetail

import android.app.Activity
import android.arch.lifecycle.MutableLiveData
import android.view.View
import xlab.world.xlab.data.adapter.GoodsOrderData
import xlab.world.xlab.data.adapter.GoodsOrderListData
import xlab.world.xlab.server.provider.ApiGodoProvider
import xlab.world.xlab.utils.rx.SchedulerProvider
import xlab.world.xlab.utils.support.GodoData
import xlab.world.xlab.utils.support.NetworkCheck
import xlab.world.xlab.utils.support.PrintLog
import xlab.world.xlab.utils.support.SupportData
import xlab.world.xlab.view.AbstractViewModel

class OrderDetailViewModel(private val apiGodo: ApiGodoProvider,
                           private val networkCheck: NetworkCheck,
                           private val scheduler: SchedulerProvider): AbstractViewModel() {
    private val viewModelTag = "OrderDetail"

    private var resultCode = Activity.RESULT_CANCELED

    private var orderGoodsData: GoodsOrderData = GoodsOrderData()

    val uiData = MutableLiveData<UIModel>()

    fun setResultCode(resultCode: Int) {
        this.resultCode = SupportData.setResultCode(oldResultCode = this.resultCode, newResultCode = resultCode)
    }

    fun backBtnAction() {
        uiData.postValue(UIModel(resultCode = resultCode))
    }

    fun loadOrderDetail(authorization: String, orderNo: String) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = networkCheck.networkErrorMsg))
            return
        }

        uiData.value = UIModel(isLoading = true)
        launch {
            apiGodo.requestOrderDetail(scheduler = scheduler, authorization = authorization, orderNo = orderNo,
                    responseData = {
                        PrintLog.d("requestOrderList success", it.toString(), viewModelTag)
                        val newOrderGoodsData = GoodsOrderData(total = it.total)
                        // 주문 상품 목록
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

                        // 결제 정보
                        var depositPrice: String? = null // 입금 금액
                        var depositPriceVisibility: Int = View.GONE // 입금 금액 보이기 & 숨기기
                        var depositBank: String? = null // 입금 은행
                        var depositBankVisibility: Int = View.GONE // 입금 은행 보이기 & 숨기기
                        var depositAccount: String? = null // 입금 계좌
                        var depositAccountVisibility: Int = View.GONE // 입금 계좌 보이기 & 숨기기
                        var accountHolder: String? = null // 예금주
                        var accountHolderVisibility: Int = View.GONE // 예금주 보이기 & 숨기기
                        var depositName: String? = null // 입금자명
                        var depositNameVisibility: Int = View.GONE // 입금자명 보이기 & 숨기기
                        it.payment.bankInfo?.let { bankInfo ->
                            if (bankInfo.isNotEmpty()) {
                                depositPriceVisibility = View.VISIBLE
                                depositPrice = SupportData.applyPriceFormat(price = it.price.settle)
                                if (bankInfo.size > 0) {
                                    depositBank = bankInfo[0]
                                    depositBankVisibility = View.VISIBLE
                                }
                                if (bankInfo.size > 1) {
                                    depositAccount = bankInfo[1]
                                    depositAccountVisibility = View.VISIBLE
                                }
                                if (bankInfo.size > 2) {
                                    accountHolder = bankInfo[2]
                                    accountHolderVisibility = View.VISIBLE
                                }
                                if (it.payment.name.isNotEmpty()) {
                                    depositName = it.payment.name
                                    depositNameVisibility = View.VISIBLE
                                }
                            }
                        }

                        uiData.value = UIModel(isLoading = false, orderGoodsData = this.orderGoodsData,
                                receiverName = it.receiver.name, receiverPhoneNum = it.receiver.phone,
                                receiverZoneCode = it.receiver.zoneCode, receiverAddress = "${it.receiver.address} ${it.receiver.subAddress}",
                                receiverMemo = if (it.receiver.memo.isNotEmpty()) it.receiver.memo else null,
                                receiverMemoVisibility = if (it.receiver.memo.isNotEmpty()) View.VISIBLE else View.GONE,
                                paymentType = GodoData.paymentTypeMap[it.payment.type],
                                depositPrice = depositPrice, depositPriceVisibility = depositPriceVisibility,
                                depositBank = depositBank, depositBankVisibility = depositBankVisibility,
                                depositAccount = depositAccount, depositAccountVisibility = depositAccountVisibility,
                                accountHolder = accountHolder, accountHolderVisibility = accountHolderVisibility,
                                depositName = depositName, depositNameVisibility = depositNameVisibility,
                                totalGoodsPrice = SupportData.applyPriceFormat(price = it.price.total),
                                totalDeliveryPrice = SupportData.applyPriceFormat(price = it.price.delivery),
                                totalPaymentPrice = SupportData.applyPriceFormat(price = it.price.settle))
                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(isLoading = false)
                        errorData?.let {
                            PrintLog.e("requestOrderList fail", errorData.message, viewModelTag)
                        }
                    })
        }
    }
}

data class UIModel(val isLoading: Boolean? = null, val toastMessage: String? = null, val resultCode: Int? = null,
                   val orderGoodsData: GoodsOrderData? = null,
                   val receiverName: String? = null, val receiverPhoneNum: String? = null,
                   val receiverZoneCode: String? = null, val receiverAddress: String? = null,
                   val receiverMemo: String? = null, val receiverMemoVisibility: Int? = null,
                   val paymentType: String? = null,
                   val depositPrice: String? = null, val depositPriceVisibility: Int? = null,
                   val depositBank: String? = null, val depositBankVisibility: Int? = null,
                   val depositAccount: String? = null, val depositAccountVisibility: Int? = null,
                   val accountHolder: String? = null, val accountHolderVisibility: Int? = null,
                   val depositName: String? = null, val depositNameVisibility: Int? = null,
                   val totalGoodsPrice: String? = null, val totalDeliveryPrice: String? = null, val totalPaymentPrice: String? = null)
