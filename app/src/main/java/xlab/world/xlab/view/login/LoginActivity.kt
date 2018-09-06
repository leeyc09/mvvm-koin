package xlab.world.xlab.view.login

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import com.kakao.auth.Session
import kotlinx.android.synthetic.main.action_bar_default.*
import kotlinx.android.synthetic.main.activity_login.*
import org.koin.android.architecture.ext.viewModel
import org.koin.android.ext.android.inject
import xlab.world.xlab.R
import xlab.world.xlab.utils.rx.argument
import xlab.world.xlab.utils.support.*
import xlab.world.xlab.utils.support.AppConstants.FACEBOOK_LOGIN
import xlab.world.xlab.utils.support.AppConstants.KAKAO_LOGIN
import xlab.world.xlab.utils.support.AppConstants.LOCAL_LOGIN
import xlab.world.xlab.utils.view.dialog.DefaultProgressDialog
import xlab.world.xlab.utils.view.toast.DefaultToast
import xlab.world.xlab.view.IntentPassName
import xlab.world.xlab.view.register.LocalRegisterActivity

class LoginActivity : AppCompatActivity(), View.OnClickListener, View.OnTouchListener {
    private val loginViewModel: LoginViewModel by viewModel()
    private val socialAuth: SocialAuth by inject()
    private val viewFunction: ViewFunction by inject()
    private val spHelper: SPHelper by inject()

    private val isComePreLoadActivity: Boolean? by argument(IntentPassName.IS_COME_PRELOAD_ACTIVITY, true)
    private var linkData: Uri? = null

    private lateinit var defaultToast: DefaultToast
    private lateinit var progressDialog: DefaultProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        onSetup()

        onBindEvent()

