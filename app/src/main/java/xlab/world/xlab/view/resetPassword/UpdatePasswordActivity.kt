package xlab.world.xlab.view.resetPassword

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.action_bar_default.*
import org.koin.android.ext.android.inject
import xlab.world.xlab.R
import xlab.world.xlab.utils.support.SPHelper
import xlab.world.xlab.utils.view.dialog.DefaultProgressDialog
import xlab.world.xlab.utils.view.toast.DefaultToast
import xlab.world.xlab.view.resetPassword.fragment.NewPasswordFragment
import xlab.world.xlab.view.resetPassword.fragment.PasswordConfirmFragment

class UpdatePasswordActivity : AppCompatActivity(), View.OnClickListener {
    private val spHelper: SPHelper by inject()

    private lateinit var defaultToast: DefaultToast
    private lateinit var progressDialog: DefaultProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_password)

        onSetup()

        onBindEvent()

        observeViewModel()
    }

    override fun onBackPressed() {
        actionBackBtn.performClick()
    }

    private fun onSetup() {
        // 타이틀, 확인 버튼 비활성화
        actionBarTitle.visibility = View.GONE
        actionBtn.visibility = View.GONE

        // Toast, Dialog 초기화
        defaultToast = DefaultToast(context = this)
        progressDialog = DefaultProgressDialog(context = this)

        // 프래그먼트 초기화
        val passwordConfirmFragment = PasswordConfirmFragment.newFragment()
        supportFragmentManager.beginTransaction().replace(R.id.fragmentLayout, passwordConfirmFragment).commit()
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

    fun runNewPasswordFragment() {
        val newPasswordFragment = NewPasswordFragment.newFragment(accessToken = spHelper.accessToken)
        supportFragmentManager.beginTransaction().replace(R.id.fragmentLayout, newPasswordFragment).commit()
    }

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, UpdatePasswordActivity::class.java)
        }
    }
}
