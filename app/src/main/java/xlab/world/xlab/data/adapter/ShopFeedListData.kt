package xlab.world.xlab.data.adapter

import java.io.Serializable

data class ShopFeedListData(val dataType: Int,
                            val categoryText: String = "",
                            val categoryCode: String = "",
                            var goodsData: SearchGoodsData = SearchGoodsData()): Serializable
