package xlab.world.xlab.view.register

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.res.ResourcesCompat
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import android.widget.TextView
import kotlinx.android.synthetic.main.action_bar_default.*
import kotlinx.android.synthetic.main.activity_local_register.*
import org.koin.android.architecture.ext.viewModel
import org.koin.android.ext.android.inject
import xlab.world.xlab.R
import xlab.world.xlab.utils.support.*
import xlab.world.xlab.utils.view.dialog.DefaultProgressDialog
import xlab.world.xlab.utils.view.toast.DefaultToast

class LocalRegisterActivity : AppCompatActivity(), View.OnClickListener, View.OnTouchListener {
    private val registerViewModel: RegisterViewModel by viewModel()
    private val viewFunction: ViewFunction by inject()
    private val letterOrDigitInputFilter: LetterOrDigitInputFilter by inject()
    private val runActivity: RunActivity by inject()

    private var resultCode = Activity.RESULT_CANCELED

    private lateinit var defaultToast: DefaultToast
    private lateinit var progressDialog: DefaultProgressDialog

    private val clickPolicy1 = object: ClickableSpan() {
        override fun onClick(widget: View?) {
            runActivity.defaultWebViewActivity(context = this@LocalRegisterActivity, pageTitle = getString(R.string.inquiry_under3),
                    webUrl =  AppConstants.LOCAL_HTML_URL + "clause.html", zoomControl = true)
        }

        override fun updateDrawState(ds: TextPaint) {
            super.updateDrawState(ds)
            ds.color = ResourcesCompat.getColor(resources, R.color.color6D6D6D, null)
        }
    }
    private val clickPolicy2 = object: ClickableSpan() {
        override fun onClick(widget: View?) {
            runActivity.defaultWebViewActivity(context = this@LocalRegisterActivity, pageTitle = getString(R.string.inquiry_under2),
                    webUrl =  AppConstants.LOCAL_HTML_URL + "personalInfo.html", zoomControl = true)
        }

        override fun updateDrawState(ds: TextPaint) {
            super.updateDrawState(ds)
            ds.color = ResourcesCompat.getColor(resources, R.color.color6D6D6D, null)
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_local_register)

        onSetup()

        onBindEvent()

        observeViewModel()
    }

    override fun onPause() {
        // 엑티비티 멈출때 키보드 숨기
        viewFunction.hideKeyboard(context = this, view = mainLayout)
        super.onPause()
    }

    override fun onBackPressed() {
        actionBackBtn.performClick()
    }

    private fun onSetup() {
        // Toast, Dialog 초기화
        defaultToast = DefaultToast(context = this)
        progressDialog = DefaultProgressDialog(context = this)

        actionBarTitle.visibility = View.GONE
        actionBtn.visibility = View.GONE

        // 약관텍스트 터치 가능하게
        textViewAgreement.movementMethod = LinkMovementMethod.getInstance()

        // 닉네임 숫자 or 문자만 가능하게
        editTextNick.filters = arrayOf(letterOrDigitInputFilter)

        registerViewModel.contentTextSet(policy1 = clickPolicy1, policy2 = clickPolicy2)
    }

