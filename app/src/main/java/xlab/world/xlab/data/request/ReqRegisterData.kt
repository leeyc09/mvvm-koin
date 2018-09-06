package xlab.world.xlab.data.request

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ReqRegisterData(@SerializedName("type") val loginType: Int,
                           @SerializedName("email") val email:String = "",
                           @SerializedName("password") val password:String = "",
                           @SerializedName("nickName") val nickName:String = "",
                           @SerializedName("socialID") val socialID: String): Serializable
