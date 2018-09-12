package xlab.world.xlab.data.adapter

import java.io.Serializable

data class SearchGoodsData(var total:Int = -1,
                           var nextPage: Int = 1,
                           var isLoading: Boolean = true,
                           val items: ArrayList<SearchGoodsListData> = ArrayList()): Serializable {
    override fun toString(): String {
        return "total: $total / nextPage: $nextPage / isLoading: $isLoading / items.size: ${items.size}"
    }
}
