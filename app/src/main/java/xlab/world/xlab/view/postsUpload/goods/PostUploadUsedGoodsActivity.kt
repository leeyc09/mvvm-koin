package xlab.world.xlab.view.postsUpload.goods

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.action_bar_default.*
import xlab.world.xlab.R

class PostUploadUsedGoodsActivity : AppCompatActivity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_upload_used_goods)

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
        actionBackBtn.setOnClickListener(this) // 뒤로가기
    }

    private fun observeViewModel() {

    }

    override fun onClick(v: View?) {
        v?.let {
            when (v.id) {
                R.id.actionBackBtn -> { // 뒤로가기

                }
            }
        }
    }

    companion object {
        fun newIntent(context: Context): Intent {
            val intent = Intent(context, PostUploadUsedGoodsActivity::class.java)

            return intent
        }
    }
}
