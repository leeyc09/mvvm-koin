package xlab.world.xlab.data.response

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ResCartCntData(@SerializedName("message") val message: String,
                          @SerializedName("total") val total: Int): Serializable