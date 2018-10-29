package xlab.world.xlab.view.orderCRR

import android.app.Activity
import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.view.View
import io.reactivex.Observable
import xlab.world.xlab.R
import xlab.world.xlab.data.adapter.CartData
import xlab.world.xlab.data.adapter.CartListData
import xlab.world.xlab.data.request.ReqOrderCRRData
import xlab.world.xlab.server.provider.ApiGodoProvider
import xlab.world.xlab.utils.rx.SchedulerProvider
import xlab.world.xlab.utils.rx.with
import xlab.world.xlab.utils.support.GodoData
import xlab.world.xlab.utils.support.NetworkCheck
import xlab.world.xlab.utils.support.PrintLog
import xlab.world.xlab.utils.support.RequestCodeData
import xlab.world.xlab.view.AbstractViewModel

class OrderCRRViewModel(private val apiGodo: ApiGodoProvider,
                        private val networkCheck: NetworkCheck,
                        private val scheduler: SchedulerProvider): AbstractViewModel() {
    private val viewModelTag = "OrderCRR"

    private var crrMode: Int = RequestCodeData.ORDER_REFUND
    private var orderNo: String = ""
    private var sno: String = ""

    private var crrGoodsData: CartData = CartData()

    val uiData = MutableLiveData<UIModel>()

    fun initViewLayout(context: Context, crrMode: Int, orderNo: String, sno: String) {
        this.crrMode = crrMode
        this.orderNo = orderNo
        this.sno = sno
        launch {
            Observable.create<Any> {
                var titleStr: String? = null
                var reasonTitleStr: String? = null
                var refundVisibility: Int? = null
                val reasonList = ArrayList<String>()
                val bankList = arrayListOf<String>()
                bankList.addAll(context.resources.getStringArray(R.array.bankArray))

                when (crrMode) {
                    RequestCodeData.ORDER_REFUND -> { // 환불
                        titleStr = context.getString(R.string.order_step_request_refund)
                        reasonTitleStr = context.getString(R.string.goods_refund_reason)
                        refundVisibility = View.VISIBLE
                        reasonList.addAll(context.resources.getStringArray(R.array.refundReasonArray))
                    }
                    RequestCodeData.ORDER_RETURN -> { // 반품
                        titleStr = context.getString(R.string.order_step_request_return)
                        reasonTitleStr = context.getString(R.string.goods_return_reason)
                        refundVisibility = View.VISIBLE
                        reasonList.addAll(context.resources.getStringArray(R.array.returnReasonArray))
                    }
                    RequestCodeData.ORDER_CHANGE -> { // 교환
                        titleStr = context.getString(R.string.order_step_request_change)
                        reasonTitleStr = context.getString(R.string.goods_change_reason)
                        refundVisibility = View.GONE
                        reasonList.addAll(context.resources.getStringArray(R.array.changeReasonArray))
                    }
                }

                uiData.postValue(UIModel(titleStr = titleStr, reasonTitleStr = reasonTitleStr, refundVisibility = refundVisibility,
                        reasonList = reasonList, bankList = bankList))
            }.with(scheduler).subscribe {}
        }
    }

    fun loadCrrGoodsData(authorization: String) {
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
                        val newCrrGoodsData = CartData(total = 0)
                        it.goodsList?.forEach { goods ->
                            val newData = CartListData(
                                    sno = goods.sno,
                                    goodsCode = goods.code,
                                    goodsImage = goods.image,
                                    goodsBrand = goods.brand,
                                    goodsName = goods.name,
                                    goodsOption = null,
                                    goodsCnt = goods.count,
                                    goodsPrice = goods.price,
                                    goodsInitCnt = goods.count,
                                    deliverySno = goods.deliverySno,
                                    isSelect = this.sno == goods.sno
                            )
                            when (this.crrMode) {
                                RequestCodeData.ORDER_REFUND -> { // 환불일 경우 -> 결제완료, 상품준비중 상태 상품만 추가
                                    when (goods.orderState) {
                                        "p1", "g1" -> { // 결제완료, 상품준비중
                                            newCrrGoodsData.items.add(newData)
                                            newCrrGoodsData.total++
                                        }
                                    }
                                }
                                RequestCodeData.ORDER_RETURN -> { // 반품일 경우 -> 배송중, 배송완료 상태 상품만 추가
                                    when (goods.orderState) {
                                        "d1", "d2" -> { // 배송중, 배송완료
                                            newCrrGoodsData.items.add(newData)
                                            newCrrGoodsData.total++
                                        }
                                    }
                                }
                                RequestCodeData.ORDER_CHANGE -> { // 교환일 경우 -> 결제완료, 상품준비중, 배송중, 배송완료 상태 상품만 추가
                                    when (goods.orderState) {
                                        "p1", "g1", "d1", "d2" -> { // 결제완료, 상품준비중, 배송중, 배송완료
                                            newCrrGoodsData.items.add(newData)
                                            newCrrGoodsData.total++
                                        }
                                    }
                                }
                            }
                        }
                        this.crrGoodsData.updateData(cartData = newCrrGoodsData)

                        uiData.value = UIModel(isLoading = false, crrGoodsData = this.crrGoodsData,
                                crrGoodsSelectCnt = "1", crrGoodsTotalCnt = this.crrGoodsData.total.toString(),
                                selectAll = this.crrGoodsData.total == 1, finishBtnEnable= false)
                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(isLoading = false)
                        errorData?.let {
                            PrintLog.e("requestOrderList fail", errorData.message, viewModelTag)
                        }
                    })
        }
    }

    fun selectAllCrrGoodsData(isSelectAll: Boolean) {
        launch {
            Observable.create<Boolean> {
                // 전체 상품 선택&해제
                crrGoodsData.items.forEach { data ->
                    data.isSelect = !isSelectAll
                }

                it.onNext(!isSelectAll)
                it.onComplete()
            }.with(scheduler).subscribe {
                uiData.value = UIModel(crrGoodsSelectCnt = (if (it) crrGoodsData.total else 0).toString(),
                        crrGoodsDataUpdate = true, selectAll = it, needFinishBtnEnableCheck = true)
            }
        }
    }

    fun selectCrrGoodsData(crrGoodsIndex: Int) {
        launch {
            Observable.create<Int> {
                this.crrGoodsData.items[crrGoodsIndex].isSelect = !this.crrGoodsData.items[crrGoodsIndex].isSelect
                // 선택 된 상품 갯수 계산
                var selectCnt = 0
                crrGoodsData.items.forEach { data ->
                    if (data.isSelect) selectCnt++
                }

                it.onNext(selectCnt)
                it.onComplete()
            }.with(scheduler).subscribe {
                uiData.value = UIModel(crrGoodsSelectCnt = it.toString(),
                        crrGoodsDataUpdateIndex = crrGoodsIndex,
                        selectAll = it == crrGoodsData.total,
                        needFinishBtnEnableCheck = true)
            }
        }
    }

    fun crrGoodsCntPlus(crrGoodsIndex: Int) {
        if (this.crrGoodsData.items[crrGoodsIndex].goodsInitCnt > this.crrGoodsData.items[crrGoodsIndex].goodsCnt) {
            this.crrGoodsData.items[crrGoodsIndex].goodsCnt++
            uiData.postValue(UIModel(crrGoodsDataUpdateIndex = crrGoodsIndex))
        }
    }

    fun crrGoodsCntMinus(crrGoodsIndex: Int) {
        if (this.crrGoodsData.items[crrGoodsIndex].goodsCnt > 1) {
            this.crrGoodsData.items[crrGoodsIndex].goodsCnt--
            uiData.postValue(UIModel(crrGoodsDataUpdateIndex = crrGoodsIndex))
        }
    }

    fun checkFinishEnable(crrGoodsSelectCnt: Int, isReasonSelect: Boolean, isBankSelect: Boolean,
                          bankAccount: String, accountHolder: String) {
        launch {
            Observable.create<Boolean> {
                var finishEnable = crrGoodsSelectCnt > 0 && // 선택 상품 1개 이상
                        isReasonSelect // 사유 선택

                // 환불, 반품 -> 환분 정보까지 필수 입력사항
                if (finishEnable &&
                        (crrMode == RequestCodeData.ORDER_REFUND || crrMode == RequestCodeData.ORDER_RETURN)) {
                    finishEnable = isBankSelect && // 사유, 환불 은행 선택
                            bankAccount.isNotEmpty() && accountHolder.isNotEmpty() // 환불 계좌번호, 예금주 입력
                }

                it.onNext(finishEnable)
                it.onComplete()
            }.with(scheduler).subscribe {
                uiData.value = UIModel(finishBtnEnable = it)
            }
        }
    }

    fun orderCRR(authorization: String, reason: String, detailReason: String,
                 bankName: String, bankAccount: String, accountHolder: String) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = networkCheck.networkErrorMsg))
            return
        }

        uiData.value = UIModel(isLoading = true)
        launch {
            val reqOrderCrrData = ReqOrderCRRData()

            // crr 모드
            val mode = when (crrMode) {
                RequestCodeData.ORDER_REFUND -> GodoData.REQUEST_REFUND_CODE
                RequestCodeData.ORDER_RETURN -> GodoData.REQUEST_RETURN_CODE
                RequestCodeData.ORDER_CHANGE -> GodoData.REQUEST_CHANGE_CODE
                else -> ""
            }
            reqOrderCrrData.addMode(mode = mode)
            // set order no
            reqOrderCrrData.addOrderNo(orderNo = orderNo)
            // set goods sno, count
            crrGoodsData.items.forEach { goods ->
                if (goods.isSelect)
                    reqOrderCrrData.addClaimGoods(goodsNo = goods.sno, goodsCnt = goods.goodsCnt)
            }
            // set reason
            reqOrderCrrData.addReason(reason = reason)
            reqOrderCrrData.addDetailReason(detailReason = detailReason)

            // set bank info
            if (crrMode != RequestCodeData.ORDER_CHANGE) { // 교환 아닌 경우 환불 정보 입력
                reqOrderCrrData.addBankName(bankName = bankName)
                reqOrderCrrData.addBankAccount(bankAccount = bankAccount)
                reqOrderCrrData.addAccountHolder(accountHolder = accountHolder)
            }

            apiGodo.requestOrderCRR(scheduler = scheduler, authorization = authorization, requestBody = reqOrderCrrData.getReqBody(),
                    responseData = {
                        val toastMessage = when (crrMode) {
                            RequestCodeData.ORDER_REFUND -> "환불 신청되었습니다"
                            RequestCodeData.ORDER_RETURN -> "반품 신청되었습니다"
                            RequestCodeData.ORDER_CHANGE -> "교환 신청되었습니다"
                            else -> null
                        }
                        uiData.value = UIModel(isLoading = false, toastMessage = toastMessage,
                                resultCode = Activity.RESULT_OK)
                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(isLoading = false)
                        errorData?.let {
                            PrintLog.e("requestOrderCRR fail", errorData.message, viewModelTag)
                        }
                    })
        }
    }

    fun backBtnAction() {
        uiData.postValue(UIModel(resultCode = Activity.RESULT_CANCELED))
    }
}

data class UIModel(val isLoading: Boolean? = null, val toastMessage: String? = null, val resultCode: Int? = null,
                   val titleStr: String? = null, val reasonTitleStr: String? = null, val refundVisibility: Int? = null,
                   val reasonList: ArrayList<String>? = null, val bankList: ArrayList<String>? = null,
                   val crrGoodsData: CartData? = null, val crrGoodsDataUpdate: Boolean? = null, val crrGoodsDataUpdateIndex: Int? = null,
                   val crrGoodsSelectCnt: String? = null, val crrGoodsTotalCnt: String? = null,
                   val selectAll: Boolean? = null,
                   val needFinishBtnEnableCheck: Boolean? = null, val finishBtnEnable: Boolean? = null)
