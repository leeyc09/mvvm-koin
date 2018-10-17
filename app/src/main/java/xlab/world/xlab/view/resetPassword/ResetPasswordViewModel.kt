package xlab.world.xlab.view.resetPassword

import android.arch.lifecycle.MutableLiveData
import android.content.Context
import io.reactivex.Flowable
import xlab.world.xlab.R
import xlab.world.xlab.data.request.ReqConfirmEmailData
import xlab.world.xlab.data.request.ReqPasswordData
import xlab.world.xlab.server.provider.ApiUserProvider
import xlab.world.xlab.utils.rx.SchedulerProvider
import xlab.world.xlab.utils.support.*
import xlab.world.xlab.view.AbstractViewModel
import xlab.world.xlab.view.SingleLiveEvent
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong

class ResetPasswordViewModel(private val apiUser: ApiUserProvider,
                             private val networkCheck: NetworkCheck,
                             private val scheduler: SchedulerProvider): AbstractViewModel() {
    val tag = "ResetPassword"

    val requestConfirmEmailEvent = SingleLiveEvent<ResetPasswordEvent>()
    val requestConfirmCodeEvent = SingleLiveEvent<RequestConfirmCodeEvent>()
    val requestConfirmPasswrodEvent = SingleLiveEvent<ResetPasswordEvent>()
    val requestChangePasswordEvent = SingleLiveEvent<ResetPasswordEvent>()
    val uiData = MutableLiveData<UIModel>()

    // 인증코드 타이머
    private val timerSec: Long = 600L
    private var elapsedTime = AtomicLong()
    private var stopped = AtomicBoolean()

    // 메일 인증 시도
    fun requestConfirmEmail(context: Context, email: String) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = networkCheck.networkErrorMsg))
            return
        }
        if (!DataRegex.emailRegex(email)) { // 이메일 정규식 확인
            uiData.postValue(UIModel(toastMessage = context.getString(R.string.toast_email_format_wrong)))
            return
        }

        uiData.value = UIModel(isLoading = true)
        launch {
            val reqConfirmEmailData = ReqConfirmEmailData(email = email, code = "")
            apiUser.requestConfirmEmail(scheduler = scheduler, reqConfirmEmailData = reqConfirmEmailData,
                    responseData = {
                        uiData.value = UIModel(isLoading = false)
                        requestConfirmEmailEvent.postValue(ResetPasswordEvent(status = true))
                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(isLoading = false)
                        requestConfirmEmailEvent.postValue(ResetPasswordEvent(status = false))
                        errorData?.let {
                            val errorMessage = errorData.message.split(ApiCallBackConstants.DELIMITER_CHARACTER)
                            PrintLog.d("requestConfirmEmail fail", errorMessage.toString())
                            if (errorMessage.size > 1)
                                uiData.value = UIModel(toastMessage = errorMessage[1])
                        }
                    })
        }
    }
    // 인증코드 타이머 시작
    fun startTimer() {
        PrintLog.d("timer", "start")
        launch {
            stopped.set(false)
            Flowable.interval(0, 1, TimeUnit.SECONDS)
                    .takeWhile { !stopped.get() && it + 1 < timerSec }
                    .map { elapsedTime.addAndGet(1) }
                    .subscribe({
                        val leftSec = timerSec - it
                        val min = leftSec / 60
                        val minStr =
                                if (min > 9) min.toString()
                                else "0$min"
                        val sec = leftSec % 60
                        val secStr =
                                if (sec > 9) sec.toString()
                                else "0$sec"

                        uiData.value = UIModel(timerText = "$minStr:$secStr")
                    }, {
                        PrintLog.d("timer error", it.message!!)
                        uiData.value = UIModel(isEndTimer = true)
                    }, {
                        PrintLog.d("timer complete", "")
                        if (!stopped.get())
                            uiData.value = UIModel(isEndTimer = true)
                    })
        }
    }
    // 인증코드 타이머 종료
    fun stopTimer() {
        PrintLog.d("timer", "stop")
        stopped.set(true)
        elapsedTime.addAndGet(-elapsedTime.get())
    }
    // 인증코드 확인 요청
    fun requestConfirmCode(email: String, code: String) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.value = UIModel(toastMessage = networkCheck.networkErrorMsg)
            return
        }
        uiData.value = UIModel(isLoading = true)
        launch {
            val reqConfirmEmailData = ReqConfirmEmailData(email = email, code = code)
            apiUser.requestConfirmEmailCode(scheduler = scheduler, reqConfirmEmailData = reqConfirmEmailData,
                    responseData = {
                        PrintLog.d("requestConfirmEmail success", it.accessToken)
                        uiData.value = UIModel(isLoading = false)
                        requestConfirmCodeEvent.postValue(RequestConfirmCodeEvent(accessToken = it.accessToken))
                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(isLoading = false)
                        requestConfirmCodeEvent.postValue(RequestConfirmCodeEvent(accessToken = ""))
                        errorData?.let {
                            val errorMessage = errorData.message.split(ApiCallBackConstants.DELIMITER_CHARACTER)
                            PrintLog.d("requestConfirmEmail fail", errorMessage.toString())
                            if (errorMessage.size > 1)
                                uiData.value = UIModel(toastMessage = errorMessage[1])
                        }
                    })
        }
    }
    // 비밀번호 체크 요쳥
    fun requestConfirmPassword(context: Context, authorization: String, password: String) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = networkCheck.networkErrorMsg))
            return
        }

        uiData.value = UIModel(isLoading = true)
        launch {
            val reqPasswordData = ReqPasswordData(password = password)
            apiUser.requestConfirmPassword(scheduler = scheduler, authorization = authorization, reqPasswordData = reqPasswordData,
                    responseData = {
                        uiData.value = UIModel(isLoading = false)
                        requestConfirmPasswrodEvent.postValue(ResetPasswordEvent(status = true))
                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(isLoading = false, toastMessage = context.getString(R.string.toast_wrong_password))
                        errorData?.let {
                            PrintLog.d("requestConfirmPassword fail", errorData.message)
                        }
                    })
        }
    }

    // 비빌번호 변경 요청
    fun requestChangePassword(context: Context, authorization: String, password: String) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = networkCheck.networkErrorMsg))
            return
        }

        uiData.value = UIModel(isLoading = true)
        launch {
            val reqNewPasswordData = ReqPasswordData(password = password)
            apiUser.requestChangePassword(scheduler = scheduler, authorization = authorization, reqNewPasswordData = reqNewPasswordData,
                    responseData = {
                        PrintLog.d("requestChangePassword success", "")
                        uiData.value = UIModel(isLoading = false, toastMessage = context.getString(R.string.toast_reset_password_success))
                        requestChangePasswordEvent.postValue(ResetPasswordEvent(status = true))
                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(isLoading = false)
                        errorData?.let {
                            PrintLog.d("requestChangePassword fail", errorData.message)
                            uiData.value = UIModel(toastMessage = errorData.message)
                        }
                    })
        }
    }
}

data class RequestConfirmCodeEvent(val accessToken: String? = null)
data class ResetPasswordEvent(val status: Boolean? = null)
data class UIModel(val isLoading: Boolean? = null, val toastMessage: String? = null,
                   val timerText: String? = null, val isEndTimer: Boolean? = null)