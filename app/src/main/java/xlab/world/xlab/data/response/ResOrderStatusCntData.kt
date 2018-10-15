package xlab.world.xlab.data.response

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ResOrderStatusCntData(@SerializedName("message") val message: String,
                                 @SerializedName("orderStatusNum") val orderStatusCnt: ArrayList<Int>): Serializable