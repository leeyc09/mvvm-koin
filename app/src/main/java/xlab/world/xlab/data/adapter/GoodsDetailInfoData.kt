package xlab.world.xlab.data.adapter

import java.io.Serializable

data class GoodsDetailInfoData(val items: ArrayList<GoodsDetailInfoListData> = ArrayList()): Serializable {
    fun updateData(goodsDetailInfoData: GoodsDetailInfoData) {
        items.clear()
        goodsDetailInfoData.items.forEach {
            items.add(it.copy())
        }
    }
}
