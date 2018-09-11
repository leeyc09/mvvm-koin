package xlab.world.xlab.data.response

import com.google.gson.annotations.SerializedName
import xlab.world.xlab.utils.support.AppConstants
import java.io.Serializable

data class ResDetailPostsData(@SerializedName("message") val message: String,
                              @SerializedName("total") val total: Int,
                              @SerializedName("postDetailList") val postsData: ArrayList<PostDetailData>?): Serializable {
}
