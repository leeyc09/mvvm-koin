package xlab.world.xlab.server.provider

import io.reactivex.disposables.Disposable
import xlab.world.xlab.data.request.ReqLoginData
import xlab.world.xlab.data.response.ResErrorData
import xlab.world.xlab.data.response.ResLoginData
import xlab.world.xlab.server.`interface`.IUserRequest
import xlab.world.xlab.server.errorHandle
import xlab.world.xlab.utils.rx.SchedulerProvider
import xlab.world.xlab.utils.rx.with

interface ApiUserProvider {
    fun requestLogin(scheduler: SchedulerProvider, reqLoginData: ReqLoginData,
                     responseData: (ResLoginData) -> Unit, errorData: (ResErrorData?) -> Unit): Disposable
}

class ApiUser(private val iUserRequest: IUserRequest): ApiUserProvider {
    override fun requestLogin(scheduler: SchedulerProvider, reqLoginData: ReqLoginData,
                              responseData: (ResLoginData) -> Unit, errorData: (ResErrorData?) -> Unit): Disposable {
        return iUserRequest.login(reqLoginData = reqLoginData, type = reqLoginData.type)
                .with(scheduler)
                .subscribe({
                    responseData(it)
                }, {
                    errorData(errorHandle<ResErrorData>(it))
                })
    }
}