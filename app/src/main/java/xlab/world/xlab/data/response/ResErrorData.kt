package xlab.world.xlab.data.response

import com.google.gson.annotations.SerializedName
import java.net.HttpURLConnection

data class ResErrorData(@SerializedName("message") val message: String?): BaseErrorData(HttpURLConnection.HTTP_INTERNAL_ERROR)