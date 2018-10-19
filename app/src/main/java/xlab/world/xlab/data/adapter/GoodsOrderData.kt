package xlab.world.xlab.data.adapter

import java.io.Serializable

data class GoodsOrderData(var total:Int = -1,
                          var nextPage: Int = 1,
                          var isLoading: Boolean = true,
                          val items: ArrayList<GoodsOrderListData> = ArrayList()): Serializable {

    fun updateData(goodsOrderData: GoodsOrderData) {
        items.clear()
        goodsOrderData.items.forEach {
            items.add(it.copy())
        }

        isLoading = false
        total = goodsOrderData.total
        nextPage = 2
    }

    fun addData(goodsOrderData: GoodsOrderData) {
        goodsOrderData.items.forEach {
            items.add(it.copy())
        }

        isLoading = false
        nextPage += 1
    }
}
