package xlab.world.xlab.utils.view.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.TextView
import kotlinx.android.synthetic.main.dialog_default_one.*
import xlab.world.xlab.R

class DefaultOneDialog(context: Context,
                       private val text: String,
                       private val listener: Listener?): Dialog(context), View.OnClickListener {

    interface Listener {
        fun onOkayTouch(tag: Any?)
    }

    private var tag: Any? = null

    override
    fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_default_one)

        onSetup()

        onBindEvent()
    }

    override fun dismiss() {
        this.tag = null
        super.dismiss()
    }

    private fun onSetup() {
        textViewMedium.setText(text, TextView.BufferType.SPANNABLE)

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

    fun showDialog(tag: Any?) {
        this.tag = tag
    }
}