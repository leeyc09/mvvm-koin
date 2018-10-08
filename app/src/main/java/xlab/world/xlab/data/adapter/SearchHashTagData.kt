package xlab.world.xlab.data.adapter

import java.io.Serializable

data class SearchHashTagData(var total:Int = -1,
                             var nextPage: Int = 1,
                             var isLoading: Boolean = true,
                             var searchText: String = "",
                             val items: ArrayList<SearchHashTagListData> = ArrayList()): Serializable
