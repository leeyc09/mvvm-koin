package xlab.world.xlab.data.request

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ReqConfirmEmailData(@SerializedName("email") val email: String,
                               @SerializedName("code") val code: String): Serializable