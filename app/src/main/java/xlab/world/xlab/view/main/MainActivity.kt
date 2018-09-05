package xlab.world.xlab.view.main

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import xlab.world.xlab.R

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    companion object {
        fun newIntent(context: Context, linkData: Uri?): Intent {
            val intent = Intent(context, MainActivity::class.java)
            intent.data = linkData

            return intent
        }
    }
}
