package xlab.world.xlab.view.preload

import android.arch.lifecycle.MutableLiveData
import android.util.Patterns
import io.reactivex.Observable
import xlab.world.xlab.R
import xlab.world.xlab.data.request.ReqLoginData
import xlab.world.xlab.data.response.ResCheckValidTokenData
import xlab.world.xlab.data.response.ResLoginData
import xlab.world.xlab.server.provider.ApiUserProvider
import xlab.world.xlab.utils.rx.SchedulerProvider
import xlab.world.xlab.utils.rx.with
import xlab.world.xlab.utils.support.*
import xlab.world.xlab.view.AbstractViewModel
import xlab.world.xlab.view.SingleLiveEvent
import java.net.HttpURLConnection

class PreloadViewModel(private val apiUser: ApiUserProvider,
                       private val networkCheck: NetworkCheck,
                       private val scheduler: SchedulerProvider): AbstractViewModel() {

    val checkValidTokenEvent = SingleLiveEvent<CheckValidTokenEvent>()
    val uiData = MutableLiveData<UIModel>()

    fun checkValidToken(authorization: String, fcmToken: String = "") {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.value = UIModel(toastMessage = MessageConstants.CHECK_NETWORK_CONNECT)
            return
        }

        uiData.value = UIModel(isLoading = true)
        launch {
            // access token 만료 확인
            apiUser.checkValidToken(scheduler = scheduler, authorization = authorization, fcmToken = fcmToken,
                    responseData = { loginData -> // 만료 안된경우 -> 로그인데이터 받아옴
                        checkValidTokenEvent.postValue(CheckValidTokenEvent(loginData = loginData))
                        uiData.value = UIModel(isLoading = false)
                    }, errorData = { errorData ->
                uiData.value = UIModel(isLoading = false)
                errorData?.let {
                    val errorMessage = errorData.message.split(ApiCallBackConstants.DELIMITER_CHARACTER)
                    if (errorMessage.size > 1) {
                        if (errorMessage[1] == ApiCallBackConstants.TOKEN_EXPIRE) // 만료 에러 callback message
                            checkValidTokenEvent.postValue(CheckValidTokenEvent(isExpireToken = true))
                        else
                            checkValidTokenEvent.postValue(CheckValidTokenEvent(isExpireToken = false))
                    } else {
                        checkValidTokenEvent.postValue(CheckValidTokenEvent(isExpireToken = false))
                    }
                }
            })
        }
    }
}

data class CheckValidTokenEvent(val loginData: ResCheckValidTokenData? = null, val isExpireToken: Boolean? = null)
data class UIModel(val isLoading: Boolean? = null, val toastMessage: String? = null)