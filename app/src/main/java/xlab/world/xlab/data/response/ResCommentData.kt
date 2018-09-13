package xlab.world.xlab.data.response

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ResCommentData(@SerializedName("message") val message: String,
                          @SerializedName("total") val total: Int,
                          @SerializedName("comments") val comments: ArrayList<Comment>?): Serializable {

    data class Comment(@SerializedName("userId") val userId: String,
                       @SerializedName("nickName") val nickName: String,
                       @SerializedName("profileImg") val profileImg: String,
                       @SerializedName("content") val content: String,
                       @SerializedName("uploadYear") val uploadYear: Int,
                       @SerializedName("uploadMonth") val uploadMonth: Int,
                       @SerializedName("uploadDay") val uploadDay: Int,
                       @SerializedName("uploadHour") val uploadHour: Int,
                       @SerializedName("uploadMinute") val uploadMinute: Int): Serializable

}