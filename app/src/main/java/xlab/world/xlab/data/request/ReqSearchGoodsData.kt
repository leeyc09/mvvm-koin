package xlab.world.xlab.data.request

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ReqSearchGoodsData(@SerializedName("text") val text: String,
                              @SerializedName("code") val code: String): Serializable