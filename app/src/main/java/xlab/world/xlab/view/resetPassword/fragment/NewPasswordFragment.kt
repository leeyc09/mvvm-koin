package xlab.world.xlab.view.resetPassword.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import kotlinx.android.synthetic.main.fragment_new_password.*
import org.koin.android.architecture.ext.viewModel
import org.koin.android.ext.android.inject
import xlab.world.xlab.R
import xlab.world.xlab.utils.support.DataRegex
import xlab.world.xlab.utils.support.SPHelper
import xlab.world.xlab.utils.support.ViewFunction
import xlab.world.xlab.utils.view.dialog.DefaultProgressDialog
import xlab.world.xlab.utils.view.toast.DefaultToast
import xlab.world.xlab.view.register.RegisterViewModel
import xlab.world.xlab.view.resetPassword.ResetPasswordViewModel

class NewPasswordFragment: Fragment(), View.OnClickListener {
    private val resetPasswordViewModel: ResetPasswordViewModel by viewModel()

    private lateinit var defaultToast: DefaultToast
    private lateinit var progressDialog: DefaultProgressDialog

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_new_password, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        onSetup()

        onBindEvent()

        observeViewModel()
    }

    private fun onSetup() {
        textViewConfirmPasswordText.isSelected = false
        textViewConfirmPasswordLength.isSelected = false

        // Toast, Dialog 초기화
        defaultToast = DefaultToast(context = context!!)
        progressDialog = DefaultProgressDialog(context = context!!)
    }

    private fun onBindEvent() {
        finishBtn.setOnClickListener(this) // 비밀번호 변경 완료버튼

        ViewFunction.onTextChange(editText = editTextPassword) { password ->
            textViewConfirmPasswordText.isSelected = DataRegex.passwordTextRegex(password = password)
            textViewConfirmPasswordLength.isSelected = DataRegex.passwordLengthRegex(password = password)
            finishBtn.isEnabled = textViewConfirmPasswordText.isSelected && textViewConfirmPasswordLength.isSelected
        }

        ViewFunction.onKeyboardActionTouch(editText = editTextPassword, putActionID = EditorInfo.IME_ACTION_DONE) { isTouch ->
            if (isTouch && finishBtn.isEnabled)
                finishBtn.performClick()
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

        // 비밀번호 변경 Event
        resetPasswordViewModel.requestChangePasswordEvent.observe(owner = this, observer = android.arch.lifecycle.Observer { requestChangePasswordEvent ->
            requestChangePasswordEvent?.let { _ ->
                requestChangePasswordEvent.status?.let {
                    if (it) { // 비밀번호 변경 성공
                        (context as AppCompatActivity).finish()
                    }
                }
            }
        })
    }

    override fun onClick(v: View?) {
        v?.let {
            when (v.id) {
                R.id.finishBtn -> { // 비밀번호 변경 완료버튼
                    resetPasswordViewModel.requestChangePassword(context = context!!, authorization = "Bearer ${getBundleAccessToken()}", password = getPassword())
                }
            }
        }
    }

    private fun getBundleAccessToken(): String = arguments?.getString("accessToken") ?: ""
    private fun getPassword(): String = editTextPassword?.text.toString()

    companion object {
        fun newFragment(accessToken: String): NewPasswordFragment {
            val fragment = NewPasswordFragment()

            val args = Bundle()
            args.putString("accessToken", accessToken)
            fragment.arguments = args

            return fragment
        }
    }
}