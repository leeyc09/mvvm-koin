package xlab.world.xlab.data.request

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ReqUserReportData(@SerializedName("respondentID") val respondentID: String,
                             @SerializedName("content") val content: String = "기본신고"): Serializable