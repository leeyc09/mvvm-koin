package xlab.world.xlab.view.crrDetail

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import kotlinx.android.synthetic.main.action_bar_default.*
import kotlinx.android.synthetic.main.activity_crrdetail.*
import org.koin.android.architecture.ext.viewModel
import org.koin.android.ext.android.inject
import xlab.world.xlab.R
import xlab.world.xlab.utils.support.IntentPassName
import xlab.world.xlab.utils.support.SPHelper
import xlab.world.xlab.utils.view.dialog.DefaultProgressDialog
import xlab.world.xlab.utils.view.toast.DefaultToast

class CRRDetailActivity : AppCompatActivity(), View.OnClickListener {
    private val crrDetailViewModel: CRRDetailViewModel by viewModel()
    private val spHelper: SPHelper by inject()

    private lateinit var defaultToast: DefaultToast
    private lateinit var progressDialog: DefaultProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crrdetail)

        onSetup()

        onBindEvent()

        observeViewModel()
    }

    override fun onBackPressed() {
        actionBackBtn.performClick()
    }

    private fun onSetup() {
        actionBtn.visibility = View.GONE

        // Toast, Dialog 초기화
        defaultToast = DefaultToast(context = this)
        progressDialog = DefaultProgressDialog(context = this)

        crrDetailViewModel.loadCRRDetail(context = this, authorization = spHelper.authorization,
                handelSno = intent.getStringExtra(IntentPassName.HANDLE_SNO))
    }

    private fun onBindEvent() {
        actionBackBtn.setOnClickListener(this) // 뒤로가기
    }

    private fun observeViewModel() {
        // UI 이벤트 observe
        crrDetailViewModel.uiData.observe(this, android.arch.lifecycle.Observer { uiData ->
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
                uiData.titleStr?.let {
                    actionBarTitle.setText(it, TextView.BufferType.SPANNABLE)
                }
                uiData.reasonTitle?.let {
                    reasonTitle.setText(it, TextView.BufferType.SPANNABLE)
                }
                uiData.refundInfo?.let {
                    textViewRefundInfo.setText(it, TextView.BufferType.SPANNABLE)
                }
                uiData.refundInfoVisibility?.let {
                    refundLayout.visibility = it
                }
                uiData.adminMemoTitle?.let {
                    textViewAdminMemoTitle.setText(it, TextView.BufferType.SPANNABLE)
                }
                uiData.adminMemo?.let {
                    textViewAdminMemo.setText(it, TextView.BufferType.SPANNABLE)
                }
                uiData.adminMemoVisibility?.let {
                    adminMemoLayout.visibility = it
                }
                uiData.crrReason?.let {
                    textViewReason.setText(it, TextView.BufferType.SPANNABLE)
                }
                uiData.crrMemo?.let {
                    textVieMemo.setText(it, TextView.BufferType.SPANNABLE)
                }
                uiData.crrMemoVisibility?.let {
                    memoLayout.visibility = it
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
            }
        }
    }

    companion object {
        fun newIntent(context: Context, handleSno: String): Intent {
            val intent = Intent(context, CRRDetailActivity::class.java)
            intent.putExtra(IntentPassName.HANDLE_SNO, handleSno)

            return intent
        }
    }
}
