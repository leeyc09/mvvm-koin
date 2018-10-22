package xlab.world.xlab.data.response

import com.google.gson.annotations.SerializedName
import xlab.world.xlab.utils.support.ApiCallBackConstants
import java.net.HttpURLConnection

data class ResMessageErrorData(@SerializedName("message") val message: String): BaseErrorData(HttpURLConnection.HTTP_INTERNAL_ERROR) {
    fun getErrorDetail():String? {
        val errorMessage = message.split(ApiCallBackConstants.DELIMITER_CHARACTER)
        return if (errorMessage.size > 1) errorMessage[1] else null
    }
}