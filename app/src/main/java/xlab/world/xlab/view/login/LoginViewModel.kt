package xlab.world.xlab.view.login

import android.arch.lifecycle.MutableLiveData
import io.reactivex.Observable
import xlab.world.xlab.data.request.ReqLoginData
import xlab.world.xlab.data.response.ResCheckValidTokenData
import xlab.world.xlab.data.response.ResUserLoginData
import xlab.world.xlab.server.provider.ApiUserProvider
import xlab.world.xlab.utils.rx.SchedulerProvider
import xlab.world.xlab.utils.rx.with
import xlab.world.xlab.utils.support.*
import xlab.world.xlab.view.AbstractViewModel
import xlab.world.xlab.view.SingleLiveEvent
import java.net.HttpURLConnection

class LoginViewModel(private val apiUser: ApiUserProvider,
                     private val networkCheck: NetworkCheck,
                     private val scheduler: SchedulerProvider): AbstractViewModel() {

    val tag = "Login"

    val requestLoginByAccessTokenEvent = SingleLiveEvent<RequestLoginByAccessTokenEvent>()
    val generateTokenEvent = SingleLiveEvent<GenerateTokenEvent>()
    val requestLoginEvent = SingleLiveEvent<RequestLoginEvent>()
    val uiData = MutableLiveData<UIModel>()


    // access token 로그인 시도
    fun requestLoginByAccessToken(authorization: String, fcmToken: String = "") {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = MessageConstants.CHECK_NETWORK_CONNECT))
            return
        }

        uiData.value = UIModel(isLoading = true)
        launch {
            // access token 만료 확인
            apiUser.checkValidToken(scheduler = scheduler, authorization = authorization, fcmToken = fcmToken,
                    responseData = { loginData -> // 만료 안된경우 -> 로그인데이터 받아옴
                        PrintLog.d("checkValidToken success", loginData.toString(), tag)
                        requestLoginByAccessTokenEvent.postValue(RequestLoginByAccessTokenEvent(loginData = loginData))
                        uiData.value = UIModel(isLoading = false)
                    }, errorData = { errorData ->
                uiData.value = UIModel(isLoading = false)
                errorData?.let {
                    val errorMessage = errorData.message.split(ApiCallBackConstants.DELIMITER_CHARACTER)
                    PrintLog.d("checkValidToken fail", errorMessage.toString(), tag)
                    if (errorMessage.size > 1) {
                        if (errorMessage[1] == ApiCallBackConstants.TOKEN_EXPIRE) // 만료 에러 callback message
                            requestLoginByAccessTokenEvent.postValue(RequestLoginByAccessTokenEvent(isExpireToken = true))
                        else
                            requestLoginByAccessTokenEvent.postValue(RequestLoginByAccessTokenEvent(isExpireToken = false))
                    } else {
                        requestLoginByAccessTokenEvent.postValue(RequestLoginByAccessTokenEvent(isExpireToken = false))
                    }
                }
            })
        }
    }
    // 토큰 갱신
    fun generateNewToken(authorization: String) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.value = UIModel(toastMessage = MessageConstants.CHECK_NETWORK_CONNECT)
            return
        }
        uiData.value = UIModel(isLoading = true)
        launch {
            // refresh token 받기
            apiUser.getRefreshToken(scheduler = scheduler, authorization = authorization,
                    responseData = { refreshTokenData ->
                        // refresh token 으로 new access token 발행 요청
                        apiUser.generateToken(scheduler = scheduler, authorization = refreshTokenData.refreshToken,
                                responseData = { newTokenData ->
                                    PrintLog.d("generateToken success", newTokenData.accessToken, tag)
                                    uiData.value = UIModel(isLoading = false)
                                    generateTokenEvent.postValue(GenerateTokenEvent(newAccessToken = newTokenData.accessToken))
                                },
                                errorData = { errorData ->
                                    errorData?.let {
                                        PrintLog.d("generateToken fail", errorData.message, tag)
                                    }
                                    uiData.value = UIModel(isLoading = false)
                                    generateTokenEvent.postValue(GenerateTokenEvent(isFailGenerateToken = true))
                                })
                    },
                    errorData = { errorData ->
                        errorData?.let {
                            PrintLog.d("getRefreshToken fail", errorData.message, tag)
                        }
                        uiData.value = UIModel(isLoading = false)
                        generateTokenEvent.postValue(GenerateTokenEvent(isFailGenerateToken = true))
                    })
        }
    }
    // 로그인 요청
    fun requestLogin(loginType: Int, email: String = "", password: String = "", socialToken: String = "", fcmToken: String = "") {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.value = UIModel(toastMessage = MessageConstants.CHECK_NETWORK_CONNECT)
            return
        }
        if (loginType == AppConstants.LOCAL_LOGIN) { // 로컬 로그인 요청일 경우 -> 이메일 정규식 확인
            if (!DataRegex.emailRegex(email)) {
                uiData.value = UIModel(toastMessage = MessageConstants.LOGIN_WRONG_EMAIL_PATTERN)
                return
            }
        }

        uiData.value = UIModel(isLoading = true)
        launch {
            // request login api
            val reqLoginData = ReqLoginData(loginType = loginType, email = email, password = password, socialToken = socialToken, fcmToken = fcmToken)
            apiUser.requestLogin(scheduler = scheduler, reqLoginData = reqLoginData,
                    responseData = { loginData ->
                        loginData.needRegisterSocial = loginData.message != ApiCallBackConstants.SUCCESS_LOGIN
                        PrintLog.d("requestLogin success", loginData.toString(), tag)
                        requestLoginEvent.postValue(RequestLoginEvent(loginData = loginData))
                        uiData.value = UIModel(isLoading = false)
                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(isLoading = false)
                        requestLoginEvent.postValue(RequestLoginEvent(isLoginFail = true))
                        errorData?.let {
                            PrintLog.d("requestLogin fail", errorData.message, tag)
                            if (errorData.errorCode == HttpURLConnection.HTTP_BAD_REQUEST)
                                uiData.value = UIModel(toastMessage = errorData.message)
                        }
            })
        }
    }

    // 로그인 버튼 활성화
    fun isLoginEnable(email: String, password: String) {
        launch {
            Observable.create<Boolean> {
                it.onNext(email.isNotEmpty() && password.isNotEmpty())
                it.onComplete()
            }.with(scheduler).subscribe{ isEnable -> uiData.value = UIModel(isLoginBtnEnable = isEnable) }
        }
    }
}

data class RequestLoginByAccessTokenEvent(val loginData: ResCheckValidTokenData? = null, val isExpireToken: Boolean? = null)
data class GenerateTokenEvent(val newAccessToken: String? = null, val isFailGenerateToken: Boolean? = null)
data class RequestLoginEvent(val loginData: ResUserLoginData? = null, val isLoginFail: Boolean? = null)
data class UIModel(val isLoading: Boolean? = null, val toastMessage: String? = null, val isLoginBtnEnable: Boolean? = null)