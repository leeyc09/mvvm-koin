package xlab.world.xlab.server.provider

import io.reactivex.disposables.Disposable
import xlab.world.xlab.data.request.ReqLoginData
import xlab.world.xlab.data.response.*
import xlab.world.xlab.server.`interface`.IUserRequest
import xlab.world.xlab.server.errorHandle
import xlab.world.xlab.utils.rx.SchedulerProvider
import xlab.world.xlab.utils.rx.with

interface ApiUserProvider {
    fun checkValidToken(scheduler: SchedulerProvider, authorization: String, fcmToken: String,
                        responseData: (ResCheckValidTokenData) -> Unit, errorData: (ResMsgErrorData?) -> Unit): Disposable

    fun getRefreshToken(scheduler: SchedulerProvider, authorization: String,
                        responseData: (ResRefreshTokenData) -> Unit, errorData: (ResMsgErrorData?) -> Unit): Disposable

    fun generateToken(scheduler: SchedulerProvider, authorization: String,
                      responseData: (ResGenerateTokenData) -> Unit, errorData: (ResMsgErrorData?) -> Unit): Disposable

    fun requestLogin(scheduler: SchedulerProvider, reqLoginData: ReqLoginData,
                     responseData: (ResLoginData) -> Unit, errorData: (ResMsgErrorData?) -> Unit): Disposable
}

class ApiUser(private val iUserRequest: IUserRequest): ApiUserProvider {
    override fun checkValidToken(scheduler: SchedulerProvider, authorization: String, fcmToken: String,
                                 responseData: (ResCheckValidTokenData) -> Unit, errorData: (ResMsgErrorData?) -> Unit): Disposable {
        return iUserRequest.checkValidToken(authorization = authorization, fcmToken = fcmToken)
                .with(scheduler)
                .subscribe({
                    responseData(it)
                }, {
                    errorData(errorHandle<ResMsgErrorData>(it))
                })
    }

    override fun getRefreshToken(scheduler: SchedulerProvider, authorization: String,
                                 responseData: (ResRefreshTokenData) -> Unit, errorData: (ResMsgErrorData?) -> Unit): Disposable {
        return iUserRequest.getRefreshToken(authorization = authorization)
                .with(scheduler)
                .subscribe({
                    responseData(it)
                }, {
                    errorData(errorHandle<ResMsgErrorData>(it))
                })
    }

    override fun generateToken(scheduler: SchedulerProvider, authorization: String,
                               responseData: (ResGenerateTokenData) -> Unit, errorData: (ResMsgErrorData?) -> Unit): Disposable {
        return iUserRequest.generateToken(authorization = authorization)
                .with(scheduler)
                .subscribe({
                    responseData(it)
                }, {
                    errorData(errorHandle<ResMsgErrorData>(it))
                })
    }

    override fun requestLogin(scheduler: SchedulerProvider, reqLoginData: ReqLoginData,
                              responseData: (ResLoginData) -> Unit, errorData: (ResMsgErrorData?) -> Unit): Disposable {
        return iUserRequest.login(reqLoginData = reqLoginData, type = reqLoginData.type)
                .with(scheduler)
                .subscribe({
                    responseData(it)
                }, {
                    errorData(errorHandle<ResMsgErrorData>(it))
                })
    }
}