package xlab.world.xlab.server.provider

import io.reactivex.disposables.Disposable
import xlab.world.xlab.data.request.ReqLoginData
import xlab.world.xlab.data.request.ReqRegisterData
import xlab.world.xlab.data.response.*
import xlab.world.xlab.server.`interface`.IUserRequest
import xlab.world.xlab.server.errorHandle
import xlab.world.xlab.utils.rx.SchedulerProvider
import xlab.world.xlab.utils.rx.with

interface ApiUserProvider {
    // access token 만료 확인
    fun checkValidToken(scheduler: SchedulerProvider, authorization: String, fcmToken: String,
                        responseData: (ResCheckValidTokenData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable

    // refresh token 가져오기
    fun getRefreshToken(scheduler: SchedulerProvider, authorization: String,
                        responseData: (ResRefreshTokenData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable

    // access token 갱신
    fun generateToken(scheduler: SchedulerProvider, authorization: String,
                      responseData: (ResGenerateTokenData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable

    // login 요청
    fun requestLogin(scheduler: SchedulerProvider, reqLoginData: ReqLoginData,
                     responseData: (ResLoginData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable

    // register 요청
    fun requestRegister(scheduler: SchedulerProvider, reqRegisterData: ReqRegisterData,
                        responseData: (ResMessageData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable
}

class ApiUser(private val iUserRequest: IUserRequest): ApiUserProvider {
    override fun checkValidToken(scheduler: SchedulerProvider, authorization: String, fcmToken: String,
                                 responseData: (ResCheckValidTokenData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable {
        return iUserRequest.checkValidToken(authorization = authorization, fcmToken = fcmToken)
                .with(scheduler)
                .subscribe({
                    responseData(it)
                }, {
                    errorData(errorHandle<ResMessageErrorData>(it))
                })
    }

    override fun getRefreshToken(scheduler: SchedulerProvider, authorization: String,
                                 responseData: (ResRefreshTokenData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable {
        return iUserRequest.getRefreshToken(authorization = authorization)
                .with(scheduler)
                .subscribe({
                    responseData(it)
                }, {
                    errorData(errorHandle<ResMessageErrorData>(it))
                })
    }

    override fun generateToken(scheduler: SchedulerProvider, authorization: String,
                               responseData: (ResGenerateTokenData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable {
        return iUserRequest.generateToken(authorization = authorization)
                .with(scheduler)
                .subscribe({
                    responseData(it)
                }, {
                    errorData(errorHandle<ResMessageErrorData>(it))
                })
    }

    override fun requestLogin(scheduler: SchedulerProvider, reqLoginData: ReqLoginData,
                              responseData: (ResLoginData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable {
        return iUserRequest.login(reqLoginData = reqLoginData, type = reqLoginData.loginType)
                .with(scheduler)
                .subscribe({
                    responseData(it)
                }, {
                    errorData(errorHandle<ResMessageErrorData>(it))
                })
    }

    override fun requestRegister(scheduler: SchedulerProvider, reqRegisterData: ReqRegisterData,
                                 responseData: (ResMessageData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable {
        return iUserRequest.register(reqRegisterData = reqRegisterData)
                .with(scheduler)
                .subscribe({
                    responseData(it)
                }, {
                    errorData(errorHandle<ResMessageErrorData>(it))
                })
    }
}