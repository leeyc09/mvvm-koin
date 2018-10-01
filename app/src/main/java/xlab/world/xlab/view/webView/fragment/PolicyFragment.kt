package xlab.world.xlab.view.webView.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import kotlinx.android.synthetic.main.fragment_policy.*
import xlab.world.xlab.R

class PolicyFragment: Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_policy, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        onSetup()
    }

    private fun onSetup() {
        webView.settings.loadWithOverviewMode = true
        webView.settings.useWideViewPort = true
        webView.settings.builtInZoomControls = true
        webView.settings.setSupportZoom(true)

        webView.loadUrl(getBundleUrl())
        webView.webChromeClient = WebChromeClient()
    }

    private fun getBundleUrl(): String = arguments?.getString("url") ?: ""

    companion object {
        fun newFragment(url: String): PolicyFragment {
            val fragment = PolicyFragment()

            val args = Bundle()
            args.putString("url", url)
            fragment.arguments = args

            return fragment
        }
    }
}