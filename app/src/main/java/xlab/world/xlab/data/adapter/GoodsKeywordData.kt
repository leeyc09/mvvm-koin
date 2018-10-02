package xlab.world.xlab.data.adapter

import java.io.Serializable

data class GoodsKeywordData(var total:Int = -1,
                            var nextPage: Int = 1,
                            var isLoading: Boolean = true,
                            val items: ArrayList<GoodsKeywordListData> = ArrayList()): Serializable
