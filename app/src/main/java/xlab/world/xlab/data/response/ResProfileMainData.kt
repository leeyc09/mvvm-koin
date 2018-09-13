package xlab.world.xlab.data.response

import com.google.gson.annotations.SerializedName
import xlab.world.xlab.utils.support.AppConstants
import java.io.Serializable

data class ResProfileMainData(@SerializedName("message") val message: String,
                              @SerializedName("profileImg") val profileImg: String,
                              @SerializedName("nickName") val nickName: String,
                              @SerializedName("introduction") val introduction: String,
                              @SerializedName("followingTotal") val followingCnt: Int,
                              @SerializedName("followerTotal") val followerCnt: Int,
                              @SerializedName("isFollowing") val isFollowing: Boolean): Serializable