package xlab.world.xlab.view.login

import android.arch.lifecycle.MutableLiveData
import xlab.world.xlab.data.request.ReqLoginData
import xlab.world.xlab.server.ApiUser
import xlab.world.xlab.utils.PrintLog
import xlab.world.xlab.utils.rx.SchedulerProvider
import xlab.world.xlab.utils.rx.with
import xlab.world.xlab.view.AbstractViewModel
import xlab.world.xlab.view.SingleLiveEvent

class LoginViewModel(private val scheduler: SchedulerProvider): AbstractViewModel() {
    val loginEvent = SingleLiveEvent<LoginEvent>()
    val uiData = MutableLiveData<LoginUIModel>()

    fun requestLogin(apiUser: ApiUser, email: String, password: String) {
//        uiData.value = LoginUIModel(loginEmail = email, loginPassword = password)
        loginEvent.value = LoginEvent(isLoading = true)
        launch {
            // request login api
            val reqLoginData = ReqLoginData(
                    type = 0,
                    email = email,
                    password = password,
                    fcmToken = "")
            apiUser.requestLogin(scheduler = scheduler, reqLoginData = reqLoginData)
                    .with(scheduler)
                    .subscribe({
                        loginEvent.postValue(LoginEvent(isSuccess = true))
                    }, { err ->
                        loginEvent.postValue(LoginEvent(error = err))
                    })
        }
    }
}

data class LoginEvent(val isLoading: Boolean = false, val isSuccess: Boolean = false, val error: Throwable? = null)
data class LoginUIModel(val toastMessage: String? = null)