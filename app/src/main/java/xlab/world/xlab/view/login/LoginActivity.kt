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
import xlab.world.xlab.utils.support.*
import xlab.world.xlab.utils.support.AppConstants.FACEBOOK_LOGIN
import xlab.world.xlab.utils.support.AppConstants.KAKAO_LOGIN
import xlab.world.xlab.utils.support.AppConstants.LOCAL_LOGIN
import xlab.world.xlab.utils.view.dialog.DefaultProgressDialog
import xlab.world.xlab.utils.view.toast.DefaultToast
import xlab.world.xlab.utils.support.IntentPassName
import xlab.world.xlab.utils.view.dialog.ShopAccountDialog

class LoginActivity : AppCompatActivity(), View.OnClickListener, View.OnTouchListener {
    private val loginViewModel: LoginViewModel by viewModel()
    private val spHelper: SPHelper by inject()

    private val socialAuth: SocialAuth = SocialAuth()

    private lateinit var defaultToast: DefaultToast

    private lateinit var progressDialog: DefaultProgressDialog
    private lateinit var shopAccountDialog: ShopAccountDialog

    private val shopAccountListener = object: ShopAccountDialog.Listener {
        override fun webViewFinish(result: Boolean) {
            if (result) {
                loginViewModel.finishView()
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        PrintLog.d("resultCode", resultCode.toString(), this::class.java.name)
        PrintLog.d("requestCode", requestCode.toString(), this::class.java.name)

        when (resultCode) {
            Activity.RESULT_OK -> {
                when (requestCode) {
                    RequestCodeData.REGISTER_USER -> { // 회원가입 성공
                        data?.let {
                            rootLayout.visibility = View.INVISIBLE
                            val accessToken = data.getStringExtra(IntentPassName.ACCESS_TOKEN)
                            loginViewModel.requestLoginByAccessToken(authorization = "Bearer $accessToken", fcmToken = spHelper.fcmToken)
                        }
                    }
                }
            }
            Activity.RESULT_CANCELED -> {
                when (requestCode) {
                    RequestCodeData.REGISTER_USER -> { // 회원가입 취소
                        // 페이스북, 카카오 로그아웃
                        socialAuth.facebookLogout()
                        socialAuth.kakaoLogout()
                    }
                }
            }
        }

        // Call facebook login callback manager
        if (socialAuth.facebookCallbackManager != null)
            socialAuth.facebookCallbackManager!!.onActivityResult(requestCode, resultCode, data)
        // Call kakao login callback manager
        if (Session.getCurrentSession().handleActivityResult(requestCode, resultCode, data)) {
            return
        }
    }

    private fun onSetup() {
        // 타이틀, 확인 버튼 비활성화
        actionBarTitle.visibility = View.GONE
        actionBtn.visibility = View.GONE

        // 페이스북 email 읽기 권한 추가
        originFacebookBtn.setReadPermissions("email")

        // Toast, Dialog 초기화
        defaultToast = DefaultToast(context = this)
        progressDialog = DefaultProgressDialog(context = this)
        shopAccountDialog = ShopAccountDialog(context = this, listener = shopAccountListener)

        // 페이스북, 카카오 로그아웃
        socialAuth.facebookLogout()
        socialAuth.kakaoLogout()

        loginViewModel.initData(isComePreLoadActivity = intent.getBooleanExtra(IntentPassName.IS_COME_PRELOAD_ACTIVITY, true),
                linkData = intent.data)
        loginViewModel.isLoginEnable(email = getEmailText(), password = getPasswordText())
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
        ViewFunction.showUpKeyboardLayout(view = mainLayout) { visibility ->
            loginViewModel.keyboardLayoutEvent(keyBoardVisibility = visibility)
        }
        // 이메일, 패스워드 지우기 이미지 활성화 이벤트
        ViewFunction.onFocusChange(editText = editTextMail) { hasFocus ->
            editTextMail.setCompoundDrawablesWithIntrinsicBounds(0,0,
                    if (hasFocus) R.drawable.textdelete_black
                    else 0,0)
        }
        ViewFunction.onFocusChange(editText = editTextPassword) { hasFocus ->
            editTextPassword.setCompoundDrawablesWithIntrinsicBounds(0,0,
                    if (hasFocus) R.drawable.textdelete_black
                    else 0,0)
        }

        // 이메일, 패스워드 입력 이벤트
        ViewFunction.onTextChange(editText = editTextMail) { _ ->
            loginViewModel.isLoginEnable(email = getEmailText(), password = getPasswordText())
        }
        ViewFunction.onTextChange(editText = editTextPassword) { _ ->
            loginViewModel.isLoginEnable(email = getEmailText(), password = getPasswordText())
        }

        // 패스워드 입력시 앤터 누르면 로그인 이벤트
        ViewFunction.onKeyboardActionTouch(editText = editTextPassword, putActionID = EditorInfo.IME_ACTION_DONE) { isTouch ->
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
                uiData.resultCode?.let {
                    setResult(it)
                    finish()
                }
                uiData.backBtnVisibility?.let {
                    actionBackBtn.visibility = it
                }
                uiData.guestBtnVisibility?.let {
                    guestBtn.visibility = it
                }
                uiData.registerLayoutVisibility?.let {
                    registerLayout.visibility = it
                }
                uiData.popupVisibility?.let {
                    layoutPopUp.visibility = it
                }
                uiData.loginBtnEnable?.let {
                    loginBtn.isEnabled = it
                }
            }
        })

        // access token 로그인 시도 이벤트 observe
        loginViewModel.loginByAccessTokenData.observe(owner = this, observer = android.arch.lifecycle.Observer { checkValidTokenEvent ->
            checkValidTokenEvent?.let { _ ->
                checkValidTokenEvent.loginData?.let { loginData -> // 로그인 성공
                    successLogin(accessToken = loginData.accessToken,
                            userType = loginData.loginType,
                            userId = loginData.userID,
                            socialId = loginData.socialID,
                            userLevel = loginData.userLevel,
                            userEmail = loginData.email,
                            push = loginData.isPushAlarmOn)
                }
            }
        })

        // 로그인 이벤트 observe
        loginViewModel.loginData.observe(owner = this, observer = android.arch.lifecycle.Observer { eventData ->
            eventData?.let { _ ->
                eventData.loginData?.let { loginData ->
                    if (loginData.needRegisterSocial) { // 소셜 회원가입 필요
                        RunActivity.socialRegisterActivity(context = this, userData = loginData)
                    } else {
                        successLogin(accessToken = loginData.accessToken,
                                userType = loginData.loginType,
                                userId = loginData.userID,
                                socialId = loginData.socialID,
                                userLevel = loginData.userLevel,
                                userEmail = loginData.email,
                                push = loginData.isPushAlarmOn)
                    }
                }
                eventData.isLoginFail?.let {
                    if (it) {
                        socialAuth.facebookLogout()
                        socialAuth.kakaoLogout()
                    }
                }
            }
        })

        // 화면 전환 이벤트 observe
        loginViewModel.finishViewData.observe(owner = this, observer = android.arch.lifecycle.Observer { eventData ->
            eventData?.let {_->
                eventData.runMainActivity?.let {
                    RunActivity.mainActivity(context = this, linkData = null)
                    finish()
                }
                eventData.runGuestMode?.let {
                    guestBtn.performClick()
                }
            }
        })
    }

