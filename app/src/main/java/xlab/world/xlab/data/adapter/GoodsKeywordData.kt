package xlab.world.xlab.data.adapter

import java.io.Serializable

data class GoodsKeywordData(var total:Int = -1,
                            var nextPage: Int = 1,
                            var isLoading: Boolean = true,
                            val items: ArrayList<GoodsKeywordListData> = ArrayList()): Serializable {
    fun updateData(goodsKeywordData: GoodsKeywordData) {
        items.clear()
        goodsKeywordData.items.forEach {
            items.add(it.copy())
        }

        isLoading = false
        total = goodsKeywordData.total
        nextPage = 2
    }

    fun addData(goodsKeywordData: GoodsKeywordData) {
        goodsKeywordData.items.forEach {
            items.add(it.copy())
        }

        isLoading = false
        nextPage += 1
    }
}
