package xlab.world.xlab.data.response

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ResUpdateTopicToggleData(@SerializedName("message") val message: String,
                                    @SerializedName("isHidden") val isHidden: Boolean): Serializable