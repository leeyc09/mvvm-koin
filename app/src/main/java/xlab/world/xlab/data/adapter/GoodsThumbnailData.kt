package xlab.world.xlab.data.adapter

import java.io.Serializable

data class GoodsThumbnailData(var total:Int = -1,
                              var nextPage: Int = 1,
                              var isLoading: Boolean = true,
                              val items: ArrayList<GoodsThumbnailListData> = ArrayList()): Serializable {

    fun updateData(goodsThumbnailData: GoodsThumbnailData) {
        items.clear()
        goodsThumbnailData.items.forEach {
            items.add(it.copy())
        }

        isLoading = false
        total = goodsThumbnailData.total
        nextPage = 2
    }

    fun addData(goodsThumbnailData: GoodsThumbnailData) {
        goodsThumbnailData.items.forEach {
            items.add(it.copy())
        }

        isLoading = false
        nextPage += 1
    }
}
