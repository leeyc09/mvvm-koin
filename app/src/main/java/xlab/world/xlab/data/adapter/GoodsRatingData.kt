package xlab.world.xlab.data.adapter

import java.io.Serializable

data class GoodsRatingData(var total:Int = -1,
                           var nextPage: Int = 1,
                           var isLoading: Boolean = true,
                           val items: ArrayList<GoodsRatingListData> = ArrayList()): Serializable {

    fun updateData(goodsRatingData: GoodsRatingData) {
        items.clear()
        goodsRatingData.items.forEach {
            items.add(it.copy())
        }

        isLoading = false
        total = goodsRatingData.total
        nextPage = 2
    }

    fun addData(goodsRatingData: GoodsRatingData) {
        goodsRatingData.items.forEach {
            items.add(it.copy())
        }

        isLoading = false
        nextPage += 1
    }
}
