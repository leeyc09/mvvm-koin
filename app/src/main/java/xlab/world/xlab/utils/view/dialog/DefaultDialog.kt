package xlab.world.xlab.utils.view.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.TextView
import kotlinx.android.synthetic.main.dialog_default.*
import xlab.world.xlab.R

/**
 * Created by dongunkim on 2018. 3. 5..
 */
class DefaultDialog(context: Context,
                    private val textBold: String,
                    private val textRegular: String,
                    private val listener: Listener?): Dialog(context), View.OnClickListener {

    interface Listener {
        fun onOkayTouch(tag: Any?)
    }

    private var tag: Any? = null

    override
    fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_default)

        onSetup()

        onBindEvent()
    }

    private fun onSetup() {
        textViewBold.setText(textBold, TextView.BufferType.SPANNABLE)
        textViewRegular.setText(textRegular, TextView.BufferType.SPANNABLE)

        // 버튼 터치 리스너 없으면 확인 버튼만 활성화
        listener?:let { cancelBtn.visibility = View.GONE }
    }

    private fun onBindEvent() {
        okayBtn.setOnClickListener(this)
        cancelBtn.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        v?.let {
            when (v.id) {
                R.id.okayBtn -> {
                    listener?.onOkayTouch(tag)
                    dismiss()
                }
                R.id.cancelBtn -> {
                    dismiss()
                }
            }
        }
    }

    fun setTag(tag: Any) {
        this.tag = tag
    }
}