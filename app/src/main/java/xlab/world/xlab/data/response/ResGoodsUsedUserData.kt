package xlab.world.xlab.data.response

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ResGoodsUsedUserData(@SerializedName("message") val message: String,
                                @SerializedName("total") val total: Int,
                                @SerializedName("users") val userData: ArrayList<User>?): Serializable {

    data class User(@SerializedName("userId") val id: String,
                     @SerializedName("nickName") val nickName: String,
                     @SerializedName("profileImg") val profileImg: String): Serializable
}
