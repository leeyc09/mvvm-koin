package xlab.world.xlab.data.response

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ResLikeSavePostData(@SerializedName("message") val message: String,
                               @SerializedName("status") val status: Boolean): Serializable