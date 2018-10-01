package xlab.world.xlab.data.response

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ResSettingPushData(@SerializedName("message") val message: String,
                              @SerializedName("result") val result: Boolean): Serializable