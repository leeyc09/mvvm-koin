package xlab.world.xlab.data.adapter

import xlab.world.xlab.utils.support.AppConstants
import java.io.Serializable

data class ExploreFeedListData(val dataType: Int,
                               val imageURL: String?,
                               val goodsCd: String = "",
                               val postId: String = "",
                               val postsType: Int = AppConstants.POSTS_IMAGE,
                               val youTubeVideoID: String = ""): Serializable
