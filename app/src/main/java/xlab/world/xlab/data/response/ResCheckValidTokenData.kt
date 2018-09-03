package xlab.world.xlab.data.response

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ResCheckValidTokenData(@SerializedName("message") val message: String,
                                  @SerializedName("accessToken") val accessToken: String,
                                  @SerializedName("type") val loginType: Int,
                                  @SerializedName("userID") val userID: String,
                                  @SerializedName("socialID") val socialID: String,
                                  @SerializedName("level") val userLevel: Int,
                                  @SerializedName("email") val email: String,
                                  @SerializedName("push") val isPushAlarmOn: Boolean): Serializable