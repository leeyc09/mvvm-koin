package xlab.world.xlab.view.goodsInfo

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import kotlinx.android.synthetic.main.action_bar_default.*
import kotlinx.android.synthetic.main.activity_goods_necessary.*
import org.koin.android.architecture.ext.viewModel
import xlab.world.xlab.R
import xlab.world.xlab.utils.support.IntentPassName

class GoodsNecessaryActivity : AppCompatActivity(), View.OnClickListener {
    private val goodsInfoVewModel: GoodsInfoVewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_goods_necessary)

        onSetup()

        onBindEvent()

        observeViewModel()
    }

    override fun onBackPressed() {
        actionBackBtn.performClick()
    }

    private fun onSetup() {
        actionBarTitle.setText(getText(R.string.item_info_necessary), TextView.BufferType.SPANNABLE)
        actionBtn.visibility = View.GONE

        textViewGoodsName.setText(intent.getStringExtra(IntentPassName.GOODS_NAME), TextView.BufferType.SPANNABLE)
        textViewOrigin.setText(intent.getStringExtra(IntentPassName.GOODS_ORIGIN), TextView.BufferType.SPANNABLE)
        textViewMaker.setText(intent.getStringExtra(IntentPassName.GOODS_MAKER), TextView.BufferType.SPANNABLE)

        goodsInfoVewModel.necessaryPhoneLayout(deliveryNo = intent.getStringExtra(IntentPassName.DELIVER_NO))
    }

    private fun onBindEvent() {
        actionBackBtn.setOnClickListener(this) // 뒤로가기
        textViewGoodsCall.setOnClickListener(this) // 상품 관련 전화 (올라펫)
        textViewServiceCall.setOnClickListener(this) // 서비스 관련 전화 (슬랩)
        textViewServiceCall2.setOnClickListener(this) // 상품 & 서비스 관련 전화 (슬랩)
    }

    private fun observeViewModel() {
        // UI 이벤트 observe
        goodsInfoVewModel.uiData.observe(this, android.arch.lifecycle.Observer { uiData ->
            uiData?.let { _ ->
                uiData.holapetPhoneVisibility?.let {
                    holapetPhoneLayout.visibility = it
                }
                uiData.xlabPhoneVisibility?.let {
                    xlabPhoneLayout.visibility = it
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
                R.id.textViewGoodsCall,
                R.id.textViewServiceCall,
                R.id.textViewServiceCall2-> { // 전화 걸기
                    val num = (v as TextView).text.toString()
                    val intent = Intent(Intent.ACTION_DIAL)
                    intent.data = Uri.parse("tel:$num")
                    startActivity(intent)
                }
            }
        }
    }

    companion object {
        fun newIntent(context: Context, deliveryNo: String, goodsName: String, origin: String, maker: String): Intent {
            val intent = Intent(context, GoodsNecessaryActivity::class.java)
            intent.putExtra(IntentPassName.DELIVER_NO, deliveryNo)
            intent.putExtra(IntentPassName.GOODS_NAME, goodsName)
            intent.putExtra(IntentPassName.GOODS_ORIGIN, origin)
            intent.putExtra(IntentPassName.GOODS_MAKER, maker)

            return intent
        }
    }
}
