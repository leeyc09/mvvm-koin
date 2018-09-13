package xlab.world.xlab.data.adapter

import java.io.Serializable

data class CommentListData(val userId: String,
                           val userProfileUrl: String,
                           val userNickName: String,
                           val uploadYear: Int,
                           val uploadMonth: Int,
                           val uploadDay: Int,
                           val uploadHour: Int,
                           val uploadMinute: Int,
                           val comment: String): Serializable
