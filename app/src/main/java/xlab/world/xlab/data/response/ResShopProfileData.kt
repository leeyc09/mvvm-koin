package xlab.world.xlab.data.response

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ResShopProfileData(@SerializedName("message") val message: String = "",
                              @SerializedName("name") var name: String = "",
                              @SerializedName("email") var email: String = ""): Serializable