package xlab.world.xlab.view.login

import android.arch.lifecycle.MutableLiveData
import android.util.Patterns
import io.reactivex.Observable
import xlab.world.xlab.R
import xlab.world.xlab.data.request.ReqLoginData
import xlab.world.xlab.data.response.ResLoginData
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
    val requestLoginEvent = SingleLiveEvent<RequestLoginEvent>()
    val socialLoginEvent = SingleLiveEvent<SocialLoginEvent>()
    val uiData = MutableLiveData<UIModel>()

    // 로그인 요청
    fun requestLogin(loginType: Int, email: String = "", password: String = "", socialToken: String = "", fcmToken: String = "") {
        if (loginType == AppConstants.LOCAL_LOGIN) { // 로컬 로그인 요청일 경우 -> 이메일 정규식 확인
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                uiData.value = UIModel(toastMessage = MessageConstants.LOGIN_WRONG_EMAIL_PATTERN)
                return
            }
        }
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.value = UIModel(toastMessage = MessageConstants.CHECK_NETWORK_CONNECT)
            return
        }

        uiData.value = UIModel(isLoading = true)
        launch {
            // request login api
            val reqLoginData = ReqLoginData(type = loginType, email = email, password = password, socialToken = socialToken, fcmToken = fcmToken)
            apiUser.requestLogin(scheduler = scheduler, reqLoginData = reqLoginData,
                    responseData = { loginData ->
                        requestLoginEvent.postValue(RequestLoginEvent(loginData = loginData))
                        uiData.value = UIModel(isLoading = false)
                    }, errorData = { errorData ->
                uiData.value = UIModel(isLoading = false)
                errorData?.let {
                    if (errorData.errorCode == HttpURLConnection.HTTP_BAD_REQUEST)
                        uiData.value = UIModel(toastMessage = errorData.message)
                }
            })
        }
    }
    // 페이스북 로그인 요청
    fun requestFacebookLogin(socialAuth: SocialAuth) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.value = UIModel(toastMessage = MessageConstants.CHECK_NETWORK_CONNECT)
            return
        }

        uiData.value = UIModel(isLoading = true)
        launch {
            Observable.create<String> {
                // 페이스북 토큰 요청
                socialAuth.getFacebookToken( { facebookToken ->
                    it.onNext(facebookToken)
                    it.onComplete()
                }, { errorMsg ->
                    it.tryOnError(Throwable(errorMsg))
                })
            }.with(scheduler).subscribe ({ facebookToken ->
                socialLoginEvent.value = SocialLoginEvent(facebookToken = facebookToken)
                uiData.value = UIModel(isLoading = false)
            }, { error ->
                socialAuth.facebookLogout()
                uiData.value = UIModel(isLoading = false, toastMessage = error.message)
            })
        }
    }
    // 카카오 로그인 요청
    fun requestKakaoLogin(socialAuth: SocialAuth) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.value = UIModel(toastMessage = MessageConstants.CHECK_NETWORK_CONNECT)
            return
        }

        uiData.value = UIModel(isLoading = true)
        launch {
           Observable.create<String> {
                // 카카오 토큰 요청
                socialAuth.getKakaoToken( { kakaoToken ->
                    it.onNext(kakaoToken)
                    it.onComplete()
                }, { errorMsg ->
                    it.tryOnError(Throwable(errorMsg))
                })
            }.with(scheduler).subscribe ({ kakaoToken ->
                socialLoginEvent.value = SocialLoginEvent(kakaoToken = kakaoToken)
                uiData.value = UIModel(isLoading = false)
            }, { error ->
                socialAuth.kakaoLogout()
                uiData.value = UIModel(isLoading = false, toastMessage = error.message)
            })
        }
    }
    // 로그인 버튼 활성화
    fun isLoginEnable(email: String, password: String) {
        launch {
            Observable.create<Boolean> {
                it.onNext(email.isNotEmpty() && password.isNotEmpty())
                it.onComplete()
            }.with(scheduler).subscribe{ isEnable -> uiData.value = UIModel(isLoginEnable = isEnable) }
        }
    }
}

data class RequestLoginEvent(val loginData: ResLoginData? = null)
data class SocialLoginEvent(val facebookToken: String? = null, val kakaoToken: String? = null)
data class UIModel(val isLoading: Boolean? = null, val toastMessage: String? = null, val isLoginEnable: Boolean? = null)