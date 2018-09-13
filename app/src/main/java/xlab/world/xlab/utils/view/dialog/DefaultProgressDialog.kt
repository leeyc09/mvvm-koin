package xlab.world.xlab.utils.view.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Window
import kotlinx.android.synthetic.main.dialog_default_progress.*
import xlab.world.xlab.R
import xlab.world.xlab.utils.support.PrintLog

class DefaultProgressDialog(context: Context): Dialog(context) {
    private val tag = "Progress"

    override
    fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_default_progress)

        onSetup()

        onBindEvent()
    }

    private fun onSetup() {
        setCancelable(false)
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        window?.decorView?.setBackgroundResource(android.R.color.transparent)
        window?.setDimAmount(0.0f)
    }

    private fun onBindEvent() {
        mainLayout.setOnClickListener {}
    }

    override fun show() {
        PrintLog.d("dialog", "show", tag)
        super.show()
    }

    override fun dismiss() {
        PrintLog.d("dialog", "dismiss", tag)
        super.dismiss()
    }
}