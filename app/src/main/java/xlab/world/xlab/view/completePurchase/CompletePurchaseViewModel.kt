package xlab.world.xlab.view.completePurchase

import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.support.v4.content.res.ResourcesCompat
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.View
import xlab.world.xlab.R
import xlab.world.xlab.server.provider.ApiGodoProvider
import xlab.world.xlab.utils.rx.SchedulerProvider
import xlab.world.xlab.utils.support.GodoData
import xlab.world.xlab.utils.support.NetworkCheck
import xlab.world.xlab.utils.support.PrintLog
import xlab.world.xlab.utils.support.SupportData
import xlab.world.xlab.view.AbstractViewModel

class CompletePurchaseViewModel(private val apiGodo: ApiGodoProvider,
                                private val networkCheck: NetworkCheck,
                                private val scheduler: SchedulerProvider): AbstractViewModel() {
    private val viewModelTag = "CompletePurchase"

    val uiData = MutableLiveData<UIModel>()

    fun loadCompleteOrderData(context: Context, authorization: String, orderNo: String) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = networkCheck.networkErrorMsg))
            return
        }

        uiData.value = UIModel(isLoading = true)
        launch {
            apiGodo.requestOrderDetail(scheduler = scheduler, authorization = authorization, orderNo = orderNo,
                    responseData = {
                        PrintLog.d("requestOrderDetail success", it.toString(), viewModelTag)
                        // 결제 방식에 따른 타이틀 & 결제 가격 화면 설정
                        var titleStr: SpannableString? = null // 타이틀 이름
                        var subTitleVisibility: Int? = null // 서브 타이틀 보이기 & 숨기기
                        var paymentPriceVisibility: Int? = null // 결제 가격 보이기 & 숨기기
                        var paymentPrice: String? = null // 결제 가격
                        when (it.payment.type) {
                            "gb", "fa", // 무통장 입금
                            "pv", "fv", "ev" -> {  // 가상계좌
                                titleStr = SpannableString(context.getString(R.string.complete_order_title))
                                subTitleVisibility = View.VISIBLE
                                paymentPriceVisibility = View.GONE
                            }
                            "pc", "fc", "ec",// 카드결제
                            "pb", "fb", "eb" -> {  // 계좌이체
                                titleStr = SpannableString(context.getString(R.string.complete_payment_title))
                                subTitleVisibility = View.GONE
                                paymentPriceVisibility = View.VISIBLE
                                paymentPrice = SupportData.applyPriceFormat(price = it.price.settle)
                            }
                        }
                        titleStr?.let {_->
                            val blackColor = ResourcesCompat.getColor(context.resources, R.color.color000000, null)
                            val purpleColor = ResourcesCompat.getColor(context.resources, R.color.color5300ED, null)
                            titleStr.setSpan(ForegroundColorSpan(blackColor), 0, 3, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                            titleStr.setSpan(ForegroundColorSpan(purpleColor), 3, 6, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                            titleStr.setSpan(ForegroundColorSpan(blackColor), 6, titleStr.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                        }

                        // 입금 은행 정보
                        var bankInfoVisibility: Int = View.GONE // 입금 은행 정보 보이기 & 숨기기
                        var depositPrice: String? = null // 입금 금액
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
                                bankInfoVisibility = View.VISIBLE
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
                        // 상품 이름, 배송지, 결제 방식
                        var orderGoodsName: String? = null // 상품 이름
                        it.goodsList?.let { goodsList ->
                            val orderFirstGoods = goodsList.first()
                            orderGoodsName = orderFirstGoods.name +
                                    if (goodsList.size > 1) "외 ${goodsList.size - 1}건"
                                    else ""
                        }

                        uiData.value = UIModel(isLoading = false,
                                titleStr = titleStr, subTitleVisibility = subTitleVisibility,
                                paymentPriceVisibility = paymentPriceVisibility, paymentPrice = paymentPrice,
                                bankInfoVisibility = bankInfoVisibility, depositPrice = depositPrice,
                                depositBank = depositBank, depositBankVisibility = depositBankVisibility,
                                depositAccount = depositAccount, depositAccountVisibility = depositAccountVisibility,
                                accountHolder = accountHolder, accountHolderVisibility = accountHolderVisibility,
                                depositName = depositName, depositNameVisibility = depositNameVisibility,
                                orderNo = orderNo, orderGoodsName = orderGoodsName, receiverName = it.receiver.name,
                                receiverPhone = it.receiver.phone, receiverZoneCode = it.receiver.zoneCode,
                                receiverAddress = it.receiver.address, receiverSubAddress = it.receiver.subAddress,
                                receiverSubAddressVisibility = if (it.receiver.subAddress.isEmpty()) View.GONE else View.VISIBLE,
                                paymentType = GodoData.paymentTypeMap[it.payment.type])
                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(isLoading = false)
                        errorData?.let {
                            PrintLog.e("requestOrderDetail fail", errorData.message, viewModelTag)
                        }
                    })
        }
    }
}

data class UIModel(val isLoading: Boolean? = null, val toastMessage: String? = null,
                   val titleStr: SpannableString? = null,
                   val subTitleVisibility: Int? = null, val paymentPriceVisibility: Int? = null,
                   val paymentPrice: String? = null,
                   val bankInfoVisibility: Int? = null, val depositPrice: String? = null,
                   val depositBank: String? = null, val depositBankVisibility: Int? = null,
                   val depositAccount: String? = null, val depositAccountVisibility: Int? = null,
                   val accountHolder: String? = null, val accountHolderVisibility: Int? = null,
                   val depositName: String? = null, val depositNameVisibility: Int? = null,
                   val orderNo: String? = null, val orderGoodsName: String? = null, val receiverName: String? = null,
                   val receiverPhone: String? = null, val receiverZoneCode: String? = null,
                   val receiverAddress: String? = null, val receiverSubAddress: String? = null,
                   val receiverSubAddressVisibility: Int? = null, val paymentType: String? = null)