    override fun onClick(v: View?) {
        v?.let {
            when (v.id) {
                R.id.actionBackBtn -> { // 뒤로가기
                    loginViewModel.backBtnAction()
                }
                R.id.facebookBtn -> { // 페이스북 로그인 버튼
                    originFacebookBtn.performClick()
                    // 페이스북 토큰 요청
                    socialAuth.getFacebookToken({ facebookToken ->
                        loginViewModel.requestLogin(context = this,
                                loginType = FACEBOOK_LOGIN,
                                socialToken = facebookToken,
                                fcmToken = spHelper.fcmToken)
                    }, { _ ->
                        socialAuth.facebookLogout()
                    })
                }
                R.id.kakaoBtn -> { // 카카오 로그인 버튼
                    originKakaoBtn.performClick()
                    // 카카오 토큰 요청
                    socialAuth.getKakaoToken({ kakaoToken ->
                        loginViewModel.requestLogin(context = this,
                                loginType = KAKAO_LOGIN,
                                socialToken = kakaoToken,
                                fcmToken = spHelper.fcmToken)
                    }, { _ ->
                        socialAuth.kakaoLogout()
                    })
                }
                R.id.registerBtn -> { // 회원가입 버튼
                    // clear mail and password
                    editTextMail?.setText("")
                    editTextPassword?.setText("")
                    RunActivity.localRegisterActivity(context = this)
                }
                R.id.guestBtn -> { // 둘러보기(게스트 모드)
                    RunActivity.mainActivity(context = this, linkData = intent.data)
                    finish()
                }
                R.id.loginBtn -> { // 로컬 로그인 버튼
                    loginViewModel.requestLogin(context = this,
                            loginType = LOCAL_LOGIN,
                            email = getEmailText(),
                            password = getPasswordText(),
                            fcmToken = spHelper.fcmToken)
                }
                R.id.passwordResetBtn -> { // 비밀번호 재설정
                    RunActivity.resetPasswordActivity(context = this, email = getEmailText())
                }
            }
        }
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        v?.let {
            when (v.id) {
                R.id.editTextMail, // 이메일, 패스워드 텍스트 지우기
                R.id.editTextPassword -> {
                    ViewFunction.onDrawableTouch(v as EditText, event!!) { isTouch ->
                        if (isTouch) {
                            v.setText("")
                            v.performClick()
                        }
                    }
                }
                R.id.mainLayout -> { // 키보드 숨기기
                    ViewFunction.hideKeyboard(context = this, view = v)
                }
            }
        }
        return false
    }

    private fun successLogin(accessToken: String, userType: Int, userId: String, socialId: String,
                             userLevel: Int, userEmail: String, push: Boolean) {
        spHelper.login(accessToken = accessToken,
                userType = userType,
                userId = userId,
                socialId = socialId,
                userLevel = userLevel,
                userEmail = userEmail,
                push = push)

        shopAccountDialog.requestLogin(userId = userId)
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
