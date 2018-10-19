package xlab.world.xlab.data.response

import com.google.gson.annotations.SerializedName
import xlab.world.xlab.utils.support.AppConstants
import java.io.Serializable

data class ResGoodsSimpleData(@SerializedName("message") val message: String,
                              @SerializedName("image") val image: String,
                              @SerializedName("name") val name: String,
                              @SerializedName("brandName") val brand: String): Serializable
