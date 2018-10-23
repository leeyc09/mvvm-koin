package xlab.world.xlab.data.adapter

import java.io.Serializable

data class SelectUsedGoodsData(var total:Int = -1,
                               var nextPage: Int = 1,
                               var isLoading: Boolean = true,
                               val items: ArrayList<SelectUsedGoodsListData> = ArrayList()): Serializable {
    fun updateData(selectUsedGoodsData: SelectUsedGoodsData) {
        items.clear()
        selectUsedGoodsData.items.forEach {
            items.add(it.copy())
        }

        isLoading = false
        total = selectUsedGoodsData.total
        nextPage = 2
    }

    fun addData(selectUsedGoodsData: SelectUsedGoodsData) {
        selectUsedGoodsData.items.forEach {
            items.add(it.copy())
        }

        isLoading = false
        nextPage += 1
    }

    fun removeData(index: Int) {
        items.removeAt(index)

        total -= 1

        isLoading = false
    }
}
