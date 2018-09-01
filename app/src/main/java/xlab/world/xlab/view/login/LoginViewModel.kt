package xlab.world.xlab.view.login

import android.arch.lifecycle.MutableLiveData
import xlab.world.xlab.data.request.ReqLoginData
import xlab.world.xlab.data.response.ResErrorData
import xlab.world.xlab.server.provider.ApiUserProvider
import xlab.world.xlab.utils.rx.SchedulerProvider
import xlab.world.xlab.utils.rx.with
import xlab.world.xlab.utils.support.SocialAuth
import xlab.world.xlab.view.AbstractViewModel
import xlab.world.xlab.view.SingleLiveEvent
import xlab.world.xlab.server.errorHandle
import java.net.HttpURLConnection


class LoginViewModel(private val apiUser: ApiUserProvider,
                     private val socialAuth: SocialAuth,
                     private val scheduler: SchedulerProvider): AbstractViewModel() {
    val requestLoginEvent = SingleLiveEvent<RequestLoginEvent>()
    val socialLoginEvent = SingleLiveEvent<SocialLoginEvent>()
    val uiData = MutableLiveData<UIModel>()

    fun requestLogin(loginType: Int, email: String = "", password: String = "", socialToken: String = "") {
        uiData.value = UIModel(isLoading = true)
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
        // 페이스북 토큰 요청
        socialAuth.getFacebookToken( { facebookToken ->
            socialLoginEvent.value = SocialLoginEvent(facebookToken = facebookToken)
            uiData.value = UIModel(isLoading = false)
        }, { errorMsg ->
            socialAuth.facebookLogout()
            uiData.value = UIModel(isLoading = false, toastMessage = errorMsg)
        })
    }
    fun requestKakaoLogin() {
        uiData.value = UIModel(isLoading = true)
        // 카카오 토큰 요청
        socialAuth.getKakaoToken( { kakaoToken ->
            socialLoginEvent.value = SocialLoginEvent(kakaoToken = kakaoToken)
            uiData.value = UIModel(isLoading = false)
        }, { errorMsg ->
            socialAuth.kakaoLogout()
            uiData.value = UIModel(isLoading = false, toastMessage = errorMsg)
        })
    }
}

data class RequestLoginEvent(val successLogin: Boolean? = null)
data class SocialLoginEvent(val facebookToken: String? = null, val kakaoToken: String? = null)
data class UIModel(val isLoading: Boolean? = null, val toastMessage: String? = null)