package xlab.world.xlab.data.adapter

import xlab.world.xlab.data.response.PostDetailData
import xlab.world.xlab.utils.support.AppConstants
import java.io.Serializable

data class PostDetailListData(val dataType: Int,
                              val postType: Int,
                              val postId: String,
                              val userId: String,
                              val userProfileURL: String,
                              val userNickName: String,
                              var isFollowing: Boolean,
                              val uploadYear: Int,
                              val uploadMonth: Int,
                              val uploadDay: Int,
                              val uploadHour: Int,
                              val uploadMinute: Int,
                              val imageURL: ArrayList<String>,
                              var lastImageIndex: Int = 0,
                              val youTubeVideoID: String,
                              var likeNum: Int,
                              val commentsNum: Int,
                              var content: String,
                              val contentOrigin: String,
                              var showAllContent: Boolean = false,
                              val goodsList: ArrayList<PostDetailData.Goods>,
                              var isLike: Boolean,
                              var isSave: Boolean,
                              var hideContent: Boolean = false,
                              val isMyPost: Boolean): Serializable
