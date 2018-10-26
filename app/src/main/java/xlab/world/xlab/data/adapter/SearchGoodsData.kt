package xlab.world.xlab.data.adapter

import java.io.Serializable

data class SearchGoodsData(var total:Int = -1,
                           var nextPage: Int = 1,
                           var isLoading: Boolean = true,
                           val items: ArrayList<SearchGoodsListData> = ArrayList()): Serializable {

    fun updateData(searchGoodsData: SearchGoodsData) {
        items.clear()
        searchGoodsData.items.forEach {
            items.add(it.copy())
        }

        isLoading = false
        total = searchGoodsData.total
        nextPage = 2
    }

    fun addData(searchGoodsData: SearchGoodsData) {
        searchGoodsData.items.forEach {
            items.add(it.copy())
        }

        isLoading = false
        nextPage += 1
    }
}

