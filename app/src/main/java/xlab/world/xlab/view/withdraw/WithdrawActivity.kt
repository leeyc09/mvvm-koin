package xlab.world.xlab.view.withdraw

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import kotlinx.android.synthetic.main.action_bar_default.*
import kotlinx.android.synthetic.main.activity_withdraw.*
import org.koin.android.architecture.ext.viewModel
import org.koin.android.ext.android.inject
import xlab.world.xlab.R
import xlab.world.xlab.utils.support.SPHelper
import xlab.world.xlab.utils.view.dialog.DefaultProgressDialog
import xlab.world.xlab.utils.view.toast.DefaultToast

class WithdrawActivity : AppCompatActivity(), View.OnClickListener {

    private val withdrawViewModel: WithDrawViewModel by viewModel()
    private val spHelper: SPHelper by inject()

    private lateinit var defaultToast: DefaultToast
    private lateinit var progressDialog: DefaultProgressDialog

    private val reasonRadioGroupListener = RadioGroup.OnCheckedChangeListener { _, checkedId ->
        editTextReason.setText("")
        editTextReason.visibility =
                if (checkedId == R.id.reasonRadio5) View.VISIBLE // 기타 이유
                else View.GONE // 그 외

        val radioButton = this@WithdrawActivity.findViewById(checkedId) as RadioButton
        withdrawViewModel.enableWithdraw(withdrawReason = radioButton.text.toString(), isAgree = agreeBtn.isSelected)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_withdraw)

        onSetup()

        onBindEvent()

        observeViewModel()
    }

    override fun onBackPressed() {
        actionBackBtn.performClick()
    }

    private fun onSetup() {
        actionBarTitle.visibility = View.GONE
        actionBtn.visibility = View.GONE

        defaultToast = DefaultToast(context = this)
        progressDialog = DefaultProgressDialog(context = this)
    }

    private fun onBindEvent() {
        actionBackBtn.setOnClickListener(this) // 뒤로가기
        agreeBtn.setOnClickListener(this) // 탈퇴 동의
        withdrawBtn.setOnClickListener(this) // 탈퇴하기

        reasonRadioGroup.setOnCheckedChangeListener(reasonRadioGroupListener)
    }

    private fun observeViewModel() {
        // UI 이벤트 observe
        withdrawViewModel.uiData.observe(this, android.arch.lifecycle.Observer { uiData ->
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
                uiData.enableWithdraw?.let {
                    withdrawBtn.isEnabled = it
                }
            }
        })

        // 회원탈퇴 이벤트 observe
        withdrawViewModel.withdrawEventData.observe(owner = this, observer = android.arch.lifecycle.Observer { withdrawEventData ->
            withdrawEventData?.let { _->
                withdrawEventData.status?.let { isSuccess ->
                    if (isSuccess) {
                        setResult(Activity.RESULT_OK)
                        finish()
                    }
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
                R.id.agreeBtn -> { // 탈퇴 동의
                    agreeBtn.isSelected = !agreeBtn.isSelected
                    withdrawViewModel.enableWithdraw(withdrawReason = null, isAgree = agreeBtn.isSelected)
                }
                R.id.withdrawBtn -> { // 탈퇴하기
                    withdrawViewModel.withdraw(context = this, otherReason = getOtherReason(), authorization = spHelper.authorization)
                }
            }
        }
    }

    private fun getOtherReason(): String? {
        val reason = editTextReason.text.toString().trim()
        return if (reason.isEmpty()) null else reason
    }

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, WithdrawActivity::class.java)
        }
    }
}
