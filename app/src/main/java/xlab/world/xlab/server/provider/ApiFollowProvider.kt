package xlab.world.xlab.server.provider

import io.reactivex.disposables.Disposable
import xlab.world.xlab.data.response.*
import xlab.world.xlab.server.`interface`.IFollowRequest
import xlab.world.xlab.server.errorHandle
import xlab.world.xlab.utils.rx.SchedulerProvider
import xlab.world.xlab.utils.rx.with

interface ApiFollowProvider {
    // user follow 요청
    fun requestFollow(scheduler: SchedulerProvider, authorization: String, userId: String,
               responseData: (ResFollowData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable

    // follower 가져오기
    fun requestGetFollower(scheduler: SchedulerProvider, authorization: String, userId: String, page: Int,
                           responseData: (ResUserDefaultData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable

    // following 가져오기
    fun requestGetFollowing(scheduler: SchedulerProvider, authorization: String, userId: String, page: Int,
                            responseData: (ResUserDefaultData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable
}

class ApiFollow(private val iFollowRequest: IFollowRequest): ApiFollowProvider {
    override fun requestFollow(scheduler: SchedulerProvider, authorization: String, userId: String,
                        responseData: (ResFollowData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable {
        return iFollowRequest.follow(authorization = authorization, userId = userId)
                .with(scheduler = scheduler)
                .subscribe({
                    responseData(it)
                }, {
                    errorData(errorHandle<ResMessageErrorData>(it))
                })
    }

    override fun requestGetFollower(scheduler: SchedulerProvider, authorization: String, userId: String, page: Int,
                                    responseData: (ResUserDefaultData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable {
        return iFollowRequest.getFollowerList(authorization = authorization, userId = userId, page = page)
                .with(scheduler = scheduler)
                .subscribe({
                    responseData(it)
                }, {
                    errorData(errorHandle<ResMessageErrorData>(it))
                })
    }

    override fun requestGetFollowing(scheduler: SchedulerProvider, authorization: String, userId: String, page: Int,
                                     responseData: (ResUserDefaultData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable {
        return iFollowRequest.getFollowingList(authorization = authorization, userId = userId, page = page)
                .with(scheduler = scheduler)
                .subscribe({
                    responseData(it)
                }, {
                    errorData(errorHandle<ResMessageErrorData>(it))
                })
    }
}