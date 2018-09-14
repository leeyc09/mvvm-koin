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

    // post detail 가져오기
    fun getPostDetail(scheduler: SchedulerProvider, authorization: String, postId: String,
                      responseData: (ResDetailPostData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable

    // post 지우기
    fun postDelete(scheduler: SchedulerProvider, authorization: String, postId: String,
                   responseData: (ResMessageData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable

    // my post 인지 확인하기
    fun checkMyPost(scheduler: SchedulerProvider, authorization: String, postId: String,
                   responseData: (ResCheckMyPostData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable

    //user posts thumbnail 가져오기
    fun requestUserPostsThumbnail(scheduler: SchedulerProvider, userId: String, page: Int,
                                  responseData: (ResThumbnailPostsData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable

    // user posts detail 가져오기
    fun requestUserPostsDetail(scheduler: SchedulerProvider, authorization: String, userId: String, page: Int,
                               responseData: (ResDetailPostsData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable
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

    override fun getPostDetail(scheduler: SchedulerProvider, authorization: String, postId: String,
                               responseData: (ResDetailPostData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable {
        return iPostRequest.getPostDetail(authorization = authorization, postId = postId)
                .with(scheduler)
                .subscribe({
                    responseData(it)
                }, {
                    errorData(errorHandle<ResMessageErrorData>(it))
                })
    }

    override fun postDelete(scheduler: SchedulerProvider, authorization: String, postId: String,
                            responseData: (ResMessageData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable {
        return iPostRequest.deletePost(authorization = authorization, postId = postId)
                .with(scheduler)
                .subscribe({
                    responseData(it)
                }, {
                    errorData(errorHandle<ResMessageErrorData>(it))
                })
    }

    override fun checkMyPost(scheduler: SchedulerProvider, authorization: String, postId: String,
                             responseData: (ResCheckMyPostData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable {
        return iPostRequest.checkPostsMine(authorization = authorization, postId = postId)
                .with(scheduler)
                .subscribe({
                    responseData(it)
                }, {
                    errorData(errorHandle<ResMessageErrorData>(it))
                })
    }

    override fun requestUserPostsThumbnail(scheduler: SchedulerProvider, userId: String, page: Int,
                                           responseData: (ResThumbnailPostsData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable {
        return iPostRequest.getUserPostsThumb(userId = userId, page = page)
                .with(scheduler)
                .subscribe({
                    responseData(it)
                }, {
                    errorData(errorHandle<ResMessageErrorData>(it))
                })
    }

    override fun requestUserPostsDetail(scheduler: SchedulerProvider, authorization: String, userId: String, page: Int,
                                        responseData: (ResDetailPostsData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable {
        return iPostRequest.getUserPostsDetail(authorization = authorization, userId = userId, page = page)
                .with(scheduler)
                .subscribe({
                    responseData(it)
                }, {
                    errorData(errorHandle<ResMessageErrorData>(it))
                })
    }
}