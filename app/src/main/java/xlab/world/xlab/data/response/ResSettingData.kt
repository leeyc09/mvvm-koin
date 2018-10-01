package xlab.world.xlab.data.response

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ResSettingData(@SerializedName("message") val message: String,
                          @SerializedName("userSetting") val setting: Setting): Serializable {

    data class Setting(@SerializedName("onBoarding") val onBoarding: Boolean,
                       @SerializedName("push") val push: Boolean): Serializable
}