package xlab.world.xlab.data.adapter

import xlab.world.xlab.data.response.PostDetailData
import xlab.world.xlab.utils.support.AppConstants
import java.io.Serializable

data class PostDetailListData(val dataType: Int,
                              val postType: Int = 0,
                              val postId: String = "",
                              val userId: String = "",
                              val userProfileURL: String = "",
                              val userNickName: String = "",
                              var isFollowing: Boolean = false,
                              val uploadYear: Int = 0,
                              val uploadMonth: Int = 0,
                              val uploadDay: Int = 0,
                              val uploadHour: Int = 0,
                              val uploadMinute: Int = 0,
                              val imageURL: ArrayList<String> = ArrayList(),
                              var lastImageIndex: Int = 0,
                              val youTubeVideoID: String = "",
                              var likeNum: Int = 0,
                              val commentsNum: Int = 0,
                              var content: String = "",
                              val contentOrigin: String = "",
                              var showAllContent: Boolean = false,
                              val goodsList: ArrayList<PostDetailData.Goods> = ArrayList(),
                              var isLike: Boolean = false,
                              var isSave: Boolean = false,
                              var hideContent: Boolean = false,
                              val isMyPost: Boolean = false): Serializable {

}
