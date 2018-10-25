package xlab.world.xlab.data.adapter

import java.io.Serializable

data class CommentData(var total:Int = -1,
                       var nextPage: Int = 1,
                       var isLoading: Boolean = true,
                       val items: ArrayList<CommentListData> = ArrayList()): Serializable {

    fun updateData(commentData: CommentData) {
        items.clear()
        commentData.items.forEach {
            items.add(it.copy())
        }

        isLoading = false
        total = commentData.total
        nextPage = 2
    }

    fun addData(commentData: CommentData) {
        commentData.items.forEach {
            items.add(it.copy())
        }

        isLoading = false
        nextPage += 1
    }
}
