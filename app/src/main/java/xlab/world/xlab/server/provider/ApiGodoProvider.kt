package xlab.world.xlab.server.provider

import io.reactivex.disposables.Disposable
import okhttp3.RequestBody
import xlab.world.xlab.data.response.*
import xlab.world.xlab.server.`interface`.IGodoRequest
import xlab.world.xlab.server.errorHandle
import xlab.world.xlab.utils.rx.SchedulerProvider
import xlab.world.xlab.utils.rx.with

interface ApiGodoProvider {
    // shop profile 요청
    fun requestShopProfile(scheduler: SchedulerProvider, authorization: String,
                           responseData: (ResShopProfileData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable

    // shop profile update 요청
    fun requestUpdateShopProfile(scheduler: SchedulerProvider, authorization: String, name: String, email: String,
                                 responseData: (ResMessageData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable

    // cart 목록 요청
    fun requestGetCart(scheduler: SchedulerProvider, authorization: String,
                       responseData: (ResCartData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable

    // cart 추가 요청
    fun requestAddCart(scheduler: SchedulerProvider, authorization: String, goodsNo: String, count: Int,
                       responseData: (ResAddCartData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable

    // cart 업데이트 요청
    fun requestUpdateCart(scheduler: SchedulerProvider, authorization: String, sno: String, count: Int,
                          responseData: (ResMessageData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable

    // cart 삭제 요청
    fun requestDeleteCart(scheduler: SchedulerProvider, authorization: String, sno: String,
                          responseData: (ResMessageData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable

    // order status 갯수 요청
    fun requestOrderStateCnt(scheduler: SchedulerProvider, authorization: String,
                              responseData: (ResOrderStateCntData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable

    // order state list 요청
    fun requestOrderStateList(scheduler: SchedulerProvider, authorization: String, state: Int,
                              responseData: (ResOrderHistoryData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable

    // order detail 요청
    fun requestOrderDetail(scheduler: SchedulerProvider, authorization: String, orderNo: String,
                           responseData: (ResOrderDetailData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable

    // order list 요청
    fun requestOrderList(scheduler: SchedulerProvider, authorization: String,
                         responseData: (ResOrderHistoryData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable

    // order CRR (Change Refund Return) 요청
    fun requestOrderCRR(scheduler: SchedulerProvider, authorization: String, requestBody: RequestBody,
                        responseData: (ResMessageData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable

    // CRR(Change Refund Return) 상세 요청
    fun requestCRRDetail(scheduler: SchedulerProvider, authorization: String, handleSno: String,
                         responseData: (ResCRRDetailData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable

    // order cancel 요청
    fun requestOrderCancel(scheduler: SchedulerProvider, authorization: String, orderNo: String,
                           responseData: (ResMessageData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable

    // 수취확인 요청
    fun requestOrderReceiveConfirm(scheduler: SchedulerProvider, authorization: String, orderNo: String, sno: String,
                                   responseData: (ResMessageData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable

    // 구매 확정 요청
    fun requestBuyDecide(scheduler: SchedulerProvider, authorization: String, orderNo: String, sno: String,
                         responseData: (ResMessageData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable
}

class ApiGodo(private val iGodoRequest: IGodoRequest): ApiGodoProvider {
    override fun requestShopProfile(scheduler: SchedulerProvider, authorization: String,
                                    responseData: (ResShopProfileData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable {
        return iGodoRequest.getShopProfile(authorization = authorization)
                .with(scheduler)
                .subscribe({
                    responseData(it)
                }, {
                    errorData(errorHandle<ResMessageErrorData>(it))
                })
    }

    override fun requestUpdateShopProfile(scheduler: SchedulerProvider, authorization: String, name: String, email: String,
                                          responseData: (ResMessageData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable {
        return iGodoRequest.updateShopProfile(authorization = authorization, name = name, email = email)
                .with(scheduler)
                .subscribe({
                    responseData(it)
                }, {
                    errorData(errorHandle<ResMessageErrorData>(it))
                })
    }

    override fun requestGetCart(scheduler: SchedulerProvider, authorization: String,
                                responseData: (ResCartData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable {
        return iGodoRequest.getMyCart(authorization = authorization)
                .with(scheduler)
                .subscribe({
                    responseData(it)
                }, {
                    errorData(errorHandle<ResMessageErrorData>(it))
                })
    }

    override fun requestAddCart(scheduler: SchedulerProvider, authorization: String, goodsNo: String, count: Int,
                                responseData: (ResAddCartData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable {
        return iGodoRequest.addMyCart(authorization = authorization, goodsNo = goodsNo, count = count)
                .with(scheduler)
                .subscribe({
                    responseData(it)
                }, {
                    errorData(errorHandle<ResMessageErrorData>(it))
                })
    }

    override fun requestUpdateCart(scheduler: SchedulerProvider, authorization: String, sno: String, count: Int,
                                   responseData: (ResMessageData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable {
        return iGodoRequest.updateMyCart(authorization = authorization, sno = sno, count = count)
                .with(scheduler)
                .subscribe({
                    responseData(it)
                }, {
                    errorData(errorHandle<ResMessageErrorData>(it))
                })
    }

    override fun requestDeleteCart(scheduler: SchedulerProvider, authorization: String, sno: String,
                                   responseData: (ResMessageData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable {
        return iGodoRequest.deleteMyCart(authorization = authorization, sno = sno)
                .with(scheduler)
                .subscribe({
                    responseData(it)
                }, {
                    errorData(errorHandle<ResMessageErrorData>(it))
                })
    }

    override fun requestOrderStateCnt(scheduler: SchedulerProvider, authorization: String,
                                       responseData: (ResOrderStateCntData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable {
        return iGodoRequest.getOrderStateCnt(authorization = authorization)
                .with(scheduler)
                .subscribe({
                    responseData(it)
                }, {
                    errorData(errorHandle<ResMessageErrorData>(it))
                })
    }

    override fun requestOrderStateList(scheduler: SchedulerProvider, authorization: String, state: Int,
                                       responseData: (ResOrderHistoryData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable {
        return iGodoRequest.getOrderStateHistory(authorization = authorization, state = state)
                .with(scheduler)
                .subscribe({
                    responseData(it)
                }, {
                    errorData(errorHandle<ResMessageErrorData>(it))
                })
    }

    override fun requestOrderDetail(scheduler: SchedulerProvider, authorization: String, orderNo: String,
                                    responseData: (ResOrderDetailData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable {
        return iGodoRequest.getOrderDetail(authorization = authorization, orderNo = orderNo)
                .with(scheduler)
                .subscribe({
                    responseData(it)
                }, {
                    errorData(errorHandle<ResMessageErrorData>(it))
                })
    }

    override fun requestOrderList(scheduler: SchedulerProvider, authorization: String,
                                  responseData: (ResOrderHistoryData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable {
        return iGodoRequest.getOrderHistory(authorization = authorization)
                .with(scheduler)
                .subscribe({
                    responseData(it)
                }, {
                    errorData(errorHandle<ResMessageErrorData>(it))
                })
    }

    override fun requestOrderCRR(scheduler: SchedulerProvider, authorization: String, requestBody: RequestBody,
                                 responseData: (ResMessageData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable {
        return iGodoRequest.orderCRR(authorization = authorization, requestBody = requestBody)
                .with(scheduler)
                .subscribe({
                    responseData(it)
                }, {
                    errorData(errorHandle<ResMessageErrorData>(it))
                })
    }

    override fun requestCRRDetail(scheduler: SchedulerProvider, authorization: String, handleSno: String,
                                  responseData: (ResCRRDetailData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable {
        return iGodoRequest.getCRRDetail(authorization = authorization, handleSno = handleSno)
                .with(scheduler)
                .subscribe({
                    responseData(it)
                }, {
                    errorData(errorHandle<ResMessageErrorData>(it))
                })
    }

    override fun requestOrderCancel(scheduler: SchedulerProvider, authorization: String, orderNo: String,
                                    responseData: (ResMessageData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable {
        return iGodoRequest.orderCancel(authorization = authorization, orderNo = orderNo)
                .with(scheduler)
                .subscribe({
                    responseData(it)
                }, {
                    errorData(errorHandle<ResMessageErrorData>(it))
                })
    }

    override fun requestOrderReceiveConfirm(scheduler: SchedulerProvider, authorization: String, orderNo: String, sno: String,
                                            responseData: (ResMessageData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable {
        return iGodoRequest.orderReceiveConfirm(authorization = authorization, orderNo = orderNo, sno = sno)
                .with(scheduler)
                .subscribe({
                    responseData(it)
                }, {
                    errorData(errorHandle<ResMessageErrorData>(it))
                })
    }

    override fun requestBuyDecide(scheduler: SchedulerProvider, authorization: String, orderNo: String, sno: String,
                                  responseData: (ResMessageData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable {
        return iGodoRequest.buyDecide(authorization = authorization, orderNo = orderNo, sno = sno)
                .with(scheduler)
                .subscribe({
                    responseData(it)
                }, {
                    errorData(errorHandle<ResMessageErrorData>(it))
                })
    }
}