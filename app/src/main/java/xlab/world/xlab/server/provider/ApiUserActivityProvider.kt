package xlab.world.xlab.server.provider

import io.reactivex.disposables.Disposable
import xlab.world.xlab.data.response.*
import xlab.world.xlab.server.`interface`.IPostRequest
import xlab.world.xlab.server.`interface`.IUserActivityRequest
import xlab.world.xlab.server.errorHandle
import xlab.world.xlab.utils.rx.SchedulerProvider
import xlab.world.xlab.utils.rx.with

interface ApiUserActivityProvider {
    // post like
    fun postLike(scheduler: SchedulerProvider, authorization: String, postId: String,
                 responseData: (ResLikeSavePostData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable

    // post save
    fun postSave(scheduler: SchedulerProvider, authorization: String, postId: String,
                 responseData: (ResLikeSavePostData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable
}

class ApiUserActivity(private val iUserActivityRequest: IUserActivityRequest): ApiUserActivityProvider {
    override fun postLike(scheduler: SchedulerProvider, authorization: String, postId: String,
                          responseData: (ResLikeSavePostData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable {
        return iUserActivityRequest.likePost(authorization = authorization, postId = postId)
                .with(scheduler)
                .subscribe({
                    responseData(it)
                }, {
                    errorData(errorHandle<ResMessageErrorData>(it))
                })
    }

    override fun postSave(scheduler: SchedulerProvider, authorization: String, postId: String,
                          responseData: (ResLikeSavePostData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable {
        return iUserActivityRequest.savePost(authorization = authorization, postId = postId)
                .with(scheduler)
                .subscribe({
                    responseData(it)
                }, {
                    errorData(errorHandle<ResMessageErrorData>(it))
                })
    }
}