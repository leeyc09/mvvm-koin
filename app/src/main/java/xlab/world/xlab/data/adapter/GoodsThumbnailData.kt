package xlab.world.xlab.data.adapter

import java.io.Serializable

data class GoodsThumbnailData(var total:Int = -1,
                              var nextPage: Int = 1,
                              var isLoading: Boolean = true,
                              val items: ArrayList<GoodsThumbnailListData> = ArrayList()): Serializable
