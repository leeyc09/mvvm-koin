package xlab.world.xlab.data.response

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ResSocialNotificationData(@SerializedName("message") val message: String,
                                     @SerializedName("newNoti") val newNotification: Boolean,
                                     @SerializedName("total") val total: Int,
                                     @SerializedName("notification") val notification: ArrayList<Notification>?): Serializable {

    data class Notification(@SerializedName("type") val type: Int,
                            @SerializedName("userId") val userId: String,
                            @SerializedName("fromUserId") val fromUserId: String,
                            @SerializedName("fromUserProfile") val fromUserProfile: String,
                            @SerializedName("fromUserNick") val fromUserNick: String,
                            @SerializedName("postId") val postId: String,
                            @SerializedName("postType") val postType: Int,
                            @SerializedName("postImage") val postImage: String,
                            @SerializedName("youTubeVideoID") val youTubeVideoID: String,
                            @SerializedName("comment") val comment: String,
                            @SerializedName("notiYear") val year: Int,
                            @SerializedName("notiMonth") val month: Int,
                            @SerializedName("notiDay") val day: Int,
                            @SerializedName("notiHour") val hour: Int,
                            @SerializedName("notiMin") val minute: Int): Serializable
}