package xlab.world.xlab.view.resetPassword.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotlinx.android.synthetic.main.fragment_email_confirm.*
import org.koin.android.architecture.ext.viewModel
import org.koin.android.ext.android.inject
import xlab.world.xlab.R
import xlab.world.xlab.utils.support.SPHelper
import xlab.world.xlab.utils.support.ViewFunction
import xlab.world.xlab.utils.view.dialog.DefaultOneDialog
import xlab.world.xlab.utils.view.dialog.DefaultProgressDialog
import xlab.world.xlab.utils.view.toast.DefaultToast
import xlab.world.xlab.view.resetPassword.ResetPasswordActivity
import xlab.world.xlab.view.resetPassword.ResetPasswordViewModel

class EmailConfirmFragment: Fragment(), View.OnClickListener {
    private val resetPasswordViewModel: ResetPasswordViewModel by viewModel()
    private val viewFunction: ViewFunction by inject()

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
        timeOverDialog = DefaultOneDialog(context = context!!, text = getString(R.string.end_confirm_timer), listener = null)
    }

    private fun onBindEvent() {
        confirmRequestBtn.setOnClickListener(this) // 인증번호 요청
        nextBtn.setOnClickListener(this) // 인증번호 확인 버튼

        // 다음버튼 활성화
        viewFunction.onTextChange(editText = editTextCode) { code ->
            nextBtn.isEnabled = code.length == 6
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
                uiData.timerText?.let {
                    textViewTimer.setText(it, TextView.BufferType.SPANNABLE)
                }
            }
        })

        // 메일 인증 시도 Event
        resetPasswordViewModel.requestConfirmEmailEvent.observe(owner = this, observer = android.arch.lifecycle.Observer { requestConfirmEmailEvent ->
            requestConfirmEmailEvent?.let { _ ->
                requestConfirmEmailEvent.isSuccess?.let {
                    if (it) { // 메일 인증 성공
                        confirmCodeLayout.visibility = View.VISIBLE
                        textViewMailRequest.visibility = View.INVISIBLE
                        textViewMailReRequest.visibility = View.VISIBLE

                        // 타이머 시작
                        resetPasswordViewModel.startTimer()
                    } else { // 메일 인증 실패
                        confirmCodeLayout.visibility = View.GONE
                        textViewMailRequest.visibility = View.VISIBLE
                        textViewMailReRequest.visibility = View.INVISIBLE

                        // 타이머 종료
                        resetPasswordViewModel.stopTimer()
                    }
                }
            }
        })

        // 타이머 Event
        resetPasswordViewModel.timerEvent.observe(owner = this, observer = android.arch.lifecycle.Observer { timerEvent ->
            timerEvent?.let { _ ->
                timerEvent.isEndTimer?.let {
                    if (it) {
                        timeOverDialog.show()
                        confirmCodeLayout.visibility = View.GONE
                        textViewMailRequest.visibility = View.VISIBLE
                        textViewMailReRequest.visibility = View.INVISIBLE
                    }
                }
            }
        })

        // 인증코드 확인 Event
        resetPasswordViewModel.requestConfirmCodeEvent.observe(owner = this, observer = android.arch.lifecycle.Observer { requestConfirmCodeEvent ->
            requestConfirmCodeEvent?.let { _ ->
                requestConfirmCodeEvent.accessToken?.let {
                    if (it.isNotEmpty()) { // 인증 성공
                        // 타이머 종료
                        resetPasswordViewModel.stopTimer()
                        (context as ResetPasswordActivity).runNewPasswordFragment(it)
                    }
                }
            }
        })
    }

    override fun onClick(v: View?) {
        v?.let {
            when (v.id) {
                R.id.confirmRequestBtn -> { // 인증번호 요청
                    // 타이머 종료
                    resetPasswordViewModel.stopTimer()
                    resetPasswordViewModel.requestConfirmEmail(email = getEmail())
                }
                R.id.nextBtn -> { // 인증코드 확인 버튼
                    resetPasswordViewModel.requestConfirmCode(email = getEmail(), code = getCode())
                }
            }
        }
    }

    private fun getBundleEmail(): String = arguments?.getString("email") ?: ""
    private fun getEmail(): String = editTextMail?.text.toString()
    private fun getCode(): String = editTextCode?.text.toString()

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