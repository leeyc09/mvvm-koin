package xlab.world.xlab.data.response

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ResExistNewData(@SerializedName("message") val message: String,
                           @SerializedName(value = "existNewNoti", alternate = ["existNewNotice"]) val existNew: Boolean): Serializable