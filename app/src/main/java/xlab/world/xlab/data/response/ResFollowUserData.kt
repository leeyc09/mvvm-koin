package xlab.world.xlab.data.response

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ResFollowUserData(@SerializedName("message") val message: String,
                             @SerializedName("total") val total: Int,
                             @SerializedName("follower") val follower:  ArrayList<FollowData>?,
                             @SerializedName("following") val following:  ArrayList<FollowData>?): Serializable {

    data class FollowData(@SerializedName("id") val id: String,
                          @SerializedName("nickName") val nickName: String,
                          @SerializedName("profileImg") val profileImg: String,
                          @SerializedName("petInfo") val petInfo: ArrayList<FollowPetInfo>?,
                          @SerializedName("isFollowing") val isFollowing: Boolean): Serializable

    data class FollowPetInfo(@SerializedName("type") val type: String,
                             @SerializedName("breed") val breed: String): Serializable

}
