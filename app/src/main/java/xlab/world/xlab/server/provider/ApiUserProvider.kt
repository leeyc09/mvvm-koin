package xlab.world.xlab.server.provider

import io.reactivex.disposables.Disposable
import xlab.world.xlab.data.request.ReqConfirmEmailData
import xlab.world.xlab.data.request.ReqLoginData
import xlab.world.xlab.data.request.ReqNewPasswordData
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
                        responseData: (ResGetRefreshTokenData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable

    // access token 갱신
    fun generateToken(scheduler: SchedulerProvider, authorization: String,
                      responseData: (ResGenerateTokenData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable

    // login 요청
    fun requestLogin(scheduler: SchedulerProvider, reqLoginData: ReqLoginData,
                     responseData: (ResUserLoginData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable

    // register 요청
    fun requestRegister(scheduler: SchedulerProvider, reqRegisterData: ReqRegisterData,
                        responseData: (ResUserRegisterData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable

    // email 인증 요청
    fun requestConfirmEmail(scheduler: SchedulerProvider, reqConfirmEmailData: ReqConfirmEmailData,
                            responseData: (ResMessageData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable

    // email code 인증 요청
    fun requestConfirmEmailCode(scheduler: SchedulerProvider, reqConfirmEmailData: ReqConfirmEmailData,
                                responseData: (ResConfirmEmailCodeData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable

    // change password 요청
    fun requestChangePassword(scheduler: SchedulerProvider, authorization: String, reqNewPasswordData: ReqNewPasswordData,
                              responseData: (ResMessageData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable

    // profile main data 요청
    fun requestProfileMain(scheduler: SchedulerProvider, authorization: String, userId: String,
                           responseData: (ResProfileMainData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable

    // profile edit data 요청
    fun requestProfileEdit(scheduler: SchedulerProvider, authorization: String,
                           responseData: (ResProfileEditData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable

    // recommend user data 요청
    fun requestRecommendUser(scheduler: SchedulerProvider, authorization: String, page: Int,
                             responseData: (ResUserDefaultData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable

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
                                 responseData: (ResGetRefreshTokenData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable {
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
                              responseData: (ResUserLoginData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable {
        return iUserRequest.login(reqLoginData = reqLoginData, type = reqLoginData.loginType)
                .with(scheduler)
                .subscribe({
                    responseData(it)
                }, {
                    errorData(errorHandle<ResMessageErrorData>(it))
                })
    }

    override fun requestRegister(scheduler: SchedulerProvider, reqRegisterData: ReqRegisterData,
                                 responseData: (ResUserRegisterData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable {
        return iUserRequest.register(reqRegisterData = reqRegisterData)
                .with(scheduler)
                .subscribe({
                    responseData(it)
                }, {
                    errorData(errorHandle<ResMessageErrorData>(it))
                })
    }

    override fun requestConfirmEmail(scheduler: SchedulerProvider, reqConfirmEmailData: ReqConfirmEmailData,
                                     responseData: (ResMessageData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable {
        return iUserRequest.confirmEmail(reqConfirmEmailData = reqConfirmEmailData)
                .with(scheduler)
                .subscribe({
                    responseData(it)
                }, {
                    errorData(errorHandle<ResMessageErrorData>(it))
                })
    }

    override fun requestConfirmEmailCode(scheduler: SchedulerProvider, reqConfirmEmailData: ReqConfirmEmailData,
                                         responseData: (ResConfirmEmailCodeData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable {
        return iUserRequest.confirmEmailCode(reqConfirmEmailData = reqConfirmEmailData)
                .with(scheduler)
                .subscribe({
                    responseData(it)
                }, {
                    errorData(errorHandle<ResMessageErrorData>(it))
                })
    }

    override fun requestChangePassword(scheduler: SchedulerProvider, authorization: String, reqNewPasswordData: ReqNewPasswordData,
                                       responseData: (ResMessageData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable {
        return iUserRequest.changePassword(authorization = authorization, reqPasswordData = reqNewPasswordData)
                .with(scheduler)
                .subscribe({
                    responseData(it)
                }, {
                    errorData(errorHandle<ResMessageErrorData>(it))
                })
    }

    override fun requestProfileMain(scheduler: SchedulerProvider, authorization: String, userId: String,
                                    responseData: (ResProfileMainData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable {
        return iUserRequest.profileMain(authorization = authorization, userId = userId)
                .with(scheduler)
                .subscribe({
                    responseData(it)
                }, {
                    errorData(errorHandle<ResMessageErrorData>(it))
                })
    }

    override fun requestProfileEdit(scheduler: SchedulerProvider, authorization: String,
                                    responseData: (ResProfileEditData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable {
        return iUserRequest.profileEdit(authorization = authorization)
                .with(scheduler)
                .subscribe({
                    responseData(it)
                }, {
                    errorData(errorHandle<ResMessageErrorData>(it))
                })
    }

    override fun requestRecommendUser(scheduler: SchedulerProvider, authorization: String, page: Int,
                                      responseData: (ResUserDefaultData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable {
        return iUserRequest.getRecommendUser(authorization = authorization, page = page)
                .with(scheduler)
                .subscribe({
                    responseData(it)
                }, {
                    errorData(errorHandle<ResMessageErrorData>(it))
                })
    }
}