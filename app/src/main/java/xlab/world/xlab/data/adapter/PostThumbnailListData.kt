package xlab.world.xlab.data.adapter

import xlab.world.xlab.data.response.PostDetailData
import xlab.world.xlab.utils.support.AppConstants
import java.io.Serializable

data class PostThumbnailListData(val dataType: Int,
                                 val postType: Int = 0,
                                 val postId: String = "",
                                 val imageURL: String? = null,
                                 val youTubeVideoID: String = ""): Serializable

