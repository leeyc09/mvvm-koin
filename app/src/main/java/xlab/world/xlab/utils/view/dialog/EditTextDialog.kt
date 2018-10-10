package xlab.world.xlab.utils.view.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.*
import kotlinx.android.synthetic.main.dialog_edit_text.*
import xlab.world.xlab.R
import xlab.world.xlab.utils.view.toast.DefaultToast

class EditTextDialog(context: Context,
                     private val listener: EditTextDialogListener?): Dialog(context), View.OnClickListener {

    interface EditTextDialogListener {
        fun onOkayTouch(text: String)
    }

    private lateinit var defaultToast: DefaultToast

    override
    fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_edit_text)

        onSetup()

        onBindEvent()
    }

    override fun dismiss() {
        editText.setText("")
        super.dismiss()
    }

    private fun onSetup() {
        defaultToast = DefaultToast(context = context)
        setCancelable(false)

        if (listener == null)
            cancelBtn.visibility = View.GONE
    }

    private fun onBindEvent() {
        okayBtn.setOnClickListener(this)
        cancelBtn.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        v?.let {
            when (v.id) {
                R.id.okayBtn -> {
                    if (listener == null) {
                        dismiss()
                    } else {
                        val text = editText.text.toString()
                        if (text.isEmpty()) {
                            defaultToast.showToast("empty youtube id")
                        } else {
                            listener.onOkayTouch(editText.text.toString().trim())
                            dismiss()
                        }
                    }
                }
                R.id.cancelBtn -> {
                    dismiss()
                }
            }
        }
    }
}