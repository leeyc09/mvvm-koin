package xlab.world.xlab.data.response

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ResProfileEditData(@SerializedName("message") val message: String = "",
                              @SerializedName("profileImg") val profileImg: String = "",
                              @SerializedName("nickName") var nickName: String = "",
                              @SerializedName("introduction") var introduction: String = "",
                              @SerializedName("gender") var gender: Int = -1,
                              @SerializedName("locale") var locale: String = "",
                              @SerializedName("birthYear") var birthYear: String = ""): Serializable
