package xlab.world.xlab.utils.view.dialog

import android.app.Dialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.Window
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import kotlinx.android.synthetic.main.dialog_shop_login.*
import xlab.world.xlab.R
import xlab.world.xlab.server.ApiURL
import xlab.world.xlab.utils.support.PrintLog
import xlab.world.xlab.utils.view.webView.DefaultWebViewClient
import java.net.URLEncoder

class ShopLoginDialog(context: Context,
                      private val listener: Listener): Dialog(context) {

    interface Listener {
        fun isSuccessLogin(result: Boolean)
    }

    private val tag = "ShopLogin"

    private lateinit var webViewClient: DefaultWebViewClient
    private val webViewClientListener = object: DefaultWebViewClient.WebViewClientListener {
        override fun onPageStarted(url: String?) {
        }
        override fun shouldOverrideUrlLoading(url: String?): Boolean {
            return true
        }
        override fun onPageFinished(url: String?) {
            url?.let { _ ->
            }
        }
        override fun onWebViewFinished(url: String?) {
            listener.isSuccessLogin(true)
            dismiss()
        }
    }
    override
    fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_shop_login)

        onSetup()

        onBindEvent()
    }

    private fun onSetup() {
        setCancelable(false)
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        window?.decorView?.setBackgroundResource(android.R.color.transparent)
        window?.setDimAmount(0.0f)
    }

    private fun onBindEvent() {
        mainLayout.setOnClickListener {}
    }

    override fun dismiss() {
        PrintLog.d("dialog", "dismiss", tag)
        super.dismiss()
    }

    fun requestLogin(userId: String) {
        super.show()

        val postData: String = "loginId=" + URLEncoder.encode(userId, "UTF-8")
        webView.postUrl(ApiURL.SHOP_LOGIN, postData.toByteArray())
        webView.webChromeClient = WebChromeClient()
        webViewClient = DefaultWebViewClient(listener = webViewClientListener, showInWebView = true, finishURL = ApiURL.SHOP_LOGIN)
        webView.webViewClient = webViewClient
    }

    fun requestLogout(userId: String) {
        super.show()

        val postData: String = "loginId=" + URLEncoder.encode(userId, "UTF-8")
        webView.postUrl(ApiURL.SHOP_LOGIN, postData.toByteArray())
        webView.webChromeClient = WebChromeClient()
        webViewClient = DefaultWebViewClient(listener = webViewClientListener, showInWebView = true, finishURL = ApiURL.SHOP_LOGOUT)
        webView.webViewClient = webViewClient

        // delete session in app
        val cookieManager = CookieManager.getInstance()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            cookieManager.removeSessionCookies { value ->
                PrintLog.d("removeAllCookies", value.toString(), tag)
            }
        } else {
            cookieManager.removeSessionCookie()
        }
    }
}