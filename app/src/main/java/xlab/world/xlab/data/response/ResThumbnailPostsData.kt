package xlab.world.xlab.data.response

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ResThumbnailPostsData(@SerializedName("message") val message: String,
                                 @SerializedName("total") val total: Int,
                                 @SerializedName(value = "posts", alternate = ["postsData", "likedPost", "savedPost"]) val postsData: ArrayList<PostsThumb>?): Serializable {

    data class PostsThumb(@SerializedName(value = "id", alternate = ["postId"]) val id: String,
                          @SerializedName("postType") val postType: Int,
                          @SerializedName("postFile") val postFile: ArrayList<String>,
                          @SerializedName("youTubeVideoID") val youTubeVideoID: String): Serializable

}