package xlab.world.xlab.server.provider

import io.reactivex.disposables.Disposable
import xlab.world.xlab.data.response.ResCountHashTagData
import xlab.world.xlab.data.response.ResMessageErrorData
import xlab.world.xlab.data.response.ResRecentHashTagData
import xlab.world.xlab.server.`interface`.IHashTagRequest
import xlab.world.xlab.server.errorHandle
import xlab.world.xlab.utils.rx.SchedulerProvider
import xlab.world.xlab.utils.rx.with

interface ApiHashTagProvider {
    // 최근 사용 해시태그 요청
    fun requestRecentHashTag(scheduler: SchedulerProvider, authorization: String,
                             responseData: (ResRecentHashTagData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable

    // 해시태그 검색 with 사용 횟수
    fun searchHashTagCount(scheduler: SchedulerProvider, authorization: String, searchText: String,
                           responseData: (ResCountHashTagData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable
}

class ApiHashTag(private val iHashTagRequest: IHashTagRequest): ApiHashTagProvider {
    override fun requestRecentHashTag(scheduler: SchedulerProvider, authorization: String,
                                      responseData: (ResRecentHashTagData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable {
        return iHashTagRequest.recentHashTag(authorization = authorization)
                .with(scheduler)
                .subscribe({
                    responseData(it)
                }, {
                    errorData(errorHandle<ResMessageErrorData>(it))
                })
    }

    override fun searchHashTagCount(scheduler: SchedulerProvider, authorization: String, searchText: String,
                                    responseData: (ResCountHashTagData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable {
        return iHashTagRequest.searchHashTagCount(authorization = authorization, query = searchText)
                .with(scheduler)
                .subscribe({
                    responseData(it)
                }, {
                    errorData(errorHandle<ResMessageErrorData>(it))
                })
    }
}