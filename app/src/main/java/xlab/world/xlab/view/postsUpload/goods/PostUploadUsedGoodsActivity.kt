package xlab.world.xlab.view.postsUpload.goods

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import xlab.world.xlab.R

class PostUploadUsedGoodsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_upload_used_goods)
    }

    companion object {
        fun newIntent(context: Context): Intent {
            val intent = Intent(context, PostUploadUsedGoodsActivity::class.java)

            return intent
        }
    }
}
