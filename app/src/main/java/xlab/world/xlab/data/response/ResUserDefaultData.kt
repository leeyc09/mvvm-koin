package xlab.world.xlab.data.response

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ResUserDefaultData(@SerializedName("message") val message: String,
                              @SerializedName("total") val total: Int,
                              @SerializedName("follower") val follower: ArrayList<UserData>?,
                              @SerializedName("following") val following: ArrayList<UserData>?,
                              @SerializedName("recommendUser") val recommend: ArrayList<UserData>?,
                              @SerializedName("users") val searchUsers: ArrayList<UserData>?): Serializable {

    data class UserData(@SerializedName("id") val id: String,
                        @SerializedName("nickName") val nickName: String,
                        @SerializedName("profileImg") val profileImg: String,
                        @SerializedName("petInfo") val petData: ArrayList<FollowUserPetInfo>?,
                        @SerializedName("isFollowing") val isFollowing: Boolean): Serializable

    data class FollowUserPetInfo(@SerializedName("type") val type: String,
                                 @SerializedName("breed") val breed: String): Serializable

}
