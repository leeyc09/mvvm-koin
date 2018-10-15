package xlab.world.xlab.view.goodsInfo

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.res.ResourcesCompat
import android.view.View
import android.webkit.WebChromeClient
import android.widget.TextView
import kotlinx.android.synthetic.main.action_bar_default.*
import kotlinx.android.synthetic.main.activity_goods_inquiry.*
import org.koin.android.architecture.ext.viewModel
import xlab.world.xlab.R
import xlab.world.xlab.utils.support.AppConstants
import xlab.world.xlab.utils.support.IntentPassName
import xlab.world.xlab.view.webView.DefaultWebViewActivity

class GoodsInquiryActivity : AppCompatActivity(), View.OnClickListener{
    private val goodsInfoVewModel: GoodsInfoVewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_goods_inquiry)

        onSetup()

        onBindEvent()

        observeViewModel()
    }

    override fun onBackPressed() {
        actionBackBtn.performClick()
    }

    private fun onSetup() {
        actionBarTitle.setText(getText(R.string.item_info_inquiry), TextView.BufferType.SPANNABLE)
        actionBtn.visibility = View.GONE

        // set web view url
        webView.loadUrl(AppConstants.LOCAL_HTML_URL + "companyInfo.html")
        webView.webChromeClient = WebChromeClient()
        webView.setBackgroundColor(ResourcesCompat.getColor(resources, R.color.colorF5F5F5, null))

        goodsInfoVewModel.inquiryLayout(context = this, deliveryNo = intent.getStringExtra(IntentPassName.DELIVER_NO))
    }

    private fun onBindEvent() {
        actionBackBtn.setOnClickListener(this) // 뒤로가기
        callLayout.setOnClickListener(this) // 전화문의
        kakaoLayout.setOnClickListener(this) // 카카오 문의
        companyInfoBtn.setOnClickListener(this) // 사업자 정보 확인
        personalInfoBtn.setOnClickListener(this) // 개인정보취급방침
        clauseBtn.setOnClickListener(this) // 이용약관
    }

    private fun observeViewModel() {
        // UI 이벤트 observe
        goodsInfoVewModel.uiData.observe(this, android.arch.lifecycle.Observer { uiData ->
            uiData?.let { _ ->
                uiData.holapetRequestVisibility?.let {
                    textViewHolapetRequest.visibility = it
                }
                uiData.companyName?.let {
                    textViewCallCompany.setText(it, TextView.BufferType.SPANNABLE)
                }
                uiData.companyPhone?.let {
                    textViewCallNum.setText(it, TextView.BufferType.SPANNABLE)
                }
                uiData.companyKaKao?.let {
                    textViewKakaoId.setText(it, TextView.BufferType.SPANNABLE)
                }
                uiData.kakaoId?.let {
                    textViewKakaoId.tag = it
                }
            }
        })
    }

    override fun onClick(v: View?) {
        v?.let {
            when (v.id) {
                R.id.actionBackBtn -> { // 뒤로가기
                    finish()
                }
                R.id.callLayout -> { // 전화문의
                    val num = textViewCallNum.text.toString()
                    val intent = Intent(Intent.ACTION_DIAL)
                    intent.data = Uri.parse("tel:$num")
                    startActivity(intent)
                }
                R.id.kakaoLayout -> { // 카카오 문의
                    val kakaoId = textViewKakaoId.tag as String
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(AppConstants.KAKO_PLUS_CHAT + kakaoId))
                    startActivity(intent)
                }
                R.id.companyInfoBtn -> { // 사업자 정보 확인
                    val intent = DefaultWebViewActivity.newIntent(this,
                            pageTitle = resources.getString(R.string.inquiry_under1), webUrl = "http://www.ftc.go.kr/bizCommPop.do?wrkr_no=2618123403&apv_perm_no=", zoomControl = false)
                    startActivity(intent)
                }
                R.id.personalInfoBtn -> { // 개인정보취급방침
                    val intent = DefaultWebViewActivity.newIntent(this,
                            pageTitle = resources.getString(R.string.inquiry_under2), webUrl = AppConstants.LOCAL_HTML_URL + "personalInfo.html", zoomControl = true)
                    startActivity(intent)
                }
                R.id.clauseBtn -> { // 이용약관
                    val intent = DefaultWebViewActivity.newIntent(this,
                            pageTitle = resources.getString(R.string.inquiry_under3), webUrl = AppConstants.LOCAL_HTML_URL + "clause.html", zoomControl = true)
                    startActivity(intent)
                }
            }
        }
    }

    companion object {
        fun newIntent(context: Context, deliveryNo: String): Intent {
            val intent = Intent(context, GoodsInquiryActivity::class.java)
            intent.putExtra(IntentPassName.DELIVER_NO, deliveryNo)

            return intent
        }
    }
}
