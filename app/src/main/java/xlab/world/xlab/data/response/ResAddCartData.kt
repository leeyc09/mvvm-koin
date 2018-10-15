package xlab.world.xlab.data.response

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ResAddCartData(@SerializedName("message") val message: String,
                          @SerializedName("sno") val sno: Int): Serializable