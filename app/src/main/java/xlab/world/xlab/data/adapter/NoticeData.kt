package xlab.world.xlab.data.adapter

import java.io.Serializable

data class NoticeData(var total:Int = -1,
                      var nextPage: Int = 1,
                      var isLoading: Boolean = true,
                      val items: ArrayList<NoticeListData> = ArrayList()): Serializable {

    fun updateData(noticeData: NoticeData) {
        items.clear()
        noticeData.items.forEach {
            items.add(it.copy())
        }

        isLoading = false
        total = noticeData.total
        nextPage = 2
    }

    fun addData(noticeData: NoticeData) {
        noticeData.items.forEach {
            items.add(it.copy())
        }

        isLoading = false
        nextPage += 1
    }
}
