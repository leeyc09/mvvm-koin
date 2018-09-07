package xlab.world.xlab.data.request

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ReqNewPasswordData(@SerializedName("password") val password: String): Serializable