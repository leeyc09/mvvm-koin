package xlab.world.xlab.view.webView

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.webkit.WebChromeClient
import android.widget.TextView
import kotlinx.android.synthetic.main.action_bar_default.*
import kotlinx.android.synthetic.main.activity_default_web_view.*
import xlab.world.xlab.R
import xlab.world.xlab.utils.support.IntentPassName
import xlab.world.xlab.utils.support.PrintLog

class DefaultWebViewActivity : AppCompatActivity() {

    private val tag = "DefaultWebView"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_default_web_view)

        onSetup()

        onBindEvent()
    }

    override fun onBackPressed() {
        actionBackBtn.performClick()
    }

    private fun onSetup() {
        actionBtn.visibility = View.GONE

        // 타이틀 set
        val title: String? = intent.getStringExtra(IntentPassName.PAGE_TITLE)
        title?.let {
            PrintLog.d("title", title)
            actionBarTitle.visibility = View.VISIBLE
            actionBarTitle.setText(title, TextView.BufferType.SPANNABLE)
        }?:let {
            PrintLog.d("title", "null")
            actionBarTitle.visibility = View.GONE
        }


        // webView zoom control
        val zoomControl: Boolean = intent.getBooleanExtra(IntentPassName.WEB_ZOOM_CONTROL, true)
        webView.settings.loadWithOverviewMode = true
        webView.settings.useWideViewPort = true
        webView.settings.builtInZoomControls = zoomControl
        webView.settings.setSupportZoom(zoomControl)

        // set web view url
        val url = intent.getStringExtra(IntentPassName.WEB_URL)
        webView.loadUrl(url)
        webView.webChromeClient = WebChromeClient()
    }

    private fun onBindEvent() {
        actionBackBtn.setOnClickListener {
            finish()
        }
    }

    companion object {
        fun newIntent(context: Context, pageTitle: String?, webUrl: String, zoomControl: Boolean): Intent {
            val intent = Intent(context, DefaultWebViewActivity::class.java)
            intent.putExtra(IntentPassName.PAGE_TITLE, pageTitle)
            intent.putExtra(IntentPassName.WEB_URL, webUrl)
            intent.putExtra(IntentPassName.WEB_ZOOM_CONTROL, zoomControl)

            return intent
        }
    }
}
