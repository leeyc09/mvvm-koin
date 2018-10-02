package xlab.world.xlab.data.adapter

import java.io.Serializable

data class GoodsKeywordListData(val dataType: Int,
                                val keywordText: String = "",
                                val keywordCode: String = ""): Serializable
