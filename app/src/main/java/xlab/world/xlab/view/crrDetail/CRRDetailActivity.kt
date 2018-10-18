package xlab.world.xlab.view.crrDetail

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.action_bar_default.*
import org.koin.android.architecture.ext.viewModel
import org.koin.android.ext.android.inject
import xlab.world.xlab.R
import xlab.world.xlab.utils.support.IntentPassName
import xlab.world.xlab.utils.support.SPHelper
import xlab.world.xlab.utils.view.dialog.DefaultProgressDialog

class CRRDetailActivity : AppCompatActivity(), View.OnClickListener {
    private val crrDetailViewModel: CRRDetailViewModel by viewModel()
    private val spHelper: SPHelper by inject()

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

    }

    private fun onBindEvent() {

    }

    private fun observeViewModel() {

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
