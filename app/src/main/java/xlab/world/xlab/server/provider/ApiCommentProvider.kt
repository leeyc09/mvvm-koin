package xlab.world.xlab.server.provider

import io.reactivex.disposables.Disposable
import okhttp3.RequestBody
import xlab.world.xlab.data.response.*
import xlab.world.xlab.server.`interface`.ICommentRequest
import xlab.world.xlab.server.`interface`.IFollowRequest
import xlab.world.xlab.server.errorHandle
import xlab.world.xlab.utils.rx.SchedulerProvider
import xlab.world.xlab.utils.rx.with

interface ApiCommentProvider {
    // comment 가져오기
    fun getComment(scheduler: SchedulerProvider, postId: String, page: Int,
                   responseData: (ResCommentData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable

    // comment 작성
    fun postComment(scheduler: SchedulerProvider, authorization: String, postId: String, requestBody: RequestBody,
                    responseData: (ResMessageData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable

    // comment 삭제
    fun deleteComment(scheduler: SchedulerProvider, authorization: String, postId: String, position: Int,
                      responseData: (ResMessageData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable
}

class ApiComment(private val iCommentRequest: ICommentRequest): ApiCommentProvider {
    override fun getComment(scheduler: SchedulerProvider, postId: String, page: Int,
                            responseData: (ResCommentData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable {
        return iCommentRequest.getComments(postId = postId, page = page)
                .with(scheduler = scheduler)
                .subscribe({
                    responseData(it)
                }, {
                    errorData(errorHandle<ResMessageErrorData>(it))
                })
    }

    override fun postComment(scheduler: SchedulerProvider, authorization: String, postId: String, requestBody: RequestBody,
                             responseData: (ResMessageData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable {
        return iCommentRequest.postComment(authorization = authorization, postId = postId, requestBody = requestBody)
                .with(scheduler = scheduler)
                .subscribe({
                    responseData(it)
                }, {
                    errorData(errorHandle<ResMessageErrorData>(it))
                })
    }

    override fun deleteComment(scheduler: SchedulerProvider, authorization: String, postId: String, position: Int,
                               responseData: (ResMessageData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable {
        return iCommentRequest.deleteComment(authorization = authorization, postId = postId, index = position)
                .with(scheduler = scheduler)
                .subscribe({
                    responseData(it)
                }, {
                    errorData(errorHandle<ResMessageErrorData>(it))
                })
    }
}