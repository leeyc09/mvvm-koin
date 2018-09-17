package xlab.world.xlab.data.response

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ResProfileEditData(@SerializedName("message") val message: String,
                              @SerializedName("profileImg") val profileImg: String,
                              @SerializedName("nickName") val nickName: String,
                              @SerializedName("introduction") val introduction: String,
                              @SerializedName("gender") val gender: Int,
                              @SerializedName("locale") val locale: String,
                              @SerializedName("birthYear") val birthYear: String): Serializable
