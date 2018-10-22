package xlab.world.xlab.view.register

import android.app.Activity
import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ClickableSpan
import android.view.View
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
                        private val networkCheck: NetworkCheck,
                        private val scheduler: SchedulerProvider): AbstractViewModel() {
    private val viewModelTag = "Register"

    private var resultCode = Activity.RESULT_CANCELED

    val registerData = SingleLiveEvent<String?>()
    val uiData = MutableLiveData<UIModel>()

    // 약관 내용 텍스트 업데이트 이벤트
    fun contentTextSet(context: Context, policy1: ClickableSpan, policy2: ClickableSpan) {
        launch {
            Observable.create<SpannableString> {
                val agreementStr = SpannableString(context.getString(R.string.register_agreement))
                agreementStr.setSpan(policy1, 5, 9, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                agreementStr.setSpan(policy2, 12, 21, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

                it.onNext(agreementStr)
                it.onComplete()
            }.with(scheduler).subscribe {
                uiData.value = UIModel(agreementStr = it)
            }
        }
    }

    // 가입 요청 정보 정규식 체크
    fun inputDataRegex(email: String = "", password: String = "", nickName: String = "") {
        launch {
            Observable.create<Boolean> {
                it.onNext(DataRegex.emailRegex(email) &&
                        DataRegex.passwordLengthRegex(password)&&
                        DataRegex.passwordTextRegex(password)&&
                        DataRegex.nickNameRegex(nickName))
                it.onComplete()
            }.with(scheduler).subscribe {
                PrintLog.d("inputDataRegex", it.toString(), viewModelTag)
                uiData.value = UIModel(registerBtnEnable = it)
            }
        }
    }

    // 이메일 정규식 체크
    fun emailRegexCheck(context: Context, email: String) {
        launch {
            Observable.create<ArrayList<Any?>> {
                val emailRegex = DataRegex.emailRegex(email)
                val emailRegexText =
                        if (!emailRegex) context.getString(R.string.toast_email_format_wrong)
                        else null

                // [0] -> 정규식 결과 텍스트
                // [1] -> 정규식 결과 보이기 & 숨기기 (통과 -> 숨기기 & 실패 -> 보이기)
                it.onNext(arrayListOf(emailRegexText,
                        if (emailRegex) View.INVISIBLE else View.VISIBLE))
                it.onComplete()
            }.with(scheduler).subscribe {
                PrintLog.d("emailRegexCheck", it.toString(), viewModelTag)
                uiData.value = UIModel(emailRegexText = it[0] as String?, emailRegexVisibility = it[1] as Int)
            }
        }
    }

    // 비밀번호 정규식 체크
    fun passwordRegexCheck(password: String) {
        launch {
            Observable.create<ArrayList<Boolean>> {
                // [0] -> 비밀번호 길이 체크 결과
                // [1] -> 비밀번호 텍스트 정규식 체크 결과
                // [2] -> 통합 결과
                it.onNext(arrayListOf(DataRegex.passwordLengthRegex(password),
                        DataRegex.passwordTextRegex(password)))
                it.onComplete()
            }.with(scheduler).subscribe {
                PrintLog.d("passwordRegexCheck", it.toString(), viewModelTag)
                uiData.value = UIModel(passwordLengthRegex = it[0],
                        passwordTextRegex = it[1],
                        passwordRegex = it[0] && it[1],
                        passwordRegexVisibility = View.VISIBLE)
            }
        }
    }

    // 닉네임 정규식 체크
    fun nickNameRegexCheck(context: Context, nickName: String) {
        launch {
            Observable.create<ArrayList<Any>> {
                val nickNameRegex = DataRegex.nickNameRegex(nickName)
                val nickNameRegexText = context.getString(R.string.confirm_nick_length)

                // [0] -> 정규식 알림 텍스트
                // [1] -> 정규식 체크 결과
                it.onNext(arrayListOf(nickNameRegexText, nickNameRegex))
                it.onComplete()
            }.with(scheduler).subscribe {
                PrintLog.d("nickRegexCheck", it.toString(), viewModelTag)
                uiData.value = UIModel(nickNameRegexText = it[0] as String,
                        nickNameRegex = it[1] as Boolean,
                        nickNameRegexVisibility = View.VISIBLE)
            }
        }
    }

    // 회원 가입 요청
    fun requestRegister(context: Context, loginType: Int, email: String, password: String = "",
                        nickName: String, socialId: String = "") {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.value = UIModel(toastMessage = networkCheck.networkErrorMsg)
            return
        }

        uiData.value = UIModel(isLoading = true)
        val reqRegisterData = ReqRegisterData(loginType = loginType, email = email, password = password, nickName = nickName, socialID = socialId)
        apiUser.requestRegister(scheduler = scheduler, reqRegisterData = reqRegisterData,
                responseData = {
                    PrintLog.d("requestRegister success", it.toString(), viewModelTag)
                    resultCode = Activity.RESULT_OK
                    registerData.postValue(it.accessToken)
                    uiData.value = UIModel(isLoading = false)
                },
                errorData = { errorData ->
                    uiData.value = UIModel(isLoading = false, registerBtnEnable = false)
                    errorData?.let {
                        PrintLog.e("requestRegister fail", errorData.message, viewModelTag)
                        errorData.getErrorDetail()?.let { errorDetail ->
                            // 존재하는 유저 정보 에러 메세지
                            if(errorDetail.contains(ApiCallBackConstants.EXIST_USER_DATA)) {
                                val duplicateError = errorDetail.replace(ApiCallBackConstants.EXIST_USER_DATA, "").trim().split("__")
                                if (duplicateError.size > 1) { // 이메일, 닉네임 중복
                                    uiData.value = UIModel(emailRegexVisibility = View.VISIBLE, emailRegexText = context.getString(R.string.used_email),
                                            nickNameRegexVisibility = View.VISIBLE, nickNameRegex = false, nickNameRegexText = context.getString(R.string.used_nick))
                                } else { // 이메일, 닉네임 둘 중 하나 중복
                                    when (duplicateError[0].trim()) {
                                        ApiCallBackConstants.EMAIL_DUPLICATE_CHAR -> { // 이메일 중복
                                            uiData.value = UIModel(emailRegexVisibility = View.VISIBLE, emailRegexText = context.getString(R.string.used_email))
                                        }
                                        ApiCallBackConstants.NICK_NAME_DUPLICATE_CHAR -> { // 닉네임 중복
                                            uiData.value = UIModel(nickNameRegexVisibility = View.VISIBLE, nickNameRegex = false, nickNameRegexText = context.getString(R.string.used_nick))
                                        }
                                    }
                                }
                            }
                        }
                    }
                })
    }

    // 비밀번호 정규식 텍스트 보이기 & 숨기기
    fun passwordRegexVisibility(hasFocus: Boolean, password: String) {
        launch {
            Observable.create<Int> {
                // 비밀번호 입력 필드에 포커스가 사라졌을 경우 & 비밀번호 정규식 (길이, 텍스트) 모두 통과 -> 안보이기
                val passwordRegexVisibility =
                        if (!hasFocus && DataRegex.passwordLengthRegex(password) && DataRegex.passwordTextRegex(password)) View.INVISIBLE
                        else View.VISIBLE
                it.onNext(passwordRegexVisibility)
                it.onComplete()
            }.with(scheduler).subscribe {
                PrintLog.d("passwordRegexVisibility", it.toString(), viewModelTag)
                uiData.value = UIModel(passwordRegexVisibility = it)
            }
        }
    }

    // 닉네임 정규식 텍스트 보이기 & 숨기기
    fun nickNameRegexVisibility(hasFocus: Boolean, nickName: String) {
        launch {
            Observable.create<Int> {
                // 닉네임 입력 필드에 포커스가 사라졌을 경우 & 닉네임 정규식 통과 -> 텍스트 안보이기
                val nickNameRegexVisibility =
                        if (!hasFocus && DataRegex.nickNameRegex(nickName)) View.INVISIBLE
                        else View.VISIBLE
                it.onNext(nickNameRegexVisibility)
                it.onComplete()
            }.with(scheduler).subscribe {
                PrintLog.d("nickNameRegexVisibility", it.toString(), viewModelTag)
                uiData.value = UIModel(nickNameRegexVisibility = it)
            }
        }
    }

    fun actionBackAction() {
        uiData.postValue(UIModel(resultCode = resultCode))
    }
}

data class UIModel(val isLoading: Boolean? = null, val toastMessage: String? = null, val resultCode: Int? = null,
                   val agreementStr: SpannableString? = null, val registerBtnEnable: Boolean? = null,
                   val emailRegexVisibility: Int? = null, val emailRegexText: String? = null,
                   val passwordRegexVisibility: Int? = null, val passwordRegex: Boolean? = null,
                   val passwordLengthRegex: Boolean? = null, val passwordTextRegex: Boolean? = null,
                   val nickNameRegexVisibility: Int? = null, val nickNameRegex: Boolean? = null, val nickNameRegexText: String? = null)