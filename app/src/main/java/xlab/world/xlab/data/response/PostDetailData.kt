package xlab.world.xlab.data.response

import com.google.gson.annotations.SerializedName
import xlab.world.xlab.utils.support.AppConstants
import java.io.Serializable

data class PostDetailData(@SerializedName("id") val id: String,
                          @SerializedName("userId") val userId: String,
                          @SerializedName("postType") val postType: Int,
                          @SerializedName("nickName") val nickName: String,
                          @SerializedName("profileImg") val profileImg: String,
                          @SerializedName("uploadYear") val uploadYear: Int,
                          @SerializedName("uploadMonth") val uploadMonth: Int,
                          @SerializedName("uploadDay") val uploadDay: Int,
                          @SerializedName("uploadHour") val uploadHour: Int,
                          @SerializedName("uploadMinute") val uploadMinute: Int,
                          @SerializedName("postFile") val postFile: ArrayList<String>,
                          @SerializedName("youTubeVideoID") val youTubeVideoID: String,
                          @SerializedName("isLiked") var isLiked: Boolean,
                          @SerializedName("likedCount") val likedCount: Int,
                          @SerializedName("commentCount") val commentCount: Int,
                          @SerializedName("isSaved") val isSaved: Boolean,
                          @SerializedName("isFollowed") val isFollowed: Boolean,
                          @SerializedName("content") val content: String,
                          @SerializedName("goods") val goods: ArrayList<Goods>): Serializable {

    data class Goods(@SerializedName("code") val code: String,
                     @SerializedName("image") val image: String,
                     @SerializedName("name") val name: String,
                     @SerializedName("brand") val brand: String): Serializable
}
