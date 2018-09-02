package xlab.world.xlab.view.login

import android.arch.lifecycle.MutableLiveData
import android.util.Patterns
import io.reactivex.Observable
import xlab.world.xlab.R
import xlab.world.xlab.data.request.ReqLoginData
import xlab.world.xlab.server.provider.ApiUserProvider
import xlab.world.xlab.utils.rx.SchedulerProvider
import xlab.world.xlab.utils.rx.with
import xlab.world.xlab.utils.support.AppConstants
import xlab.world.xlab.utils.support.MessageConstants
import xlab.world.xlab.utils.support.PrintLog
import xlab.world.xlab.utils.support.SocialAuth
import xlab.world.xlab.view.AbstractViewModel
import xlab.world.xlab.view.SingleLiveEvent
import java.net.HttpURLConnection


class LoginViewModel(private val apiUser: ApiUserProvider,
                     private val socialAuth: SocialAuth,
                     private val scheduler: SchedulerProvider): AbstractViewModel() {
    val requestLoginEvent = SingleLiveEvent<RequestLoginEvent>()
    val socialLoginEvent = SingleLiveEvent<SocialLoginEvent>()
    val uiData = MutableLiveData<UIModel>()

    fun requestLogin(loginType: Int, email: String = "", password: String = "", socialToken: String = "") {
        uiData.value = UIModel(isLoading = true)
        if (loginType == AppConstants.LOCAL_LOGIN) { // 로컬 로그인 요청일 경우 -> 이메일 정규식 확인
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                uiData.value = UIModel(isLoading = false, toastMessage = MessageConstants.LOGIN_WRONG_EMAIL_PATTERN)
                return
            }
        }
        launch {
            // request login api
            val reqLoginData = ReqLoginData(type = loginType, email = email, password = password, socialToken = socialToken, fcmToken = "")
            apiUser.requestLogin(scheduler = scheduler, reqLoginData = reqLoginData,
                    responseData = { _ ->
                        requestLoginEvent.postValue(RequestLoginEvent(successLogin = true))
                    }, errorData = { errorData ->
                requestLoginEvent.postValue(RequestLoginEvent(successLogin = false))
                errorData?.let {
                    if (errorData.errorCode == HttpURLConnection.HTTP_BAD_REQUEST)
                        uiData.value = UIModel(isLoading = false, toastMessage = errorData.message)
                }
            })
        }
    }
    fun requestFacebookLogin() {
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
    fun requestKakaoLogin() {
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
    fun isLoginEnable(email: String, password: String) {
        launch {
            Observable.create<Boolean> {
                it.onNext(email.isNotEmpty() && password.isNotEmpty())
                it.onComplete()
            }.with(scheduler).subscribe{ isEnable -> uiData.value = UIModel(isLoginEnable = isEnable) }
        }
    }
}

data class RequestLoginEvent(val successLogin: Boolean? = null)
data class SocialLoginEvent(val facebookToken: String? = null, val kakaoToken: String? = null)
data class UIModel(val isLoading: Boolean? = null, val toastMessage: String? = null, val isLoginEnable: Boolean? = null)