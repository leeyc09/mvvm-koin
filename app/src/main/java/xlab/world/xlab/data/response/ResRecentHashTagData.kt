package xlab.world.xlab.data.response

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ResRecentHashTagData(@SerializedName("message") val message: String,
                                @SerializedName("recentTagHistory") val hashTagList: ArrayList<String>?): Serializable