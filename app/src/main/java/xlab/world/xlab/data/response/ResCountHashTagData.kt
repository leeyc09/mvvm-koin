package xlab.world.xlab.data.response

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ResCountHashTagData(@SerializedName("message") val message: String,
                               @SerializedName("total") val total: Int,
                               @SerializedName("hashTags") val hashTagData: ArrayList<TagData>?): Serializable {

    data class TagData(@SerializedName("tagName") val tagName: String,
                       @SerializedName("count") val count: Int): Serializable
}