package xlab.world.xlab.view.login

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import com.kakao.auth.Session
import kotlinx.android.synthetic.main.action_bar_default.*
import kotlinx.android.synthetic.main.activity_login.*
import org.koin.android.architecture.ext.viewModel
import org.koin.android.ext.android.inject
import xlab.world.xlab.R
import xlab.world.xlab.utils.support.ViewFunction
import xlab.world.xlab.utils.rx.argument
import xlab.world.xlab.utils.support.AppConstants.FACEBOOK_LOGIN
import xlab.world.xlab.utils.support.AppConstants.LOCAL_LOGIN
import xlab.world.xlab.utils.support.PrintLog
import xlab.world.xlab.utils.support.SocialAuth
import xlab.world.xlab.utils.view.dialog.DefaultProgressDialog
import xlab.world.xlab.utils.view.toast.DefaultToast
import xlab.world.xlab.view.IntentPassName

class LoginActivity : AppCompatActivity(), View.OnClickListener, View.OnTouchListener {

    private val loginViewModel: LoginViewModel by viewModel()
    private val socialAuth: SocialAuth by inject()

    private val isComeOtherActivity: Boolean? by argument(IntentPassName.IS_COME_OTHER_ACTIVITY, true)

    private lateinit var defaultToast: DefaultToast
    private lateinit var progressDialog: DefaultProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        onSetup()

        onBindEvent()

//        loadKoinModules(listOf(remoteModule))

//        btn.setOnClickListener {
//            loginViewModel.requestLogin(apiUser = apiUser, email = getEmailText(), password = getPasswordText())
//        }

        loginViewModel.requestLoginEvent.observe(this, android.arch.lifecycle.Observer { requestLoginEvent ->
            requestLoginEvent?.let { _ ->
                requestLoginEvent.successData?.let { successData ->

                }
                requestLoginEvent.error?.let { error ->

                }
            }
        })

        loginViewModel.socialLoginEvent.observe(this, android.arch.lifecycle.Observer { socialLoginEvent ->
            socialLoginEvent?.let { _ ->
                socialLoginEvent.facebookToken?.let { facebookToken ->
                    loginViewModel.requestLogin(loginType = FACEBOOK_LOGIN, socialToken = facebookToken)
                }
                socialLoginEvent.kakaoToken?.let {
                }
            }
        })

        loginViewModel.uiData.observe(this, android.arch.lifecycle.Observer { uiData ->
            uiData?.let { _ ->
                uiData.isLoading?.let {
                    PrintLog.d("isLoading", it.toString())
                }
                uiData.toastMessage?.let {
                    defaultToast.showToast(it)
                }
            }
        })
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
        // 다른 화면에서 로그인 화면으로 온 경우 -> 뒤로가기 버튼 활성화, 둘러보기 비활성화
        if (isComeOtherActivity == null) {
            actionBackBtn.visibility = View.VISIBLE
            guestBtn.visibility = View.INVISIBLE
        } else {
            if (isComeOtherActivity!!) {
                actionBackBtn.visibility = View.INVISIBLE
                guestBtn.visibility = View.VISIBLE
            }
            else{
                actionBackBtn.visibility = View.VISIBLE
                guestBtn.visibility = View.INVISIBLE
            }
        }
        // 타이틀, 확인 버튼 비활성화
        actionBarTitle.visibility = View.GONE
        actionBtn.visibility = View.GONE

        // Toast, Dialog 초기화
        defaultToast = DefaultToast(this)
        progressDialog = DefaultProgressDialog(this)

        // 페이스북 email 읽기 권한 추가
        originFacebookBtn.setReadPermissions("email")

        // 페이스북, 카카오 로그아웃
        socialAuth.facebookLogout()
        socialAuth.kakaoLogout()

        loginBtn.isEnabled = true
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

        // 키보드 보일경우 비밀번호 변경, 로그인 버튼 활성화
        ViewFunction.showUpKeyboardLayout(mainLayout) { visibility ->
            registerLayout.visibility =
                    if (visibility == View.VISIBLE) View.INVISIBLE
                    else View.VISIBLE
            layoutPopUp.visibility = visibility
        }
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
                    loginViewModel.requestFacebookLogin()
//                    facebookLogin()
                }
                R.id.kakaoBtn -> { // 카카오 로그인 버튼
//                    kakaoLogin()
                }
                R.id.registerBtn -> { // 회원가입 버튼
//                    register()
                }
                R.id.guestBtn -> { // 둘러보기(게스트 모드)
//                    guestLogin()
                }
                R.id.loginBtn -> { // 로컬 로그인 버튼
                    loginViewModel.requestLogin(loginType = LOCAL_LOGIN,
                            email = getEmailText(),
                            password = getPasswordText())
//                    localLogin()
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
                    ViewFunction.onDrawableTouch(v as EditText, event!!) { isTouch ->
                        if (isTouch) {
                            v.setText("")
                            v.performClick()
                        }
                    }
                }
                R.id.mainLayout -> { // 키보드 숨기기
                    if (layoutPopUp.visibility == View.VISIBLE) {
                        ViewFunction.hideKeyboard(this, v)
                    }
                }
            }
        }
        return false
    }

    private fun getEmailText() = editTextMail.text?.trim().toString()
    private fun getPasswordText() = editTextPassword.text?.trim().toString()
}
