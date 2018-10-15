package xlab.world.xlab.utils.view.dialog

import android.os.Bundle
import android.support.design.widget.BottomSheetDialogFragment
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotlinx.android.synthetic.main.dialog_buy_goods_option.*
import xlab.world.xlab.R
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*

class BuyGoodsOptionDialog: BottomSheetDialogFragment(), View.OnClickListener {

    interface Listener {
        fun addCart(count: Int)
        fun buyNow(count: Int)
    }

    private lateinit var listener: Listener

    private var unitPrice
        get() = arguments?.getInt("unitPrice") ?: 0
        set(value) {
            arguments?.putInt("unitPrice", value)
        }

    override
    fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_buy_goods_option, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        onSetup()

        onBindEvent()
    }

    private fun onSetup() {
    }

    private fun onBindEvent() {
        addCartBtn.setOnClickListener(this) // 카트 담기
        buyNowBtn.setOnClickListener(this) // 바로 구매
        goodsMinusBtn.setOnClickListener(this)
        goodsPlusBtn.setOnClickListener(this)

        textViewGoodsCountNum.addTextChangedListener(object: TextWatcher{
            override fun afterTextChanged(s: Editable?) {
                textViewBuyTotalNum.setText(s.toString(), TextView.BufferType.SPANNABLE)

                val priceFormat = NumberFormat.getInstance(Locale.KOREA) as DecimalFormat
                priceFormat.applyPattern("#,###,###,###")
                val totalPrice = s.toString().toInt() * unitPrice
                textViewBuyTotalPrice.setText(priceFormat.format(totalPrice), TextView.BufferType.SPANNABLE)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })

        goodsPlusBtn.performClick()
    }

    override fun onClick(v: View?) {
        v?.let {
            when (v.id) {
                R.id.addCartBtn -> { // 카트 담기
                    listener.addCart(textViewGoodsCountNum.text.toString().toInt())
                    dismiss()
                }
                R.id.buyNowBtn -> { // 바로 구매
                    listener.buyNow(textViewGoodsCountNum.text.toString().toInt())
                    dismiss()
                }
                R.id.goodsMinusBtn -> { // 수량 마이너스
                    val nowCnt = textViewGoodsCountNum.text.toString().toInt()
                    if (nowCnt > 1) {
                        textViewGoodsCountNum.setText((nowCnt - 1).toString(), TextView.BufferType.SPANNABLE)
                    }
                }
                R.id.goodsPlusBtn -> { // 수량 플러스
                    val nowCnt = textViewGoodsCountNum.text.toString().toInt() + 1
                    textViewGoodsCountNum.setText(nowCnt.toString(), TextView.BufferType.SPANNABLE)
                }
            }
        }
    }

    fun handle(listener: Listener) {
        this.listener = listener
    }

    fun setUnitPirce(price: Int) {
        this.unitPrice = price
    }

    companion object {
        fun newDialog(): BuyGoodsOptionDialog {
            val dialog = BuyGoodsOptionDialog()

            val args = Bundle()
            args.putInt("unitPrice", 0)
            dialog.arguments = args

            return dialog
        }
    }
}