        observeViewModel()
    }

    override fun onBackPressed() {
        actionBackBtn.performClick()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Call facebook login callback manager
        if (socialAuth.facebookCallbackManager != null)
            socialAuth.facebookCallbackManager!!.onActivityResult(requestCode, resultCode, data)
        // Call kakao login callback manager
        if (Session.getCurrentSession().handleActivityResult(requestCode, resultCode, data)) {
            return
        }
    }

    private fun onSetup() {
        linkData = intent.data

        if (isComePreLoadActivity == null) {
            actionBackBtn.visibility = View.INVISIBLE
            guestBtn.visibility = View.VISIBLE
        } else {
            // 앱 실행으로 로그인 화면으로 온 경우 -> 뒤로가기 비활성화, 둘러보기 활성화
            // 다른 화면에서 로그인 화면으로 온 경우 -> 뒤로가기 버튼 활성화, 둘러보기 비활성화
            actionBackBtn.visibility =
                    if (isComePreLoadActivity!!) View.INVISIBLE
                    else View.VISIBLE

            guestBtn.visibility =
                    if (isComePreLoadActivity!!) View.VISIBLE
                    else View.INVISIBLE
        }
        // 타이틀, 확인 버튼 비활성화
        actionBarTitle.visibility = View.GONE
        actionBtn.visibility = View.GONE

        // Toast, Dialog 초기화
        defaultToast = DefaultToast(context = this)
        progressDialog = DefaultProgressDialog(context = this)

        // 페이스북 email 읽기 권한 추가
        originFacebookBtn.setReadPermissions("email")

        // 페이스북, 카카오 로그아웃
        socialAuth.facebookLogout()
        socialAuth.kakaoLogout()
    }

    private fun onBindEvent() {
        actionBackBtn.setOnClickListener(this) // 뒤로가기
        facebookBtn.setOnClickListener(this) // 페이스북 로그인 버튼
        kakaoBtn.setOnClickListener(this) // 카카오 로그인 버튼
        registerBtn.setOnClickListener(this) // 회원가입 버튼
        guestBtn.setOnClickListener(this) // 둘러보기(게스트 모드)
        loginBtn.setOnClickListener(this) // 로컬 로그인 버튼
        passwordResetBtn.setOnClickListener(this) // 비밀번호 재설정

        editTextMail.setOnTouchListener(this) // 이메일 텍스트 지우기
        editTextPassword.setOnTouchListener(this) // 패스워드 텍스트 지우기
        mainLayout.setOnTouchListener(this) // 키보드 숨기기

        // 키보드 보일경우 비밀번호 변경, 로그인 버튼 활성화 이벤트
        viewFunction.showUpKeyboardLayout(view = mainLayout) { visibility ->
            registerLayout.visibility =
                    if (visibility == View.VISIBLE) View.INVISIBLE
                    else View.VISIBLE
            layoutPopUp.visibility = visibility
        }
        // 이메일, 패스워드 지우기 이미지 활성화 이벤트
        viewFunction.onFocusChange(editText = editTextMail) { hasFocus ->
            editTextMail.setCompoundDrawablesWithIntrinsicBounds(0,0,
                    if (hasFocus) R.drawable.textdelete_black
                    else 0,0)
        }
        viewFunction.onFocusChange(editText = editTextPassword) { hasFocus ->
            editTextPassword.setCompoundDrawablesWithIntrinsicBounds(0,0,
                    if (hasFocus) R.drawable.textdelete_black
                    else 0,0)
        }

        // 이메일, 패스워드 입력 이벤트
        viewFunction.onTextChange(editText = editTextMail) { _ ->
            loginViewModel.isLoginEnable(email = getEmailText(), password = getPasswordText())
        }
        viewFunction.onTextChange(editText = editTextPassword) { _ ->
            loginViewModel.isLoginEnable(email = getEmailText(), password = getPasswordText())
        }

        // 패스워드 입력시 앤터 누르면 로그인 이벤트
        viewFunction.onKeyboardActionTouch(editText = editTextPassword, putActionID = EditorInfo.IME_ACTION_DONE) { isTouch ->
            if (isTouch && loginBtn.isEnabled)
                loginBtn.performClick()
        }
    }

    private fun observeViewModel() {
        // UI 이벤트 observe
        loginViewModel.uiData.observe(this, android.arch.lifecycle.Observer { uiData ->
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
                uiData.isLoginBtnEnable?.let {
                    loginBtn.isEnabled = it
                }
            }
        })

        // 로그인 이벤트 observe
        loginViewModel.requestLoginEvent.observe(owner = this, observer = android.arch.lifecycle.Observer { requestLoginEvent ->
            requestLoginEvent?.let { _ ->
                requestLoginEvent.loginData?.let { loginData ->
                    spHelper.login(accessToken = loginData.accessToken,
                            userType = loginData.loginType,
                            userId = loginData.userID,
                            socialId = loginData.socialID,
                            userLevel = loginData.userLevel,
                            userEmail = loginData.email,
                            push = loginData.isPushAlarmOn)
                }
            }
        })

        // 소셜 이벤트 observe
        loginViewModel.socialLoginEvent.observe(owner = this, observer = android.arch.lifecycle.Observer { socialLoginEvent ->
            socialLoginEvent?.let { _ ->
                socialLoginEvent.facebookToken?.let { facebookToken ->
                    loginViewModel.requestLogin(loginType = FACEBOOK_LOGIN, socialToken = facebookToken, fcmToken = spHelper.fcmToken)
                }
                socialLoginEvent.kakaoToken?.let { kakaoToken ->
                    loginViewModel.requestLogin(loginType = KAKAO_LOGIN, socialToken = kakaoToken, fcmToken = spHelper.fcmToken)
                }
            }
        })
    }

    override fun onClick(v: View?) {
        v?.let {
            when (v.id) {
                R.id.actionBackBtn -> { // 뒤로가기
                    setResult(Activity.RESULT_CANCELED)
                    finish()
                }
                R.id.facebookBtn -> { // 페이스북 로그인 버튼
                    originFacebookBtn.performClick()
                    loginViewModel.requestFacebookLogin(socialAuth)
                }
                R.id.kakaoBtn -> { // 카카오 로그인 버튼
                    originKakaoBtn.performClick()
                    loginViewModel.requestKakaoLogin(socialAuth)
                }
                R.id.registerBtn -> { // 회원가입 버튼
                    // clear mail and password
                    editTextMail.text?.clear()
                    editTextPassword.text?.clear()
                    // move to register activity
                    val intent = LocalRegisterActivity.newIntent(context = this)
                    startActivityForResult(intent, RequestCodeData.REGISTER_USER)
                }
                R.id.guestBtn -> { // 둘러보기(게스트 모드)
//                    guestLogin()
                }
                R.id.loginBtn -> { // 로컬 로그인 버튼
                    loginViewModel.requestLogin(loginType = LOCAL_LOGIN,
                            email = getEmailText(),
                            password = getPasswordText(),
                            fcmToken = spHelper.fcmToken)
                }
                R.id.passwordResetBtn -> { // 비밀번호 재설정
//                    resetPassword()
                }
            }
        }
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        v?.let {
            when (v.id) {
                R.id.editTextMail, // 이메일, 패스워드 텍스트 지우기
                R.id.editTextPassword -> {
                    viewFunction.onDrawableTouch(v as EditText, event!!) { isTouch ->
                        if (isTouch) {
                            v.setText("")
                            v.performClick()
                        }
                    }
                }
                R.id.mainLayout -> { // 키보드 숨기기
                    viewFunction.hideKeyboard(context = this, view = v)
                }
            }
        }
        return false
    }

    private fun getEmailText() = editTextMail.text?.trim().toString()
    private fun getPasswordText() = editTextPassword.text?.trim().toString()

    companion object {
        fun newIntent(context: Context, isComePreLoadActivity: Boolean, linkData: Uri?): Intent {
            val intent = Intent(context, LoginActivity::class.java)
            intent.putExtra(IntentPassName.IS_COME_PRELOAD_ACTIVITY, isComePreLoadActivity)
            intent.data = linkData

            return intent
        }
    }
}
