package xlab.world.xlab.view.register

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import android.widget.TextView
import kotlinx.android.synthetic.main.action_bar_default.*
import kotlinx.android.synthetic.main.activity_local_register.*
import org.koin.android.architecture.ext.viewModel
import org.koin.android.ext.android.inject
import xlab.world.xlab.R
import xlab.world.xlab.utils.listener.DefaultListener
import xlab.world.xlab.utils.support.*
import xlab.world.xlab.utils.view.dialog.DefaultProgressDialog
import xlab.world.xlab.utils.view.toast.DefaultToast

class LocalRegisterActivity : AppCompatActivity(), View.OnClickListener, View.OnTouchListener {
    private val registerViewModel: RegisterViewModel by viewModel()
    private val letterOrDigitInputFilter: LetterOrDigitInputFilter by inject()

    private lateinit var defaultToast: DefaultToast
    private lateinit var progressDialog: DefaultProgressDialog

    private lateinit var defaultListener: DefaultListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_local_register)

        onSetup()

        onBindEvent()

        observeViewModel()
    }

    override fun onPause() {
        // 엑티비티 멈출때 키보드 숨기기
        ViewFunction.hideKeyboard(context = this, view = mainLayout)
        super.onPause()
    }

    override fun onBackPressed() {
        actionBackBtn.performClick()
    }

    private fun onSetup() {
        // 타이틀, 확인 버튼 비활성화
        actionBarTitle.visibility = View.GONE
        actionBtn.visibility = View.GONE

        // 약관 텍스트 터치 가능하게
        textViewAgreement.movementMethod = LinkMovementMethod.getInstance()

        // 닉네임 숫자 or 문자만 가능하게
        editTextNick.filters = arrayOf(letterOrDigitInputFilter)

        // Toast, Dialog 초기화
        defaultToast = DefaultToast(context = this)
        progressDialog = DefaultProgressDialog(context = this)

        // Listener 초기화
        defaultListener = DefaultListener(context = this)

        registerViewModel.contentTextSet(context = this, policy1 = defaultListener.clausePolicyListener, policy2 = defaultListener.personalInfoPolicyListener)
        registerViewModel.inputDataRegex(email = getEmailText(), password = getPasswordText(), nickName = getNickNameText())
    }

    private fun onBindEvent() {
        actionBackBtn.setOnClickListener(this) // 뒤로가기
        finishBtn.setOnClickListener(this) // 회원가입 완료 버튼
        editTextMail.setOnTouchListener(this) // 메일 지우기
        editTextNick.setOnTouchListener(this) // 닉네임 지우기
        scrollView.setOnTouchListener(this) // 키보드 숨기기

        // 키보드 보일때만 완료 버튼 보이기
        ViewFunction.showUpKeyboardLayout(view = mainLayout) { visibility ->
            layoutPopUp.visibility = visibility
        }

        // 이메일, 비밀번호, 닉네임 포커스 변경 이벤트
        ViewFunction.onFocusChange(editText = editTextMail) { hasFocus ->
            editTextMail.setCompoundDrawablesWithIntrinsicBounds(0,0,
                    if (hasFocus) R.drawable.textdelete_black
                    else 0,0)
        }
        ViewFunction.onFocusChange(editText = editTextPassword) { hasFocus ->
            registerViewModel.passwordRegexVisibility(hasFocus = hasFocus,
                    password = getPasswordText())
        }
        ViewFunction.onFocusChange(editText = editTextNick) { hasFocus ->
            editTextNick.setCompoundDrawablesWithIntrinsicBounds(0,0,
                    if (hasFocus) R.drawable.textdelete_black
                    else 0,0)

            registerViewModel.nickNameRegexVisibility(hasFocus = hasFocus,
                    nickName = getNickNameText())
        }

        // 이메일, 패스워드, 닉네임 입력 이벤트
        ViewFunction.onTextChange(editText = editTextMail) { _ ->
            registerViewModel.emailRegexCheck(context = this, email = getEmailText())
            registerViewModel.inputDataRegex(email = getEmailText(), password = getPasswordText(), nickName = getNickNameText())
        }
        ViewFunction.onTextChange(editText = editTextPassword) { _ ->
            registerViewModel.passwordRegexCheck(password = getPasswordText())
            registerViewModel.inputDataRegex(email = getEmailText(), password = getPasswordText(), nickName = getNickNameText())
        }
        ViewFunction.onTextChange(editText = editTextNick) { _ ->
            registerViewModel.nickNameRegexCheck(context = this, nickName = getNickNameText())
            registerViewModel.inputDataRegex(email = getEmailText(), password = getPasswordText(), nickName = getNickNameText())
        }
    }

    private fun observeViewModel() {
        // UI 이벤트 observe
        registerViewModel.uiData.observe(this, android.arch.lifecycle.Observer { uiData ->
            uiData?.let { _ ->
                uiData.isLoading?.let {
                    if (it && !progressDialog.isShowing)
                        progressDialog.show()
                    else if (!it && progressDialog.isShowing)
                        progressDialog.dismiss()
                }
                uiData.toastMessage?.let {
                    defaultToast.showToast(message = it)
                }
                uiData.resultCode?.let {
                    setResult(it, intent)
                    finish()
                }
                uiData.agreementStr?.let {
                    textViewAgreement.setText(it, TextView.BufferType.SPANNABLE)
                }
                uiData.registerBtnEnable?.let {
                    finishBtn.isEnabled = it
                }
                uiData.emailRegexVisibility?.let {
                    confirmMailLayout.visibility = it
                }
                uiData.emailRegexText?.let {
                    textViewConfirmMail.setText(it, TextView.BufferType.SPANNABLE)
                }
                uiData.passwordRegexVisibility?.let {
                    confirmPasswordLayout.visibility = it
                }
                uiData.passwordLengthRegex?.let {
                    textViewConfirmPasswordLength.isSelected = it
                }
                uiData.passwordTextRegex?.let {
                    textViewConfirmPasswordText.isSelected = it
                }
                uiData.nickNameRegexVisibility?.let {
                    confirmNickLayout.visibility = it
                }
                uiData.nickNameRegexText?.let {
                    textViewConfirmNick.setText(it, TextView.BufferType.SPANNABLE)
                }
                uiData.nickNameRegex?.let {
                    textViewConfirmNick.isSelected = it
                }
            }
        })

        // 회원가입 이벤트
        registerViewModel.registerData.observe(owner = this, observer = android.arch.lifecycle.Observer { eventData ->
            eventData?.let { accessToken -> // 회원가입 성공
                intent.putExtra(IntentPassName.ACCESS_TOKEN, accessToken)
                actionBackBtn.performClick()
            }
        })
    }

    override fun onClick(v: View?) {
        v?.let {
            when (v.id) {
                R.id.actionBackBtn -> { // 뒤로가기
                    registerViewModel.backBtnAction()
                }
                R.id.finishBtn -> { // 회원가입 완료 버튼
                    ViewFunction.hideKeyboard(context = this, view = v)
                    registerViewModel.requestRegister(context = this,
                            loginType = AppConstants.LOCAL_LOGIN,
                            email = getEmailText(),
                            password = getPasswordText(),
                            nickName = getNickNameText())
                }
            }
        }
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        v?.let {
            when (v.id) {
                R.id.editTextMail,
                R.id.editTextNick -> { // 메일, 닉네임 지우기
                    ViewFunction.onDrawableTouch(v as EditText, event!!) { isTouch ->
                        if (isTouch) {
                            v.setText("")
                            v.performClick()
                        }
                    }
                }
                R.id.scrollView -> { // 키보드 숨기기
                    ViewFunction.hideKeyboard(context = this, view = v)
                }
            }
        }
        return false
    }

    private fun getEmailText() = editTextMail.text?.trim().toString()
    private fun getPasswordText() = editTextPassword.text?.trim().toString()
    private fun getNickNameText() = editTextNick.text?.trim().toString()

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, LocalRegisterActivity::class.java)
        }
    }
}
