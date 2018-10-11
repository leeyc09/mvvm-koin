package xlab.world.xlab.utils.view.webView

import android.annotation.TargetApi
import android.graphics.Bitmap
import android.os.Build
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import xlab.world.xlab.utils.support.PrintLog


class DefaultWebViewClient(private val listener: Listener,
                           private val showInWebView: Boolean?,
                           private val finishURL: String?): WebViewClient() {
    private val tag = "DefaultWebViewClient"

    interface Listener {
        fun onPageStarted(url: String?)
        fun shouldOverrideUrlLoading(url: String?): Boolean
        fun onPageFinished(url: String?)
        fun onWebViewFinished(url: String?)
    }
    private val parsingHtml = "javascript:window.Xlab.getHtml(document.getElementsByTagName('html')[0].innerHTML)"

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        PrintLog.d("start URL", url!!, tag)
        listener.onPageStarted(url)
    }

    @TargetApi(Build.VERSION_CODES.N)
    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        PrintLog.d("check URL", request?.url.toString(), tag)
        listener.shouldOverrideUrlLoading(request?.url.toString())
        return showInWebView ?: listener.shouldOverrideUrlLoading(request?.url.toString())
    }

    @SuppressWarnings("deprecation")
    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
        PrintLog.d("check URL", url!!, tag)
        listener.shouldOverrideUrlLoading(url)

        return showInWebView ?: listener.shouldOverrideUrlLoading(url)
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        PrintLog.d("onPageFinished URL", url!!, tag)
        if (finishURL != null) {
            PrintLog.d("finish URL", finishURL, tag)
            if (url.contains(finishURL))
                listener.onWebViewFinished(parsingHtml)
            else
                listener.onPageFinished(url)
        } else {
            listener.onPageFinished(url)
        }
    }
}