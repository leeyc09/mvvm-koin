package xlab.world.xlab.data.response

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ResMessageData(@SerializedName("message") val message: String): Serializable