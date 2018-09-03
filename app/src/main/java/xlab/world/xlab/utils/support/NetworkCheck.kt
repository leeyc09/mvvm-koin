package xlab.world.xlab.utils.support

import android.content.Context
import android.net.ConnectivityManager
import xlab.world.xlab.utils.view.toast.DefaultToast

class NetworkCheck(private val context: Context) {
    fun isNetworkConnected(): Boolean {
        val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = manager.activeNetworkInfo

        return when (activeNetworkInfo) {
            null -> false
            else -> activeNetworkInfo.isConnected
        }
    }
}