package xlab.world.xlab.data.adapter

import xlab.world.xlab.data.response.PostDetailData
import xlab.world.xlab.utils.support.AppConstants
import java.io.Serializable

data class GalleryListData(val dataType: Int = AppConstants.GALLERY_ONE,
                           val id: String,
                           val title: String,
                           val data: String,
                           val size: String,
                           val displayName: String,
                           val duration: String = "",
                           val selected: Boolean = false,
                           val isPreview: Boolean = false): Serializable

