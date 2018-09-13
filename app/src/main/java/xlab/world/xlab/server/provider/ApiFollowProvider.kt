package xlab.world.xlab.server.provider

import io.reactivex.disposables.Disposable
import xlab.world.xlab.data.response.*
import xlab.world.xlab.server.`interface`.IFollowRequest
import xlab.world.xlab.server.errorHandle
import xlab.world.xlab.utils.rx.SchedulerProvider
import xlab.world.xlab.utils.rx.with

interface ApiFollowProvider {
    // follow user
    fun follow(scheduler: SchedulerProvider, authorization: String, userId: String,
               responseData: (ResFollowData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable
}

class ApiFollow(private val iFollowRequest: IFollowRequest): ApiFollowProvider {
    override fun follow(scheduler: SchedulerProvider, authorization: String, userId: String,
                        responseData: (ResFollowData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable {
        return iFollowRequest.follow(authorization = authorization, userId = userId)
                .with(scheduler)
                .subscribe({
                    responseData(it)
                }, {
                    errorData(errorHandle<ResMessageErrorData>(it))
                })
    }
}