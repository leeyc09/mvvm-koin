package xlab.world.xlab.data.response

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ResConfirmEmailCodeData(@SerializedName("message") val message: String,
                                   @SerializedName("accessToken") val accessToken: String): Serializable