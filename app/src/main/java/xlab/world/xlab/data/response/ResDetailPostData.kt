package xlab.world.xlab.data.response

import com.google.gson.annotations.SerializedName
import xlab.world.xlab.utils.support.AppConstants
import java.io.Serializable

data class ResDetailPostData(@SerializedName("message") val message: String,
                             @SerializedName("postDetail") val postsData: PostDetailData): Serializable {
}
