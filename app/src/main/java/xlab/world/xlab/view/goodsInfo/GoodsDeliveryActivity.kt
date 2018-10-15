package xlab.world.xlab.view.goodsInfo

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import kotlinx.android.synthetic.main.action_bar_default.*
import kotlinx.android.synthetic.main.activity_goods_delivery.*
import org.koin.android.architecture.ext.viewModel
import xlab.world.xlab.R
import xlab.world.xlab.utils.support.IntentPassName

class GoodsDeliveryActivity : AppCompatActivity(), View.OnClickListener {
    private val goodsInfoVewModel: GoodsInfoVewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_goods_delivery)

        onSetup()

        onBindEvent()

        observeViewModel()
    }

    override fun onBackPressed() {
        actionBackBtn.performClick()
    }

    private fun onSetup() {
        actionBarTitle.setText(getText(R.string.item_info_delivery), TextView.BufferType.SPANNABLE)
        actionBtn.visibility = View.GONE

        goodsInfoVewModel.deliveryLayout(context = this, deliveryNo = intent.getStringExtra(IntentPassName.DELIVER_NO))
    }

    private fun onBindEvent() {
        actionBackBtn.setOnClickListener(this) // 뒤로가기
    }

    private fun observeViewModel() {
        // UI 이벤트 observe
        goodsInfoVewModel.uiData.observe(this, android.arch.lifecycle.Observer { uiData ->
            uiData?.let { _ ->
                uiData.returnName?.let {
                    textViewReturnName.setText(it, TextView.BufferType.SPANNABLE)
                }
                uiData.returnAddress?.let {
                    textViewReturnAddress.setText(it, TextView.BufferType.SPANNABLE)
                }
                uiData.returnPhone?.let {
                    textViewReturnPhone.setText(it, TextView.BufferType.SPANNABLE)
                }
                uiData.deliveryCompany?.let {
                    textViewDeliveryCompany.setText(it, TextView.BufferType.SPANNABLE)
                    textViewReturnDeliveryCompany.setText(it, TextView.BufferType.SPANNABLE)
                }
                uiData.returnInfo?.let {
                    textViewInfo1.setText(it, TextView.BufferType.SPANNABLE)
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
            }
        }
    }

    companion object {
        fun newIntent(context: Context, deliveryNo: String): Intent {
            val intent = Intent(context, GoodsDeliveryActivity::class.java)
            intent.putExtra(IntentPassName.DELIVER_NO, deliveryNo)

            return intent
        }
    }
}
