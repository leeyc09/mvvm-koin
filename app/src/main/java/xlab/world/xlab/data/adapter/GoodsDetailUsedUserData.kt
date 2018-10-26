package xlab.world.xlab.data.adapter

import java.io.Serializable

data class GoodsDetailUsedUserData(var total:Int = -1,
                                   var nextPage: Int = 1,
                                   var isLoading: Boolean = true,
                                   val items: ArrayList<GoodsDetailUsedUserListData> = ArrayList()): Serializable {

    fun updateData(goodsDetailUsedUserData: GoodsDetailUsedUserData) {
        items.clear()
        goodsDetailUsedUserData.items.forEach {
            items.add(it.copy())
        }

        isLoading = false
        total = goodsDetailUsedUserData.total
        nextPage = 2
    }

    fun addData(goodsDetailUsedUserData: GoodsDetailUsedUserData) {
        goodsDetailUsedUserData.items.forEach {
            items.add(it.copy())
        }

        isLoading = false
        nextPage += 1
    }
}