    private fun onBindEvent() {
        actionBackBtn.setOnClickListener(this) // 뒤로가기
        finishBtn.setOnClickListener(this) // 회원가입 완료 버튼
        editTextMail.setOnTouchListener(this) // 메일 지우기
        editTextNick.setOnTouchListener(this) // 닉네임 지우기
        scrollView.setOnTouchListener(this) // 키보드 숨기기

        // 키보드 보일때만 완료 버튼 보이기
        viewFunction.showUpKeyboardLayout(mainLayout) { visibility ->
            layoutPopUp.visibility = visibility
        }

        // 이메일, 닉네임 지우기 이미지 활성화 이벤트
        viewFunction.onFocusChange(editText = editTextMail) { hasFocus ->
            editTextMail.setCompoundDrawablesWithIntrinsicBounds(0,0,
                    if (hasFocus) R.drawable.textdelete_black
                    else 0,0)
        }
        viewFunction.onFocusChange(editText = editTextNick) { hasFocus ->
            editTextNick.setCompoundDrawablesWithIntrinsicBounds(0,0,
                    if (hasFocus) R.drawable.textdelete_black
                    else 0,0)
        }

        // 이메일, 패스워드, 닉네임 입력 이벤트
        viewFunction.onTextChange(editText = editTextMail) { _ ->
            registerViewModel.emailRegexCheck(email = getEmailText())
            registerViewModel.inputDataRegex(email = getEmailText(), password = getPasswordText(), nickName = getNickNameText())
        }
        viewFunction.onTextChange(editText = editTextPassword) { _ ->
            registerViewModel.passwordRegexCheck(password = getPasswordText())
            registerViewModel.inputDataRegex(email = getEmailText(), password = getPasswordText(), nickName = getNickNameText())
        }
        viewFunction.onTextChange(editText = editTextNick) { _ ->
            registerViewModel.nickNameRegexCheck(nickName = getNickNameText())
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
                uiData.agreementStr?.let {
                    textViewAgreement.setText(it, TextView.BufferType.SPANNABLE)
                }
                uiData.emailRegexText?.let {
                    textViewConfirmMail.setText(it, TextView.BufferType.SPANNABLE)
                }
                uiData.nickNameRegexText?.let {
                    textViewConfirmNick.setText(it, TextView.BufferType.SPANNABLE)
                }
                uiData.isRegisterBtnEnable?.let {
                    finishBtn.isEnabled = it
                }
                uiData.emailRegex?.let {
                    if (it)
                        confirmMailLayout.visibility = View.INVISIBLE
                    else
                        confirmMailLayout.visibility = View.VISIBLE
                }
                uiData.passwordLengthRegex?.let {
                    confirmPasswordLayout.visibility = View.VISIBLE
                    textViewConfirmPasswordLength.isSelected = it
                }
                uiData.passwordTextRegex?.let {
                    confirmPasswordLayout.visibility = View.VISIBLE
                    textViewConfirmPasswordText.isSelected = it
                }
                uiData.nickNameRegex?.let {
                    confirmNickLayout.visibility = View.VISIBLE
                    textViewConfirmNick.isSelected = it
                }
            }
        })

        // 회원가입 이벤트
        registerViewModel.requestRegisterEvent.observe(owner = this, observer = android.arch.lifecycle.Observer { requestRegisterEvent ->
            requestRegisterEvent?.let { _ ->
                requestRegisterEvent.accessToken?.let { // 회원가입 성공
                    resultCode = Activity.RESULT_OK
                    intent.putExtra(IntentPassName.ACCESS_TOKEN, it)
                    actionBackBtn.performClick()
                }
            }
        })
    }

    override fun onClick(v: View?) {
        v?.let {
            when (v.id) {
                R.id.actionBackBtn -> { // 뒤로가기
                    setResult(resultCode, intent)
                    finish()
                }
                R.id.finishBtn -> { // 회원가입 완료 버튼
                    viewFunction.hideKeyboard(context = this, view = v)
                    registerViewModel.requestRegister(loginType = AppConstants.LOCAL_LOGIN, email = getEmailText(),
                            password = getPasswordText(), nickName = getNickNameText(), socialId = "")
                }
            }
        }
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        v?.let {
            when (v.id) {
                R.id.editTextMail,
                R.id.editTextNick -> { // 메일, 닉네임 지우기
                    viewFunction.onDrawableTouch(v as EditText, event!!) { isTouch ->
                        if (isTouch) {
                            v.setText("")
                            v.performClick()
                        }
                    }
                }
//                R.id.scrollView -> { // 키보드 숨기기
//                    viewFunction.hideKeyboard(context = this, view = v)
//                }
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
