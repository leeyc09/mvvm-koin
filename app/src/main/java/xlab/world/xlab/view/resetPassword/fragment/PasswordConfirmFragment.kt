package xlab.world.xlab.view.resetPassword.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import kotlinx.android.synthetic.main.fragment_password_confirm.*
import org.koin.android.architecture.ext.viewModel
import org.koin.android.ext.android.inject
import xlab.world.xlab.R
import xlab.world.xlab.utils.support.SPHelper
import xlab.world.xlab.utils.support.ViewFunction
import xlab.world.xlab.utils.view.dialog.DefaultProgressDialog
import xlab.world.xlab.utils.view.toast.DefaultToast
import xlab.world.xlab.view.resetPassword.ResetPasswordViewModel
import xlab.world.xlab.view.resetPassword.UpdatePasswordActivity

class PasswordConfirmFragment: Fragment(), View.OnClickListener {
    private val resetPasswordViewModel: ResetPasswordViewModel by viewModel()
    private val spHelper: SPHelper by inject()

    private lateinit var defaultToast: DefaultToast
    private lateinit var progressDialog: DefaultProgressDialog

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_password_confirm, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        onSetup()

        onBindEvent()

        observeViewModel()
    }

    private fun onSetup() {
        // Toast, Dialog 초기화
        defaultToast = DefaultToast(context = context!!)
        progressDialog = DefaultProgressDialog(context = context!!)

        nextBtn.isEnabled = false
    }

    private fun onBindEvent() {
        nextBtn.setOnClickListener(this) // 기존 비밀번호 확인 버튼

        // 다음버튼 활성화
        ViewFunction.onTextChange(editText = editTextPassword) { password ->
            nextBtn.isEnabled = password.isNotEmpty()
        }

        ViewFunction.onKeyboardActionTouch(editText = editTextPassword, putActionID = EditorInfo.IME_ACTION_DONE) { isTouch ->
           if (isTouch && nextBtn.isEnabled)
               nextBtn.performClick()
        }
    }

    private fun observeViewModel() {
        // UI 이벤트 observe
        resetPasswordViewModel.uiData.observe(this, android.arch.lifecycle.Observer { uiData ->
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
            }
        })

        // confirm password 이벤트 observe
        resetPasswordViewModel.requestConfirmPasswrodEvent.observe(owner = this, observer = android.arch.lifecycle.Observer { requestConfirmPasswrodEvent ->
            requestConfirmPasswrodEvent?.let { _ ->
                requestConfirmPasswrodEvent.status?.let {
                    if (it) { // 비밀번호 체크 성공
                        (context as UpdatePasswordActivity).runNewPasswordFragment()
                    }
                }
            }
        })
    }

    override fun onClick(v: View?) {
        v?.let {
            when (v.id) {
                R.id.nextBtn -> { // 기존 비밀번호 확인 버튼
                    resetPasswordViewModel.requestConfirmPasswrod(authorization = spHelper.authorization, password = getPassword())
                }
            }
        }
    }

    private fun getPassword(): String = editTextPassword?.text.toString()

    companion object {
        fun newFragment(): PasswordConfirmFragment {
            return PasswordConfirmFragment()
        }
    }
}