package xlab.world.xlab.data.response

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ResGoodsStatsData(@SerializedName("message") val message: String,
                             @SerializedName("statsList") val statsList: ArrayList<Stats>?): Serializable {

    data class Stats(@SerializedName("title") val title: String,
                     @SerializedName("topic") val topic: Topic,
                     @SerializedName("goods") val good: Int,
                     @SerializedName("soso") val soso: Int,
                     @SerializedName("bad") val bad: Int): Serializable

    data class Topic(@SerializedName("type") val type: String,
                     @SerializedName("breed") val breed: String,
                     @SerializedName("images") val images: ArrayList<String>?): Serializable
}
