package xlab.world.xlab.view.setting

import android.arch.lifecycle.MutableLiveData
import xlab.world.xlab.server.provider.ApiUserProvider
import xlab.world.xlab.utils.rx.SchedulerProvider
import xlab.world.xlab.utils.support.NetworkCheck
import xlab.world.xlab.utils.support.PrintLog
import xlab.world.xlab.view.AbstractViewModel
import xlab.world.xlab.view.SingleLiveEvent

class SettingViewModel(private val apiUser: ApiUserProvider,
                       private val networkCheck: NetworkCheck,
                       private val scheduler: SchedulerProvider): AbstractViewModel() {
    val tag = "Setting"

    val logoutEventData = SingleLiveEvent<SettingEvent>()
    val uiData = MutableLiveData<UIModel>()

    fun loadUserSettingData(authorization: String) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = networkCheck.networkErrorMsg))
            return
        }

        uiData.value = UIModel(isLoading = true)
        launch {
            apiUser.requestSetting(scheduler = scheduler, authorization = authorization,
                    responseData = {
                        PrintLog.d("requestSetting success", it.toString(), tag)
                        uiData.value = UIModel(isLoading = false, pushAlarm = it.setting.push)
                    },
                    errorData = { errorData ->
                        errorData?.let {
                            PrintLog.d("requestSetting fail", errorData.message, tag)
                        }
                        uiData.value = UIModel(isLoading = false)
                    })
        }
    }

    fun updatePushAlarm(authorization: String) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = networkCheck.networkErrorMsg))
            return
        }

        uiData.value = UIModel(isLoading = true)
        launch {
            apiUser.requestPushUpdate(scheduler = scheduler, authorization = authorization,
                    responseData = {
                        uiData.value = UIModel(isLoading = false, pushAlarm = it.result)
                    },
                    errorData = { errorData ->
                        errorData?.let {
                            PrintLog.d("requestPushUpdate fail", errorData.message, tag)
                        }
                        uiData.value = UIModel(isLoading = false)
                    })
        }
    }

    fun userLogout(authorization: String) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = networkCheck.networkErrorMsg))
            return
        }

        uiData.value = UIModel(isLoading = true)
        launch {
            apiUser.requestLogout(scheduler = scheduler, authorization = authorization,
                    responseData = {
                        logoutEventData.value = SettingEvent(status = true)
                        uiData.value = UIModel(isLoading = false)
                    },
                    errorData = { errorData ->
                        errorData?.let {
                            PrintLog.d("requestLogout fail", errorData.message, tag)
                        }
                        uiData.value = UIModel(isLoading = false)
                    })
        }
    }
}

data class SettingEvent(val status: Boolean? = null)
data class UIModel(val isLoading: Boolean? = null, val toastMessage: String? = null,
                   val pushAlarm: Boolean? = null)