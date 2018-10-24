package xlab.world.xlab.data.adapter

import java.io.Serializable

data class PostThumbnailData(var total:Int = -1,
                             var nextPage: Int = 1,
                             var isLoading: Boolean = true,
                             val items: ArrayList<PostThumbnailListData> = ArrayList()): Serializable {

    fun updateData(postThumbnailData: PostThumbnailData) {
        items.clear()
        postThumbnailData.items.forEach {
            items.add(it.copy())
        }

        isLoading = false
        total = postThumbnailData.total
        nextPage = 2
    }

    fun addData(postThumbnailData: PostThumbnailData) {
        postThumbnailData.items.forEach {
            items.add(it.copy())
        }

        isLoading = false
        nextPage += 1
    }
}
