package xlab.world.xlab.view.login

import android.arch.lifecycle.MutableLiveData
import com.google.android.gms.common.api.Api
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Converter
import retrofit2.HttpException
import xlab.world.xlab.data.request.ReqLoginData
import xlab.world.xlab.data.response.ResErrorData
import xlab.world.xlab.server.ApiUserProvider
import xlab.world.xlab.utils.rx.SchedulerProvider
import xlab.world.xlab.utils.rx.with
import xlab.world.xlab.utils.support.PrintLog
import xlab.world.xlab.utils.support.SocialAuth
import xlab.world.xlab.view.AbstractViewModel
import xlab.world.xlab.view.SingleLiveEvent
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Retrofit



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
            apiUser.requestLogin(reqLoginData = ReqLoginData(
                    type = loginType,
                    email = email,
                    password = password,
                    socialToken = socialToken,
                    fcmToken = ""))
                    .with(scheduler)
                    .subscribe({
                        requestLoginEvent.postValue(RequestLoginEvent(successData = true))
                    }, { error ->
                        if (error is HttpException) {
                            PrintLog.d("error", error.code().toString())
                            try {
                                val jsonObject = JSONObject(error.response().errorBody()?.string())
                                val message = jsonObject.getString("message")
                                PrintLog.d("error", message)
                            } catch (e: Exception) {
                                PrintLog.d("Exception", e.message!!)
                            }
                        }
                        requestLoginEvent.postValue(RequestLoginEvent(error = error))
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
}

data class RequestLoginEvent(val successData: Any? = null, val error: Throwable? = null)
data class SocialLoginEvent(val facebookToken: String? = null, val kakaoToken: String? = null)
data class UIModel(val isLoading: Boolean? = null, val toastMessage: String? = null)