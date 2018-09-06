package xlab.world.xlab.view.register

import android.arch.lifecycle.MutableLiveData
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ClickableSpan
import io.reactivex.Observable
import xlab.world.xlab.R
import xlab.world.xlab.data.request.ReqRegisterData
import xlab.world.xlab.server.provider.ApiUserProvider
import xlab.world.xlab.utils.rx.SchedulerProvider
import xlab.world.xlab.utils.rx.with
import xlab.world.xlab.utils.support.*
import xlab.world.xlab.view.AbstractViewModel
import xlab.world.xlab.view.SingleLiveEvent

class RegisterViewModel(private val apiUser: ApiUserProvider,
                        private val dataRegex: DataRegex,
                        private val networkCheck: NetworkCheck,
                        private val scheduler: SchedulerProvider): AbstractViewModel() {
    private val tag = "Register"

    val requestRegisterEvent = SingleLiveEvent<RequestRegisterEvent>()
    val uiData = MutableLiveData<UIModel>()

    // 약관 내용 터 업데이트 이벤트
    fun contentTextSet(policy1: ClickableSpan, policy2: ClickableSpan) {
        launch {
            Observable.create<SpannableString> {
                val agreementStr = SpannableString(MessageConstants.REGISTER_AGREEMENT)
                agreementStr.setSpan(policy1, 5, 9, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                agreementStr.setSpan(policy2, 12, 21, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

                it.onNext(agreementStr)
                it.onComplete()
            }.with(scheduler).subscribe { agreementStr ->
                uiData.value = UIModel(agreementStr = agreementStr)
            }
        }
    }

    // 가입 요청 정보 정규식 체크
    fun inputDataRegex(email: String = "", password: String = "", nickName: String = "") {
        launch {
            Observable.create<Boolean> {
                it.onNext(dataRegex.emailRegex(email) &&
                        dataRegex.passwordLengthRegex(password)&&
                        dataRegex.passwordTextRegex(password)&&
                        dataRegex.nickNameRegex(nickName))
                it.onComplete()
            }.with(scheduler).subscribe { isEnable ->
                PrintLog.d("inputDataRegex", isEnable.toString(), tag)
                uiData.value = UIModel(isRegisterBtnEnable = isEnable)
            }
        }
    }

    // 이메일 체크
    fun emailRegexCheck(email: String) {
        launch {
            Observable.create<Boolean> {
                val emailRegex = dataRegex.emailRegex(email)
                val emailRegexText =
                        if (!emailRegex) MessageConstants.EMAIL_REGEX_TEXT
                        else null

                uiData.postValue(UIModel(emailRegexText = emailRegexText))

                it.onNext(emailRegex)
                it.onComplete()
            }.with(scheduler).subscribe { isEnable ->
                PrintLog.d("emailRegexCheck", isEnable.toString(), tag)
                uiData.value = UIModel(emailRegex = isEnable)
            }
        }
    }

    // 비밀번호 체크
    fun passwordRegexCheck(password: String) {
        launch {
            Observable.create<ArrayList<Boolean>> {
                it.onNext(arrayListOf(dataRegex.passwordLengthRegex(password), dataRegex.passwordTextRegex(password)))
                it.onComplete()
            }.with(scheduler).subscribe { isEnable ->
                PrintLog.d("emailRegexCheck", isEnable.toString(), tag)
                uiData.value = UIModel(passwordLengthRegex = isEnable[0], passwordTextRegex = isEnable[1])
            }
        }
    }

    // 닉네임 체크
    fun nickNameRegexCheck(nickName: String) {
        launch {
            Observable.create<Boolean> {
                val nickNameRegex = dataRegex.nickNameRegex(nickName)
                val nickNameRegexText =
                        if (!nickNameRegex) MessageConstants.NICK_LENGTH_REGEX_TEXT
                        else null

                uiData.postValue(UIModel(nickNameRegexText = nickNameRegexText))

                it.onNext(nickNameRegex)
                it.onComplete()
            }.with(scheduler).subscribe { isEnable ->
                PrintLog.d("emailRegexCheck", isEnable.toString(), tag)
                uiData.value = UIModel(nickNameRegex = isEnable)
            }
        }
    }

    // 회원 가입 요청
    fun requestRegister(loginType: Int, email: String, password: String, nickName: String, socialId: String) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.value = UIModel(toastMessage = MessageConstants.CHECK_NETWORK_CONNECT)
            return
        }

        uiData.value = UIModel(isLoading = true)
        val reqRegisterData = ReqRegisterData(loginType = loginType, email = email, password = password, nickName = nickName, socialID = socialId)
        apiUser.requestRegister(scheduler = scheduler, reqRegisterData = reqRegisterData,
                responseData = { _ ->
                    PrintLog.d("requestRegister", "success", tag)
                    requestRegisterEvent.postValue(RequestRegisterEvent(registerSuccess = true))
                    uiData.value = UIModel(isLoading = false)
                },
                errorData = { errorData ->
                    uiData.value = UIModel(isLoading = false, isRegisterBtnEnable = false)
                    errorData?.let {
                        val errorMessage = errorData.message.split(ApiCallBackConstants.DELIMITER_CHARACTER)
                        PrintLog.d("requestRegister fail", errorData.message, tag)
                        if (errorMessage.size > 1) {
                            // 존재하는 유저 정보 에러 메세지
                            if(errorMessage[1].contains(ApiCallBackConstants.EXIST_USER_DATA)) {
                                val duplicateError = errorMessage[1].replace(ApiCallBackConstants.EXIST_USER_DATA, "").trim().split("__")
                                if (duplicateError.size > 1) { // 이메일, 닉네임 중복
                                    uiData.postValue(UIModel(emailRegex = false, nickNameRegex = false,
                                            emailRegexText = MessageConstants.DUPLICATE_EMAIL, nickNameRegexText = MessageConstants.DUPLICATE_NICK))
                                } else { // 이메일, 닉네임 둘 중 하나 중복
                                    when (duplicateError[0].trim()) {
                                        ApiCallBackConstants.EMAIL_DUPLICATE_CHAR -> { // 이메일 중복
                                            uiData.postValue(UIModel(emailRegex = false, emailRegexText = MessageConstants.DUPLICATE_EMAIL))
                                        }
                                        ApiCallBackConstants.NICK_NAME_DUPLICATE_CHAR -> { // 닉네임 중복
                                            uiData.postValue(UIModel(nickNameRegex = false, nickNameRegexText = MessageConstants.DUPLICATE_NICK))
                                        }
                                    }
                                }
                            }
                        } else {
                        }
                    }
                })
    }
}

data class RequestRegisterEvent(val registerSuccess: Boolean? = null)
data class UIModel(val isLoading: Boolean? = null, val toastMessage: String? = null,
                   val agreementStr: SpannableString? = null, val isRegisterBtnEnable: Boolean? = null,
                   val emailRegex: Boolean? = null, val passwordLengthRegex: Boolean? = null,
                   val passwordTextRegex: Boolean? = null, val nickNameRegex: Boolean? = null,
                   val emailRegexText: String? = null, val nickNameRegexText: String? = null)