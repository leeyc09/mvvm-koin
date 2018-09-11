package xlab.world.xlab.view.resetPassword

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.action_bar_default.*
import kotlinx.android.synthetic.main.activity_reset_password.*
import org.koin.android.architecture.ext.viewModel
import org.koin.android.ext.android.inject
import xlab.world.xlab.R
import xlab.world.xlab.utils.support.IntentPassName
import xlab.world.xlab.utils.support.PrintLog
import xlab.world.xlab.utils.support.SPHelper
import xlab.world.xlab.utils.support.ViewFunction
import xlab.world.xlab.utils.view.dialog.DefaultProgressDialog
import xlab.world.xlab.utils.view.toast.DefaultToast
import xlab.world.xlab.view.resetPassword.fragment.EmailConfirmFragment
import xlab.world.xlab.view.resetPassword.fragment.NewPasswordFragment

class ResetPasswordActivity : AppCompatActivity(), View.OnClickListener {
    private val resetPasswordViewModel: ResetPasswordViewModel by viewModel()
    private val spHelper: SPHelper by inject()

    private var initEmail: String = ""

    private lateinit var defaultToast: DefaultToast
    private lateinit var progressDialog: DefaultProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)
        onSetup()

        onBindEvent()

        observeViewModel()
    }

    override fun onBackPressed() {
        actionBackBtn.performClick()
    }

    private fun onSetup() {
        initEmail = intent.getStringExtra(IntentPassName.USER_EMAIL)

        // 타이틀, 확인 버튼 비활성화
        actionBarTitle.visibility = View.GONE
        actionBtn.visibility = View.GONE

        // 프래그먼트 초기화
        val emailConfirmFragment = EmailConfirmFragment.newFragment(initEmail)
        supportFragmentManager.beginTransaction().replace(R.id.fragmentLayout, emailConfirmFragment).commit()

        // Toast, Dialog 초기화
        defaultToast = DefaultToast(context = this)
        progressDialog = DefaultProgressDialog(context = this)
    }

    private fun onBindEvent() {
        actionBackBtn.setOnClickListener(this) // 뒤로가기
    }

    private fun observeViewModel() {
    }

    override fun onClick(v: View?) {
        v?.let {
            when (v.id) {
                R.id.actionBackBtn -> { // 뒤로가기
                    finish()
                }
            }
        }
    }

    fun runNewPasswordFragment(accessToken: String) {
        val newPasswordFragment = NewPasswordFragment.newFragment(accessToken)
        supportFragmentManager.beginTransaction().replace(R.id.fragmentLayout, newPasswordFragment).commit()
    }

    companion object {
        fun newIntent(context: Context, email: String): Intent {
            val intent = Intent(context, ResetPasswordActivity::class.java)
            intent.putExtra(IntentPassName.USER_EMAIL, email)

            return intent
        }
    }
}
