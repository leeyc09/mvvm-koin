package xlab.world.xlab.server.provider

import io.reactivex.disposables.Disposable
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

    // cart 추가 요청
    fun requestAddCart(scheduler: SchedulerProvider, authorization: String, goodsNo: String, count: Int,
                       responseData: (ResAddCartData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable

    // order status 갯수 요청
    fun requestOrderStatusCnt(scheduler: SchedulerProvider, authorization: String,
                              responseData: (ResOrderStatusCntData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable
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

    override fun requestOrderStatusCnt(scheduler: SchedulerProvider, authorization: String,
                                       responseData: (ResOrderStatusCntData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable {
        return iGodoRequest.getOrderStatusNum(authorization = authorization)
                .with(scheduler)
                .subscribe({
                    responseData(it)
                }, {
                    errorData(errorHandle<ResMessageErrorData>(it))
                })
    }
}