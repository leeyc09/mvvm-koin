package xlab.world.xlab.server.provider

import io.reactivex.disposables.Disposable
import okhttp3.RequestBody
import xlab.world.xlab.data.request.ReqConfirmEmailData
import xlab.world.xlab.data.request.ReqLoginData
import xlab.world.xlab.data.request.ReqPasswordData
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

    // logout 요청
    fun requestLogout(scheduler: SchedulerProvider, authorization: String,
                      responseData: (ResMessageData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable

    // register 요청
    fun requestRegister(scheduler: SchedulerProvider, reqRegisterData: ReqRegisterData,
                        responseData: (ResUserRegisterData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable

    // 회원 탈퇴 요청
    fun requestWithdraw(scheduler: SchedulerProvider, authorization: String, content: String,
                        responseData: (ResMessageData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable

    // email 인증 요청
    fun requestConfirmEmail(scheduler: SchedulerProvider, reqConfirmEmailData: ReqConfirmEmailData,
                            responseData: (ResMessageData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable

    // email code 인증 요청
    fun requestConfirmEmailCode(scheduler: SchedulerProvider, reqConfirmEmailData: ReqConfirmEmailData,
                                responseData: (ResConfirmEmailCodeData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable

    // password confirm 요청
    fun requestConfirmPassword(scheduler: SchedulerProvider, authorization: String, reqPasswordData: ReqPasswordData,
                               responseData: (ResMessageData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable

    // change password 요청
    fun requestChangePassword(scheduler: SchedulerProvider, authorization: String, reqNewPasswordData: ReqPasswordData,
                              responseData: (ResMessageData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable

    // profile main data 요청
    fun requestProfileMain(scheduler: SchedulerProvider, authorization: String, userId: String,
                           responseData: (ResProfileMainData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable

    // profile edit data 요청
    fun requestProfileEdit(scheduler: SchedulerProvider, authorization: String,
                           responseData: (ResProfileEditData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable

    // profile update 요청
    fun requestProfileUpdate(scheduler: SchedulerProvider, authorization: String, userId: String, requestBody: RequestBody,
                             responseData: (ResMessageData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable

    // recommend user data 요청
    fun requestRecommendUser(scheduler: SchedulerProvider, authorization: String, page: Int,
                             responseData: (ResUserDefaultData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable

    // setting data 요청
    fun requestSetting(scheduler: SchedulerProvider, authorization: String,
                       responseData: (ResSettingData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable

    // push setting update 요청
    fun requestPushUpdate(scheduler: SchedulerProvider, authorization: String,
                          responseData: (ResSettingPushData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable

    // user search 요청
    fun requestSearchUser(scheduler: SchedulerProvider, authorization: String, searchText: String, page: Int,
                          responseData: (ResUserDefaultData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable

    // goods used user 요청
    fun requestGoodsUsedUser(scheduler: SchedulerProvider, goodsCode: String, page: Int,
                             responseData: (ResGoodsUsedUserData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable

}

class ApiUser(private val iUserRequest: IUserRequest): ApiUserProvider {
    override fun checkValidToken(scheduler: SchedulerProvider, authorization: String, fcmToken: String,
                                 responseData: (ResCheckValidTokenData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable {
        return iUserRequest.checkValidToken(authorization = authorization, fcmToken = fcmToken)
                .with(scheduler = scheduler)
                .subscribe({
                    responseData(it)
                }, {
                    errorData(errorHandle<ResMessageErrorData>(it))
                })
    }

    override fun getRefreshToken(scheduler: SchedulerProvider, authorization: String,
                                 responseData: (ResGetRefreshTokenData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable {
        return iUserRequest.getRefreshToken(authorization = authorization)
                .with(scheduler = scheduler)
                .subscribe({
                    responseData(it)
                }, {
                    errorData(errorHandle<ResMessageErrorData>(it))
                })
    }

    override fun generateToken(scheduler: SchedulerProvider, authorization: String,
                               responseData: (ResGenerateTokenData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable {
        return iUserRequest.generateToken(authorization = authorization)
                .with(scheduler = scheduler)
                .subscribe({
                    responseData(it)
                }, {
                    errorData(errorHandle<ResMessageErrorData>(it))
                })
    }

    override fun requestLogin(scheduler: SchedulerProvider, reqLoginData: ReqLoginData,
                              responseData: (ResUserLoginData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable {
        return iUserRequest.login(reqLoginData = reqLoginData, type = reqLoginData.loginType)
                .with(scheduler = scheduler)
                .subscribe({
                    responseData(it)
                }, {
                    errorData(errorHandle<ResMessageErrorData>(it))
                })
    }

    override fun requestLogout(scheduler: SchedulerProvider, authorization: String,
                               responseData: (ResMessageData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable {
        return iUserRequest.logout(authorization = authorization)
                .with(scheduler = scheduler)
                .subscribe({
                    responseData(it)
                }, {
                    errorData(errorHandle<ResMessageErrorData>(it))
                })
    }

    override fun requestRegister(scheduler: SchedulerProvider, reqRegisterData: ReqRegisterData,
                                 responseData: (ResUserRegisterData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable {
        return iUserRequest.register(reqRegisterData = reqRegisterData)
                .with(scheduler = scheduler)
                .subscribe({
                    responseData(it)
                }, {
                    errorData(errorHandle<ResMessageErrorData>(it))
                })
    }

    override fun requestWithdraw(scheduler: SchedulerProvider, authorization: String, content: String,
                                 responseData: (ResMessageData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable {
        return iUserRequest.withdraw(authorization = authorization, content = content)
                .with(scheduler = scheduler)
                .subscribe({
                    responseData(it)
                }, {
                    errorData(errorHandle<ResMessageErrorData>(it))
                })
    }

    override fun requestConfirmEmail(scheduler: SchedulerProvider, reqConfirmEmailData: ReqConfirmEmailData,
                                     responseData: (ResMessageData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable {
        return iUserRequest.confirmEmail(reqConfirmEmailData = reqConfirmEmailData)
                .with(scheduler = scheduler)
                .subscribe({
                    responseData(it)
                }, {
                    errorData(errorHandle<ResMessageErrorData>(it))
                })
    }

    override fun requestConfirmEmailCode(scheduler: SchedulerProvider, reqConfirmEmailData: ReqConfirmEmailData,
                                         responseData: (ResConfirmEmailCodeData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable {
        return iUserRequest.confirmEmailCode(reqConfirmEmailData = reqConfirmEmailData)
                .with(scheduler = scheduler)
                .subscribe({
                    responseData(it)
                }, {
                    errorData(errorHandle<ResMessageErrorData>(it))
                })
    }

    override fun requestConfirmPassword(scheduler: SchedulerProvider, authorization: String, reqPasswordData: ReqPasswordData,
                                        responseData: (ResMessageData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable {
        return iUserRequest.checkPassword(authorization = authorization, reqPasswordData = reqPasswordData)
                .with(scheduler = scheduler)
                .subscribe({
                    responseData(it)
                }, {
                    errorData(errorHandle<ResMessageErrorData>(it))
                })
    }

    override fun requestChangePassword(scheduler: SchedulerProvider, authorization: String, reqNewPasswordData: ReqPasswordData,
                                       responseData: (ResMessageData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable {
        return iUserRequest.changePassword(authorization = authorization, reqPasswordData = reqNewPasswordData)
                .with(scheduler = scheduler)
                .subscribe({
                    responseData(it)
                }, {
                    errorData(errorHandle<ResMessageErrorData>(it))
                })
    }

    override fun requestProfileMain(scheduler: SchedulerProvider, authorization: String, userId: String,
                                    responseData: (ResProfileMainData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable {
        return iUserRequest.profileMain(authorization = authorization, userId = userId)
                .with(scheduler = scheduler)
                .subscribe({
                    responseData(it)
                }, {
                    errorData(errorHandle<ResMessageErrorData>(it))
                })
    }

    override fun requestProfileEdit(scheduler: SchedulerProvider, authorization: String,
                                    responseData: (ResProfileEditData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable {
        return iUserRequest.profileEdit(authorization = authorization)
                .with(scheduler = scheduler)
                .subscribe({
                    responseData(it)
                }, {
                    errorData(errorHandle<ResMessageErrorData>(it))
                })
    }

    override fun requestProfileUpdate(scheduler: SchedulerProvider, authorization: String, userId: String, requestBody: RequestBody,
                                      responseData: (ResMessageData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable {
        return iUserRequest.profileUpdate(authorization = authorization, userId = userId, requestBody = requestBody)
                .with(scheduler = scheduler)
                .subscribe({
                    responseData(it)
                }, {
                    errorData(errorHandle<ResMessageErrorData>(it))
                })
    }

    override fun requestRecommendUser(scheduler: SchedulerProvider, authorization: String, page: Int,
                                      responseData: (ResUserDefaultData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable {
        return iUserRequest.getRecommendUser(authorization = authorization, page = page)
                .with(scheduler = scheduler)
                .subscribe({
                    responseData(it)
                }, {
                    errorData(errorHandle<ResMessageErrorData>(it))
                })
    }

    override fun requestSetting(scheduler: SchedulerProvider, authorization: String,
                                responseData: (ResSettingData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable {
        return iUserRequest.getSetting(authorization = authorization)
                .with(scheduler = scheduler)
                .subscribe({
                    responseData(it)
                }, {
                    errorData(errorHandle<ResMessageErrorData>(it))
                })
    }

    override fun requestPushUpdate(scheduler: SchedulerProvider, authorization: String,
                                   responseData: (ResSettingPushData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable {
        return iUserRequest.pushSetting(authorization = authorization)
                .with(scheduler = scheduler)
                .subscribe({
                    responseData(it)
                }, {
                    errorData(errorHandle<ResMessageErrorData>(it))
                })
    }

    override fun requestSearchUser(scheduler: SchedulerProvider, authorization: String, searchText: String, page: Int,
                                   responseData: (ResUserDefaultData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable {
        return iUserRequest.userSearch(authorization = authorization, searchText = searchText, page = page)
                .with(scheduler = scheduler)
                .subscribe({
                    responseData(it)
                }, {
                    errorData(errorHandle<ResMessageErrorData>(it))
                })
    }

    override fun requestGoodsUsedUser(scheduler: SchedulerProvider, goodsCode: String, page: Int,
                                      responseData: (ResGoodsUsedUserData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable {
        return iUserRequest.getGoodsUsedUser(goodsCode = goodsCode, page = page)
                .with(scheduler = scheduler)
                .subscribe({
                    responseData(it)
                }, {
                    errorData(errorHandle<ResMessageErrorData>(it))
                })
    }
}