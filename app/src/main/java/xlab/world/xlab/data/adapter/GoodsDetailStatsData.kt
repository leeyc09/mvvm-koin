package xlab.world.xlab.data.adapter

import java.io.Serializable

data class GoodsDetailStatsData(var total:Int = -1,
                                var nextPage: Int = 1,
                                var isLoading: Boolean = true,
                                val items: ArrayList<GoodsDetailStatsListData> = ArrayList()): Serializable {
    fun updateData(goodsDetailStatsData: GoodsDetailStatsData) {
        items.clear()
        goodsDetailStatsData.items.forEach {
            items.add(it.copy())
        }

        isLoading = false
        total = goodsDetailStatsData.total
        nextPage = 2
    }
}
