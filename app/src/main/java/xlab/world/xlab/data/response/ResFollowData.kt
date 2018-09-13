package xlab.world.xlab.data.response

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ResFollowData(@SerializedName("message") val message: String,
                         @SerializedName("isFollowing") val status: Boolean): Serializable