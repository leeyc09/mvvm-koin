package xlab.world.xlab.view.webView

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.os.Environment
import android.provider.Browser
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.webkit.WebChromeClient
import android.widget.TextView
import kotlinx.android.synthetic.main.action_bar_default.*
import kotlinx.android.synthetic.main.activity_default_web_view.*
import org.koin.android.architecture.ext.viewModel
import org.koin.android.ext.android.inject
import xlab.world.xlab.R
import xlab.world.xlab.server.ApiURL
import xlab.world.xlab.utils.asyncTask.DownloadFileTask
import xlab.world.xlab.utils.support.AppConstants
import xlab.world.xlab.utils.support.IntentPassName
import xlab.world.xlab.utils.support.PrintLog
import xlab.world.xlab.utils.support.SPHelper
import xlab.world.xlab.utils.view.dialog.DefaultProgressDialog
import xlab.world.xlab.utils.view.toast.DefaultToast
import xlab.world.xlab.utils.view.webView.DefaultWebViewClient
import xlab.world.xlab.view.cart.CartViewModel
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.*

class BuyGoodsWebViewActivity : AppCompatActivity(), View.OnClickListener {
    private val buyGoodsViewModel: BuyGoodsViewModel by viewModel()
    private val spHelper: SPHelper by inject()

    private var isLogin = true
//    private var snoList = ArrayList<Int>()

    private val loginUrl = "${ApiURL.XLAB_GODO_MOBILE_URL_SSL}/member/login.php"
    private val orderUrl = "${ApiURL.XLAB_GODO_MOBILE_URL_SSL}/order/order.php?cartIdx="
    private val mainUrl = "${ApiURL.XLAB_GODO_MOBILE_URL_SSL}/main"
    private val cartUrl = "${ApiURL.XLAB_GODO_MOBILE_URL_SSL}/order/cart.php"
    private val finishAddressUrl = "${ApiURL.XLAB_GODO_MOBILE_URL_SSL}/order/order_end.php"
    private val orderNoContainUrl = "$finishAddressUrl?orderNo="

    private lateinit var defaultToast: DefaultToast
    private lateinit var progressDialog: DefaultProgressDialog

    private lateinit var webViewClient: DefaultWebViewClient

    private val webViewClientListener = object: DefaultWebViewClient.Listener {
        override fun onPageStarted(url: String?) {
            url?.let { _ ->
                buyGoodsViewModel.webViewPageStarted(authorization = spHelper.authorization, url = url)
            }
        }
        override fun shouldOverrideUrlLoading(url: String?): Boolean {
            url?.let { _ ->
                return buyGoodsViewModel.webViewPageLoading(authorization = spHelper.authorization, userId = spHelper.userId,
                        context = this@BuyGoodsWebViewActivity, url = url)
            }
            return false
        }
        override fun onPageFinished(url: String?) {
            url?.let { _ -> buyGoodsViewModel.webViewPageFinished(url = url, tIsLogin = isLogin) }
        }

        override fun onWebViewFinished(url: String?) {
            webView.loadUrl(url)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_default_web_view)

        onSetup()

        onBindEvent()

        observeViewModel()
    }

    override fun onBackPressed() {
        actionBackBtn.performClick()
    }

    private fun onSetup() {
        actionBarTitle.setText(getText(R.string.order_pay), TextView.BufferType.SPANNABLE)
        actionBtn.visibility = View.GONE

        // Toast, Dialog 초기화
        defaultToast = DefaultToast(context = this)
        progressDialog = DefaultProgressDialog(context = this)

        // web view 초기화
        webView.settings.javaScriptEnabled = true
        webView.webChromeClient = WebChromeClient()
        webViewClient = DefaultWebViewClient(webViewClientListener, null, finishAddressUrl)
        webView.webViewClient = webViewClient

        buyGoodsViewModel.setBuyGoodsData(snoList = intent.getIntegerArrayListExtra(IntentPassName.SNO_LIST),
                intentFrom = intent.getIntExtra(IntentPassName.INTENT_FROM, AppConstants.FROM_GOODS_DETAIL))
    }

    private fun onBindEvent() {
        actionBackBtn.setOnClickListener(this)
    }

    private fun observeViewModel() {
        // TODO: BuyGoodsViewModel 이벤트
        // UI 이벤트 observe
        buyGoodsViewModel.uiData.observe(this, android.arch.lifecycle.Observer { uiData ->
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
                uiData.resultCode?.let {
                    setResult(it)
                    finish()
                }
                uiData.webViewVisibility?.let {
                    webView.visibility = it
                }
                uiData.webViewLoadUrl?.let {
                    webView.loadUrl(it)
                }
            }
        })

        // pageLoadingEventData 이벤트 observe
        buyGoodsViewModel.pageLoadingEventData.observe(owner = this, observer = android.arch.lifecycle.Observer { eventData ->
            eventData?.let { _ ->
                eventData.fileName?.let {
                    // 안드로이드 패키지 매니저를 사용한 어플리케이션 설치.
                    val apkFile = File(Environment.getExternalStorageDirectory().toString() + "/" + it)
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive")
                    startActivity(intent)
                }
                eventData.intentData?.let {
                    val intent = Intent(Intent.ACTION_VIEW, it.uri)
                    it.category?.let { category -> intent.addCategory(category) }
                    it.browser?.let { browser -> intent.putExtra(browser, packageName) }
                    try {
                        startActivity(intent)
                    } catch (e: ActivityNotFoundException) {
                    }
                }
                eventData.needOverridePendingTransition?.let {
                    overridePendingTransition(0, 0)
                }
                eventData.startActivityIfNeeded?.let {
                    startActivityIfNeeded(it, -1)
                }
                eventData.webViewPostData?.let {
                    webView.postUrl(it.url, it.postData.toByteArray())
                }
                eventData.finishOrderNo?.let {
                    intent.putExtra(IntentPassName.ORDER_NO, it)
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                }
            }
        })
    }

    override fun onClick(v: View?) {
        v?.let {
            when (v.id) {
                R.id.actionBackBtn -> { // 뒤로가기
                    buyGoodsViewModel.actionBackAction(authorization = spHelper.authorization)
                }
            }
        }
    }

    companion object {
        fun newIntent(context: Context, snoList: ArrayList<Int>, from: Int): Intent {
            val intent = Intent(context, BuyGoodsWebViewActivity::class.java)
            intent.putExtra(IntentPassName.INTENT_FROM, from)
            intent.putIntegerArrayListExtra(IntentPassName.SNO_LIST, snoList)

            return intent
        }
    }
}
