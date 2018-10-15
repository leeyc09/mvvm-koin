package xlab.world.xlab.data.adapter

import java.io.Serializable

data class CartData(var total:Int = -1,
                    var nextPage: Int = 1,
                    var isLoading: Boolean = true,
                    val items: ArrayList<CartListData> = ArrayList()): Serializable
