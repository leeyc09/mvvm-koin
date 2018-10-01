package xlab.world.xlab.data.response

import com.google.gson.annotations.SerializedName
import xlab.world.xlab.utils.support.AppConstants
import java.io.Serializable

data class ResNoticeData(@SerializedName("message") val message: String,
                         @SerializedName("total") val total: Int,
                         @SerializedName("notices") val notices: ArrayList<Notice>?): Serializable {

    data class Notice(@SerializedName("id") val id: String,
                      @SerializedName("title") val title: String,
                      @SerializedName("content") val content: String,
                      @SerializedName("isRead") val isRead: Boolean,
                      @SerializedName("uploadYear") val year: Int,
                      @SerializedName("uploadMonth") val month: Int,
                      @SerializedName("uploadDay") val day: Int): Serializable
}
