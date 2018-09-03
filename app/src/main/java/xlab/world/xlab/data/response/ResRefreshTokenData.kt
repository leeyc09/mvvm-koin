package xlab.world.xlab.data.response

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ResRefreshTokenData(@SerializedName("refreshToken") val refreshToken: String): Serializable