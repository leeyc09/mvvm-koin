package xlab.world.xlab.data.adapter

import java.io.Serializable

data class GoodsDetailRatingData(var total:Int = -1,
                                 var nextPage: Int = 1,
                                 var isLoading: Boolean = true,
                                 val items: ArrayList<GoodsDetailRatingListData> = ArrayList()): Serializable {

    fun updateData(goodsDetailRatingData: GoodsDetailRatingData) {
        items.clear()
        goodsDetailRatingData.items.forEach {
            items.add(it.copy())
        }

        isLoading = false
        total = goodsDetailRatingData.total
        nextPage = 2
    }

    fun addData(goodsDetailRatingData: GoodsDetailRatingData) {
        goodsDetailRatingData.items.forEach {
            items.add(it.copy())
        }

        isLoading = false
        nextPage += 1
    }
}
