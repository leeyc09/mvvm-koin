package xlab.world.xlab.view.preload

import android.arch.lifecycle.MutableLiveData
import xlab.world.xlab.data.response.ResCheckValidTokenData
import xlab.world.xlab.server.provider.ApiUserProvider
import xlab.world.xlab.utils.rx.SchedulerProvider
import xlab.world.xlab.utils.support.*
import xlab.world.xlab.view.AbstractViewModel
import xlab.world.xlab.view.SingleLiveEvent

class PreloadViewModel(private val apiUser: ApiUserProvider,
                       private val networkCheck: NetworkCheck,
                       private val scheduler: SchedulerProvider): AbstractViewModel() {

    val checkValidTokenEvent = SingleLiveEvent<CheckValidTokenEvent>()
    val generateTokenEvent = SingleLiveEvent<GenerateTokenEvent>()
    val uiData = MutableLiveData<UIModel>()

    // access token 만료 확인
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
            apiUser.getRefreshToken(scheduler = scheduler, authorization = authorization, responseData = { refreshTokenData ->
                // refresh token 으로 new access token 발행 요청
                apiUser.generateToken(scheduler = scheduler, authorization = refreshTokenData.refreshToken, responseData = { newTokenData ->
                    uiData.value = UIModel(isLoading = false)
                    generateTokenEvent.postValue(GenerateTokenEvent(newAccessToken = newTokenData.accessToken))
                }, errorData = {
                    uiData.value = UIModel(isLoading = false)
                    generateTokenEvent.postValue(GenerateTokenEvent(isFailGenerateToken = true))
                })
            }, errorData = {
                uiData.value = UIModel(isLoading = false)
                generateTokenEvent.postValue(GenerateTokenEvent(isFailGenerateToken = true))
            })
        }
    }


}

data class CheckValidTokenEvent(val loginData: ResCheckValidTokenData? = null, val isExpireToken: Boolean? = null)
data class GenerateTokenEvent(val newAccessToken: String? = null, val isFailGenerateToken: Boolean? = null)
data class UIModel(val isLoading: Boolean? = null, val toastMessage: String? = null)