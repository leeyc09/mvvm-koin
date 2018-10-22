package xlab.world.xlab.view.resetPassword.fragment

import android.app.Activity
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotlinx.android.synthetic.main.fragment_email_confirm.*
import org.koin.android.architecture.ext.viewModel
import xlab.world.xlab.R
import xlab.world.xlab.utils.support.ViewFunction
import xlab.world.xlab.utils.view.dialog.DefaultOneDialog
import xlab.world.xlab.utils.view.dialog.DefaultProgressDialog
import xlab.world.xlab.utils.view.dialog.DialogCreator
import xlab.world.xlab.utils.view.toast.DefaultToast
import xlab.world.xlab.view.resetPassword.ResetPasswordActivity
import xlab.world.xlab.view.resetPassword.ResetPasswordViewModel

class EmailConfirmFragment: Fragment(), View.OnClickListener {
    private val resetPasswordViewModel: ResetPasswordViewModel by viewModel()

    private lateinit var defaultToast: DefaultToast
    private lateinit var progressDialog: DefaultProgressDialog
    private lateinit var timeOverDialog: DefaultOneDialog

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_email_confirm, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        onSetup()

        onBindEvent()

        observeViewModel()
    }

    override fun onDestroy() {
        resetPasswordViewModel.stopTimer()
        super.onDestroy()
    }

    private fun onSetup() {
        editTextMail.setText(getBundleEmail())

        // Toast, Dialog 초기화
        defaultToast = DefaultToast(context = context!!)
        progressDialog = DefaultProgressDialog(context = context!!)
        timeOverDialog = DialogCreator.timeOverDialog(context = context as Activity)

        resetPasswordViewModel.emailConfirmNextEnable(code = getCode())
    }

    private fun onBindEvent() {
        confirmRequestBtn.setOnClickListener(this) // 인증번호 요청
        nextBtn.setOnClickListener(this) // 인증번호 확인 버튼

        // 다음버튼 활성화
        ViewFunction.onTextChange(editText = editTextCode) {
            resetPasswordViewModel.emailConfirmNextEnable(code = getCode())
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
                uiData.confirmCodeVisibility?.let {
                    confirmCodeLayout.visibility = it
                }
                uiData.mailRequestVisibility?.let {
                    textViewMailRequest.visibility = it
                }
                uiData.mailReRequestVisibility?.let{
                    textViewMailReRequest.visibility = it
                }
                uiData.timerText?.let {
                    textViewTimer.setText(it, TextView.BufferType.SPANNABLE)
                }
                uiData.timerEndDialog?.let {
                    editTextCode.setText("")
                    timeOverDialog.show()
                }
                uiData.nextEnable?.let {
                    nextBtn.isEnabled = it
                }
            }
        })

        // 메일 인증 시도 이벤트 observe
        resetPasswordViewModel.confirmEmailData.observe(owner = this, observer = android.arch.lifecycle.Observer { eventData ->
            eventData?.let { isSuccess ->
                if (isSuccess) { // 메일 인증 성공
                    // 타이머 시작
                    resetPasswordViewModel.startTimer()
                } else { // 메일 인증 실패
                    // 타이머 종료
                    resetPasswordViewModel.stopTimer()
                }
            }
        })

        // 인증코드 확인 Event
        resetPasswordViewModel.confirmCodeData.observe(owner = this, observer = android.arch.lifecycle.Observer { eventData ->
            eventData?.let { accessToken ->// 인증 성공
                // 타이머 종료
                resetPasswordViewModel.stopTimer()
                (context as ResetPasswordActivity).runNewPasswordFragment(accessToken = accessToken)
            }
        })
    }

    override fun onClick(v: View?) {
        v?.let {
            when (v.id) {
                R.id.confirmRequestBtn -> { // 인증번호 요청
                    // 타이머 종료
                    resetPasswordViewModel.stopTimer()
                    resetPasswordViewModel.requestConfirmEmail(context = context!!, email = getEmail())
                }
                R.id.nextBtn -> { // 인증코드 확인 버튼
                    resetPasswordViewModel.requestConfirmCode(email = getEmail(), code = getCode())
                }
            }
        }
    }

    private fun getBundleEmail(): String = arguments?.getString("email") ?: ""
    private fun getEmail(): String = editTextMail?.text.toString().trim()
    private fun getCode(): String = editTextCode?.text.toString().trim()

    companion object {
        fun newFragment(email: String): EmailConfirmFragment {
            val fragment = EmailConfirmFragment()

            val args = Bundle()
            args.putString("email", email)
            fragment.arguments = args

            return fragment
        }
    }
}