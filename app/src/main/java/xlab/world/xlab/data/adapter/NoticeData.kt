package xlab.world.xlab.data.adapter

import java.io.Serializable

data class NoticeData(var total:Int = -1,
                      var nextPage: Int = 1,
                      var isLoading: Boolean = true,
                      val items: ArrayList<NoticeListData> = ArrayList()): Serializable
