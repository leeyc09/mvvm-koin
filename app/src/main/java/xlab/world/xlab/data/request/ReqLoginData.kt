package xlab.world.xlab.data.request

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ReqLoginData(@SerializedName("type") val loginType: Int,
                        @SerializedName("email") val email:String = "",
                        @SerializedName("password") val password:String = "",
                        @SerializedName("accessToken") val socialToken:String = "",
                        @SerializedName("fcmToken") val fcmToken: String): Serializable
