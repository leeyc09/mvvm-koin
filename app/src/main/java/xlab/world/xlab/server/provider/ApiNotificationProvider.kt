package xlab.world.xlab.server.provider

import io.reactivex.disposables.Disposable
import xlab.world.xlab.data.request.ReqGoodsSearchData
import xlab.world.xlab.data.response.*
import xlab.world.xlab.server.`interface`.INotificationRequest
import xlab.world.xlab.server.`interface`.IShopRequest
import xlab.world.xlab.server.errorHandle
import xlab.world.xlab.utils.rx.SchedulerProvider
import xlab.world.xlab.utils.rx.with

interface ApiNotificationProvider {
    // social notification 요청
    fun requestSocialNotification(scheduler: SchedulerProvider, authorization: String, page: Int,
                                  responseData: (ResSocialNotificationData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable

    // new notification 있는지 요청
    fun requestNewNotification(scheduler: SchedulerProvider, authorization: String,
                               responseData: (ResExistNewData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable
}

class ApiNotification(private val iNotificationRequest: INotificationRequest): ApiNotificationProvider {
    override fun requestSocialNotification(scheduler: SchedulerProvider, authorization: String, page: Int,
                                           responseData: (ResSocialNotificationData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable {
        return iNotificationRequest.getSocialNotification(authorization = authorization, page = page)
                .with(scheduler)
                .subscribe({
                    responseData(it)
                }, {
                    errorData(errorHandle<ResMessageErrorData>(it))
                })
    }

    override fun requestNewNotification(scheduler: SchedulerProvider, authorization: String,
                                        responseData: (ResExistNewData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable {
        return iNotificationRequest.getExistNewNotification(authorization = authorization)
                .with(scheduler)
                .subscribe({
                    responseData(it)
                }, {
                    errorData(errorHandle<ResMessageErrorData>(it))
                })
    }
}