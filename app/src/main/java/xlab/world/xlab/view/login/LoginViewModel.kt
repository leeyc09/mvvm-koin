package xlab.world.xlab.view.login

import android.app.Activity
import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.net.Uri
import android.view.View
import io.reactivex.Observable
import xlab.world.xlab.R
import xlab.world.xlab.data.request.ReqLoginData
import xlab.world.xlab.data.response.ResCheckValidTokenData
import xlab.world.xlab.data.response.ResUserLoginData
import xlab.world.xlab.server.provider.ApiUserProvider
import xlab.world.xlab.utils.rx.SchedulerProvider
import xlab.world.xlab.utils.rx.with
import xlab.world.xlab.utils.support.*
import xlab.world.xlab.view.AbstractViewModel
import xlab.world.xlab.view.SingleLiveEvent
import java.io.Serializable
import java.net.HttpURLConnection

class LoginViewModel(private val apiUser: ApiUserProvider,
                     private val networkCheck: NetworkCheck,
                     private val scheduler: SchedulerProvider): AbstractViewModel() {
    private val viewModelTag = "Login"

    private var isComePreLoadActivity: Boolean = true
    private var linkData: Uri? = null

    val loginByAccessTokenData = SingleLiveEvent<LoginByAccessTokenModel>()
    val generateTokenEvent = SingleLiveEvent<GenerateTokenModel>()
    val loginData = SingleLiveEvent<LoginModel>()
    val finishViewData = SingleLiveEvent<FinishModel>()
    val uiData = MutableLiveData<UIModel>()

    // view model data 초기화
    fun initData(isComePreLoadActivity: Boolean, linkData: Uri?) {
        launch {
            Observable.create<ArrayList<Int>> {
                this.isComePreLoadActivity = isComePreLoadActivity
                this.linkData = linkData

                // 앱 실행으로 로그인 화면으로 온 경우 -> 뒤로가기 비활성화, 둘러보기 활성화
                // 다른 화면에서 로그인 화면으로 온 경우 -> 뒤로가기 버튼 활성화, 둘러보기 비활성화
                val visibility = // [0] -> 뒤로가기, [1] -> 둘러보기
                        if (isComePreLoadActivity) arrayListOf(View.INVISIBLE, View.VISIBLE)
                        else arrayListOf(View.VISIBLE, View.INVISIBLE)

                it.onNext(visibility)
                it.onComplete()
            }.with(scheduler = scheduler).subscribe {
                PrintLog.d(title = "initData", log = it.toString(), tag = viewModelTag)
                uiData.value = UIModel(backBtnVisibility = it[0], guestBtnVisibility = it[1])
                finishViewData.value = FinishModel(runGuestMode = this.linkData?.let{_->true}?:let{_->null})
            }
        }
    }

    // 키보드 보이기 유무에 따른 이벤트
    fun keyboardLayoutEvent(keyBoardVisibility: Int) {
        launch {
            Observable.create<ArrayList<Int>> {
                // 키보드 보일경우 회원가입, 둘러보기 비활성화 & 팝업 활성화
                val visibility = // [0] -> 회원가입, 둘러보기, [1] -> 팝업
                        if (keyBoardVisibility == View.VISIBLE) arrayListOf(View.INVISIBLE, keyBoardVisibility)
                        else arrayListOf(View.VISIBLE, keyBoardVisibility)

                it.onNext(visibility)
                it.onComplete()
            }.with(scheduler = scheduler).subscribe {
                PrintLog.d(title = "keyboardLayoutEvent", log = it.toString(), tag = viewModelTag)
                uiData.value = UIModel(registerLayoutVisibility = it[0], popupVisibility = it[1])
            }
        }
    }

    // access token 으로 로그인 시도
    fun requestLoginByAccessToken(authorization: String, fcmToken: String) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = networkCheck.networkErrorMsg))
            return
        }

        uiData.value = UIModel(isLoading = true)
        launch {
            // access token 만료 확인
            apiUser.checkValidToken(scheduler = scheduler, authorization = authorization, fcmToken = fcmToken,
                    responseData = { // 만료 안된 경우 -> 로그인 데이터 받아옴
                        PrintLog.d("checkValidToken success", it.toString(), viewModelTag)
                        loginByAccessTokenData.postValue(LoginByAccessTokenModel(loginData = it))
                        uiData.value = UIModel(isLoading = false)
                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(isLoading = false)
                        errorData?.let {
                            PrintLog.e("checkValidToken fail", errorData.message, viewModelTag)
                            if (errorData.message == ApiCallBackConstants.TOKEN_EXPIRE) // 만료 된 경우 -> refresh token 으로 갱신 시도
                                loginByAccessTokenData.postValue(LoginByAccessTokenModel(isExpireToken = true))
                            else
                                loginByAccessTokenData.postValue(LoginByAccessTokenModel(isExpireToken = false))
                        }
            })
        }
    }

    // 토큰 갱신
    fun generateNewToken(authorization: String) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.value = UIModel(toastMessage = networkCheck.networkErrorMsg)
            return
        }
        uiData.value = UIModel(isLoading = true)
        launch {
            // refresh token 받기
            apiUser.getRefreshToken(scheduler = scheduler, authorization = authorization,
                    responseData = { refreshTokenData ->
                        PrintLog.d("getRefreshToken success", refreshTokenData.refreshToken, viewModelTag)
                        // refresh token 으로 access token 발행 요청
                        apiUser.generateToken(scheduler = scheduler, authorization = "Bearer ${refreshTokenData.refreshToken}",
                                responseData = { newTokenData ->
                                    PrintLog.d("generateToken success", newTokenData.accessToken)
                                    uiData.value = UIModel(isLoading = false)
                                    generateTokenEvent.postValue(GenerateTokenModel(newAccessToken = newTokenData.accessToken))
                                },
                                errorData = { errorData ->
                                    uiData.value = UIModel(isLoading = false)
                                    generateTokenEvent.postValue(GenerateTokenModel(isFailGenerateToken = true))
                                    errorData?.let {
                                        PrintLog.d("generateToken fail", errorData.message)
                                    }
                                })
                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(isLoading = false)
                        generateTokenEvent.postValue(GenerateTokenModel(isFailGenerateToken = true))
                        errorData?.let {
                            PrintLog.e("getRefreshToken fail", errorData.message)
                        }
                    })
        }
    }

    // 로그인 요청
    fun requestLogin(context: Context, loginType: Int, email: String = "", password: String = "",
                     socialToken: String = "", fcmToken: String = "") {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.value = UIModel(toastMessage = networkCheck.networkErrorMsg)
            return
        }

        if (loginType == AppConstants.LOCAL_LOGIN) { // 로컬 로그인 요청일 경우 -> 이메일 정규식 확인
            if (!DataRegex.emailRegex(email)) {
                uiData.value = UIModel(toastMessage = context.getString(R.string.toast_email_format_wrong))
                return
            }
        }

        uiData.value = UIModel(isLoading = true)
        launch {
            // request login api
            val reqLoginData = ReqLoginData(loginType = loginType, email = email, password = password, socialToken = socialToken, fcmToken = fcmToken)
            apiUser.requestLogin(scheduler = scheduler, reqLoginData = reqLoginData,
                    responseData = { loginData ->
                        // response message 가 SUCCESS_LOGIN 아닐 경우 -> social 회원가입 진행 필요
                        loginData.needRegisterSocial = loginData.message != ApiCallBackConstants.SUCCESS_LOGIN
                        PrintLog.d("requestLogin success", loginData.toString(), viewModelTag)
                        this.loginData.postValue(LoginModel(loginData = loginData))
                        uiData.value = UIModel(isLoading = false)
                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(isLoading = false)
                        loginData.postValue(LoginModel(isLoginFail = true))
                        errorData?.let {
                            PrintLog.e("requestLogin fail", errorData.message, viewModelTag)
                            if (errorData.errorCode == HttpURLConnection.HTTP_BAD_REQUEST)
                                uiData.value = UIModel(toastMessage = errorData.getErrorDetail())
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
            }.with(scheduler = scheduler).subscribe{ isEnable -> uiData.value = UIModel(loginBtnEnable = isEnable) }
        }
    }

    // 화면 전환 이벤트
    fun finishView() {
        launch {
            Observable.create<ArrayList<Any?>> {
                // 앱 실행으로 로그인 화면 온 경우 -> 메인 화면으로
                // 다른 화면에서 로그인 화면으로 온 경우 -> 이전 화면으로
                val resultData = arrayListOf(if (isComePreLoadActivity) true else null,
                        if (isComePreLoadActivity) null else ResultCodeData.LOGIN_SUCCESS)

                it.onNext(resultData)
                it.onComplete()
            }.with(scheduler = scheduler).subscribe {
                finishViewData.value = FinishModel(runMainActivity = it[0] as Boolean?)
                uiData.value = UIModel(resultCode = it[1] as Int?)
            }
        }
    }

    fun actionBackAction() {
        uiData.postValue(UIModel(resultCode = Activity.RESULT_CANCELED))
    }
}

data class LoginByAccessTokenModel(val loginData: ResCheckValidTokenData? = null, val isExpireToken: Boolean? = null): Serializable
data class GenerateTokenModel(val newAccessToken: String? = null, val isFailGenerateToken: Boolean? = null)
data class LoginModel(val loginData: ResUserLoginData? = null, val isLoginFail: Boolean? = null)
data class FinishModel(val runMainActivity: Boolean? = null, val runGuestMode: Boolean? = null)
data class UIModel(val isLoading: Boolean? = null, val toastMessage: String? = null, val resultCode: Int? = null,
                   val backBtnVisibility: Int? = null, val guestBtnVisibility: Int? = null,
                   val registerLayoutVisibility: Int? = null, val popupVisibility: Int? = null,
                   val loginBtnEnable: Boolean? = null)