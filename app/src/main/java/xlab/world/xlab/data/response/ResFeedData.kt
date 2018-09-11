package xlab.world.xlab.data.response

import com.google.gson.annotations.SerializedName
import xlab.world.xlab.utils.support.AppConstants
import java.io.Serializable

data class ResFeedData(@SerializedName("data") val feedData: ArrayList<FeedData>?): Serializable {

    data class FeedData(@SerializedName("dataType") val dataType: Int,
                        @SerializedName("posts") val posts: PostData?,
                        @SerializedName("goods") val goods: GoodsData?): Serializable

    data class PostData(@SerializedName("id") val id: String,
                        @SerializedName("postType") val postType: Int,
                        @SerializedName("postFile") val postFile: ArrayList<String>,
                        @SerializedName("youTubeVideoID") val youTubeVideoID: String): Serializable

    data class GoodsData(@SerializedName("code") val code: String,
                         @SerializedName("image") val image: String,
                         @SerializedName("topicMatch") val topicMatch: ArrayList<TopicMatch>?): Serializable

    data class TopicMatch(@SerializedName("topicId") val topicId: String,
                          @SerializedName("topicColor") val topicColor: String,
                          @SerializedName("match") val match: Int): Serializable
}
