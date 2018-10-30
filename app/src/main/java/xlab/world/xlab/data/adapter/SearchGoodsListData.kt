package xlab.world.xlab.data.adapter

import java.io.Serializable

data class SearchGoodsListData(val dataType: Int,
                               var sortType: Int,
                               val goodsCd: String = "",
                               val imageURL: String = "",
                               val price: Int = 0,
                               val title: String = "",
                               val brand: String = "",
                               var showQuestionMark: Boolean = false,
                               val matchingPercent: Int = 0,
                               val matchColor: Int = 0,
                               var withAnimation: Boolean = true): Serializable
