package xlab.world.xlab.utils.toast

import android.content.Context
import android.os.CountDownTimer
import android.view.Gravity
import android.view.View
import android.widget.TextView
import android.widget.Toast
import xlab.world.xlab.R

class AddCartToast(val context: Context): Toast(context) {
    private var toast: Toast? = null

    fun showToast(duration: Long = 700, gravity: Int = Gravity.CENTER, xOffSet: Int = 0, yOffSet: Int = 0) {
        val view = View.inflate(context, R.layout.toast_add_cart, null)

        val toastCountDown = object: CountDownTimer(duration, duration) {
            override fun onTick(millisUntilFinished: Long) {
            }

            override fun onFinish() {
                toast?.cancel()
            }
        }

        toast?.cancel()
        toast = Toast(context)
        toast?.view = view
        toast?.setGravity(gravity, xOffSet, yOffSet)
        toast?.duration = Toast.LENGTH_SHORT
        toast?.show()
        toastCountDown.start()
    }
}