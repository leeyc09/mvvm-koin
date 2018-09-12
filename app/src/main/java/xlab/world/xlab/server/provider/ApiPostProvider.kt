package xlab.world.xlab.server.provider

import io.reactivex.disposables.Disposable
import xlab.world.xlab.data.response.*
import xlab.world.xlab.server.`interface`.IPostRequest
import xlab.world.xlab.server.errorHandle
import xlab.world.xlab.utils.rx.SchedulerProvider
import xlab.world.xlab.utils.rx.with

interface ApiPostProvider {
    // all feed 가져오기
    fun getAllFeed(scheduler: SchedulerProvider, authorization: String, page: Int,
                   responseData: (ResFeedData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable

    // following feed 가져오기
    fun getFollowingFeed(scheduler: SchedulerProvider, authorization: String, page: Int,
                         responseData: (ResDetailPostsData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable

    // explore feed 가져오기
    fun getExploreFeed(scheduler: SchedulerProvider, authorization: String, page: Int,
                       responseData: (ResFeedData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable
}

class ApiPost(private val iPostRequest: IPostRequest): ApiPostProvider {
    override fun getAllFeed(scheduler: SchedulerProvider, authorization: String, page: Int,
                            responseData: (ResFeedData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable {
        return iPostRequest.getAllFeed(authorization = authorization, page = page)
                .with(scheduler)
                .subscribe({
                    responseData(it)
                }, {
                    errorData(errorHandle<ResMessageErrorData>(it))
                })
    }

    override fun getFollowingFeed(scheduler: SchedulerProvider, authorization: String, page: Int,
                                  responseData: (ResDetailPostsData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable {
        return iPostRequest.getFollowingFeed(authorization = authorization, page = page)
                .with(scheduler)
                .subscribe({
                    responseData(it)
                }, {
                    errorData(errorHandle<ResMessageErrorData>(it))
                })
    }

    override fun getExploreFeed(scheduler: SchedulerProvider, authorization: String, page: Int,
                                responseData: (ResFeedData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable {
        return iPostRequest.getExploreFeed(authorization = authorization, page = page)
                .with(scheduler)
                .subscribe({
                    responseData(it)
                }, {
                    errorData(errorHandle<ResMessageErrorData>(it))
                })
    }
}