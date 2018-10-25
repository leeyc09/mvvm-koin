package xlab.world.xlab.data.adapter

import java.io.Serializable

data class PostDetailData(var total:Int = -1,
                          var nextPage: Int = 1,
                          var isLoading: Boolean = true,
                          val items: ArrayList<PostDetailListData> = ArrayList()): Serializable {

    fun updateData(postDetailData: PostDetailData) {
        items.clear()
        postDetailData.items.forEach {
            items.add(it.copy())
        }

        isLoading = false
        total = postDetailData.total
        nextPage = 2
    }

    fun addData(postDetailData: PostDetailData) {
        postDetailData.items.forEach {
            items.add(it.copy())
        }

        isLoading = false
        nextPage += 1
    }
}
