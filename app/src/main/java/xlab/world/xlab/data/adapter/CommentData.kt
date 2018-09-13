package xlab.world.xlab.data.adapter

import java.io.Serializable

data class CommentData(var total:Int = -1,
                       var nextPage: Int = 1,
                       var isLoading: Boolean = true,
                       val items: ArrayList<CommentListData> = ArrayList()): Serializable
