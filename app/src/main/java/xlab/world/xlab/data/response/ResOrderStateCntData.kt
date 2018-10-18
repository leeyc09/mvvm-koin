package xlab.world.xlab.data.response

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ResOrderStateCntData(@SerializedName("message") val message: String,
                                @SerializedName("orderStatusNum") val orderStateCnt: ArrayList<Int>): Serializable