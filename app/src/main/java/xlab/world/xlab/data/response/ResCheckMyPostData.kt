package xlab.world.xlab.data.response

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ResCheckMyPostData(@SerializedName("message") val message: String,
                              @SerializedName("profileImg") val profileImg: String,
                              @SerializedName("isMyPost") val isMyPost: Boolean): Serializable