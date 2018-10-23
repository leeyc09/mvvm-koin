package xlab.world.xlab.data.adapter

import java.io.Serializable

data class CartData(var total:Int = -1,
                    var nextPage: Int = 1,
                    var isLoading: Boolean = true,
                    val items: ArrayList<CartListData> = ArrayList()): Serializable {

    fun updateData(cartData: CartData) {
        items.clear()
        cartData.items.forEach {
            items.add(it.copy())
        }

        isLoading = false
        total = cartData.total
        nextPage = 2
    }

    fun addData(cartData: CartData) {
        cartData.items.forEach {
            items.add(it.copy())
        }

        isLoading = false
        nextPage += 1
    }

    fun removeData(index: Int) {
        if (index in 0 until items.size) {
            items.removeAt(index)

            total -= 1
        }

        isLoading = false
    }
}
