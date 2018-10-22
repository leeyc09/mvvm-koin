package xlab.world.xlab.utils.support

import android.util.Log
import xlab.world.xlab.BuildConfig

object PrintLog {
    private const val defaultTag: String = "XLAB_LOG"
    // Log for debug
    fun d(title:String, log: String) {
        if (BuildConfig.DEBUG)
            Log.d(defaultTag ?: defaultTag, "$title => $log")
    }
    // Log for debug
    fun d(title:String, log: String, tag: String? = null) {
        if (BuildConfig.DEBUG)
            Log.d("$defaultTag $tag", "$title => $log")
    }
    // Log for error
    fun e(title:String, log: String, tag: String? = null) {
        if (BuildConfig.DEBUG)
            Log.e("$defaultTag $tag", "$title => $log")
    }
    // Log for information
    fun i(title:String, log: String, tag: String? = null) {
        if (BuildConfig.DEBUG)
            Log.i("$defaultTag $tag", "$title => $log")
    }
}