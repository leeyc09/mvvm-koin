package xlab.world.xlab.server.provider

import io.reactivex.disposables.Disposable
import xlab.world.xlab.data.response.*
import xlab.world.xlab.server.`interface`.INoticeRequest
import xlab.world.xlab.server.errorHandle
import xlab.world.xlab.utils.rx.SchedulerProvider
import xlab.world.xlab.utils.rx.with

interface ApiNoticeProvider {
    // notice data 가져오기
    fun requestNoticeData(scheduler: SchedulerProvider, authorization: String, page: Int,
                          responseData: (ResNoticeData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable

    // notice 읽음
    fun updateReadNotice(scheduler: SchedulerProvider, authorization: String, noticeId: String,
                         responseData: (ResMessageData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable

    // new notice 있는지 요청
    fun requestNewNotice(scheduler: SchedulerProvider, authorization: String,
                         responseData: (ResExistNewData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable
}

class ApiNotice(private val iNoticeRequest: INoticeRequest): ApiNoticeProvider {
    override fun requestNoticeData(scheduler: SchedulerProvider, authorization: String, page: Int, responseData: (ResNoticeData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable {
        return iNoticeRequest.getNotice(authorization = authorization, page = page)
                .with(scheduler)
                .subscribe({
                    responseData(it)
                }, {
                    errorData(errorHandle<ResMessageErrorData>(it))
                })
    }

    override fun updateReadNotice(scheduler: SchedulerProvider, authorization: String, noticeId: String, responseData: (ResMessageData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable {
        return iNoticeRequest.readNotice(authorization = authorization, noticeId = noticeId)
                .with(scheduler)
                .subscribe({
                    responseData(it)
                }, {
                    errorData(errorHandle<ResMessageErrorData>(it))
                })
    }

    override fun requestNewNotice(scheduler: SchedulerProvider, authorization: String,
                                  responseData: (ResExistNewData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable {
        return iNoticeRequest.getExistNewNotice(authorization = authorization)
                .with(scheduler)
                .subscribe({
                    responseData(it)
                }, {
                    errorData(errorHandle<ResMessageErrorData>(it))
                })
    }
}