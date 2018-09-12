package xlab.world.xlab.data.adapter

import java.io.Serializable

data class ShopFeedListData(val dataType: Int,
                            val categoryText: String = "",
                            val categoryCode: String = "",
                            var goodsData: SearchGoodsData = SearchGoodsData()): Serializable {
    override fun toString(): String {
        return "dataType: $dataType / categoryText: $categoryText / categoryCode: $categoryCode / goodsData: $goodsData"
    }
}
