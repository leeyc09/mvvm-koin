package xlab.world.xlab.data.adapter

import java.io.Serializable

data class GoodsDetailTopicMatchData(var total:Int = -1,
                                     var nextPage: Int = 1,
                                     var isLoading: Boolean = true,
                                     val items: ArrayList<GoodsDetailTopicMatchListData> = ArrayList()): Serializable {
    fun updateData(goodsDetailTopicMatchData: GoodsDetailTopicMatchData) {
        items.clear()
        goodsDetailTopicMatchData.items.forEach {
            items.add(it.copy())
        }

        isLoading = false
        total = goodsDetailTopicMatchData.total
        nextPage = 2
    }

    fun addData(goodsDetailTopicMatchData: GoodsDetailTopicMatchData) {
        goodsDetailTopicMatchData.items.forEach {
            items.add(it.copy())
        }

        isLoading = false
        nextPage += 1
    }
}
