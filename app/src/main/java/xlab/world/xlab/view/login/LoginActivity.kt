package xlab.world.xlab.view.login

import android.app.Activity
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import kotlinx.android.synthetic.main.action_bar_default.*
import kotlinx.android.synthetic.main.activity_login.*
import org.koin.android.architecture.ext.viewModel
import org.koin.android.ext.android.inject
import xlab.world.xlab.R
import xlab.world.xlab.server.ApiUser
import xlab.world.xlab.utils.PrintLog
import xlab.world.xlab.utils.ViewFunction
import xlab.world.xlab.utils.rx.argument
import xlab.world.xlab.utils.view.dialog.DefaultProgressDialog
import xlab.world.xlab.utils.view.toast.DefaultToast
import xlab.world.xlab.view.IntentPassName

class LoginActivity : AppCompatActivity(), View.OnClickListener, View.OnTouchListener {

    private val loginViewModel: LoginViewModel by viewModel()
    private val viewFuncion: ViewFunction by inject()
    private val apiUser: ApiUser by inject()

    private val isComeOtherActivity: Boolean? by argument(IntentPassName.IS_COME_OTHER_ACTIVITY, true)

    private lateinit var defaultToast: DefaultToast
    private lateinit var progressDialog: DefaultProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        onSetupView()

        defaultToast = DefaultToast(this)
        progressDialog = DefaultProgressDialog(this)

        onBindEvent()

//        loadKoinModules(listOf(remoteModule))

//        btn.setOnClickListener {
//            loginViewModel.requestLogin(apiUser = apiUser, email = getEmailText(), password = getPasswordText())
//        }

        loginViewModel.uiData.observe(this, android.arch.lifecycle.Observer { uiData ->
            uiData?.let { _ ->
                uiData.toastMessage?.let {
                    defaultToast.showToast(it)
                }
            }
        })
    }

    private fun onSetupView() {
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
//        viewFuncion.showUpKeyboardLayout(mainLayout) { visibility ->
//            registerLayout.visibility =
//                    if (visibility == View.VISIBLE) View.INVISIBLE
//                    else View.VISIBLE
//            layoutPopUp.visibility = visibility
//        }
    }

    override fun onClick(v: View?) {
        v?.let {
            when (v.id) {
                R.id.actionBackBtn -> { // 뒤로가기
                    setResult(Activity.RESULT_CANCELED)
                    finish()
                }
                R.id.facebookBtn -> { // 페이스북 로그인 버튼
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
//                R.id.editTextMail -> { // 이메일 텍스트 지우기
//                    ViewFunction.onDrawableTouch(v as EditText, event!!) { isTouch ->
//                        if (isTouch) {
//                            v.setText("")
//                            v.performClick()
//                        }
//                    }
//                }
//                R.id.editTextPassword -> { // 패스워드 텍스트 지우기
//                    ViewFunction.onDrawableTouch(v as EditText, event!!) { isTouch ->
//                        if (isTouch) {
//                            v.setText("")
//                            v.performClick()
//                        }
//                    }
//                }
//                R.id.mainLayout -> { // 키보드 숨기기
//                    if (layoutPopUp.visibility == View.VISIBLE) {
//                        ViewFunction.hideKeyboard(this, v)
//                    }
//                }
            }
        }
        return false
    }

    private fun getEmailText() = editTextMail.text?.trim().toString()
    private fun getPasswordText() = editTextPassword.text?.trim().toString()
}
