package xlab.world.xlab.data.response

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ResUserDefaultData(@SerializedName("message") val message: String,
                              @SerializedName("total") val total: Int,
                              @SerializedName(value = "users", alternate = ["follower", "following", "recommendUser"]) val userData: ArrayList<UserData>?): Serializable {

    data class UserData(@SerializedName("id") val id: String,
                        @SerializedName("nickName") val nickName: String,
                        @SerializedName("profileImg") val profileImg: String,
                        @SerializedName("petInfo") val petData: ArrayList<UserPetInfo>?,
                        @SerializedName(value = "isFollowing", alternate = ["isFollowed"]) val isFollowing: Boolean): Serializable

    data class UserPetInfo(@SerializedName("type") val type: String,
                           @SerializedName("breed") val breed: String): Serializable

}
