package xlab.world.xlab.data.adapter

import xlab.world.xlab.utils.support.AppConstants
import java.io.Serializable

data class AllFeedListData(val dataType: Int,
                           val imageURL: String?,
                           val goodsCd: String = "",
                           var showQuestionMark: Boolean = false,
                           val matchingPercent: Int = 0,
                           val matchColor: Int = 0,
                           val postId: String = "",
                           val postsType: Int = AppConstants.POSTS_IMAGE,
                           val youTubeVideoID: String = ""): Serializable
