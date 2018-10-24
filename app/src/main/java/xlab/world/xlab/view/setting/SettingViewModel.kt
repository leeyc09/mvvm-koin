package xlab.world.xlab.view.setting

import android.app.Activity
import android.arch.lifecycle.MutableLiveData
import android.view.View
import xlab.world.xlab.server.provider.ApiUserProvider
import xlab.world.xlab.utils.rx.SchedulerProvider
import xlab.world.xlab.utils.support.*
import xlab.world.xlab.view.AbstractViewModel
import xlab.world.xlab.view.SingleLiveEvent

class SettingViewModel(private val apiUser: ApiUserProvider,
                       private val networkCheck: NetworkCheck,
                       private val scheduler: SchedulerProvider): AbstractViewModel() {
    private val viewModelTag = "Setting"

    private var resultCode = Activity.RESULT_CANCELED

    val logoutData = SingleLiveEvent<Boolean?>()
    val uiData = MutableLiveData<UIModel>()

    fun setResultCode(resultCode: Int) {
        this.resultCode = SupportData.setResultCode(oldResultCode = this.resultCode, newResultCode = resultCode)
    }

    fun loadUserSettingData(userType: Int, authorization: String) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = networkCheck.networkErrorMsg))
            return
        }

        uiData.value = UIModel(isLoading = true)
        launch {
            apiUser.requestSetting(scheduler = scheduler, authorization = authorization,
                    responseData = {
                        PrintLog.d("requestSetting success", it.toString(), viewModelTag)
                        // 로컬 로그인 유저 -> 비밀번호 변경 비활성화
                        uiData.value = UIModel(isLoading = false,
                                passwordVisibility = if (userType == AppConstants.LOCAL_LOGIN) View.VISIBLE else View.GONE,
                                pushAlarm = it.setting.push)
                    },
                    errorData = { errorData ->
                        errorData?.let {
                            PrintLog.e("requestSetting fail", errorData.message, viewModelTag)
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
                            PrintLog.e("requestPushUpdate fail", errorData.message, viewModelTag)
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
                        this.resultCode = ResultCodeData.LOGOUT_SUCCESS
                        logoutData.value = true
                        uiData.value = UIModel(isLoading = false)
                    },
                    errorData = { errorData ->
                        errorData?.let {
                            PrintLog.e("requestLogout fail", errorData.message, viewModelTag)
                        }
                        uiData.value = UIModel(isLoading = false)
                    })
        }
    }

    fun backBtnAction() {
        uiData.postValue(UIModel(resultCode = resultCode))
    }
}

data class UIModel(val isLoading: Boolean? = null, val toastMessage: String? = null, val resultCode: Int? = null,
                   val passwordVisibility: Int? = null,
                   val pushAlarm: Boolean? = null)