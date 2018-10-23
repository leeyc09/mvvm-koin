package xlab.world.xlab.data.adapter

import java.io.Serializable

data class GalleryData(var total:Int = -1,
                       var nextPage: Int = 1,
                       var isLoading: Boolean = true,
                       val items: ArrayList<GalleryListData> = ArrayList()): Serializable {

    fun updateData(galleryData: GalleryData) {
        items.clear()
        galleryData.items.forEach {
            items.add(it.copy())
        }

        isLoading = false
        total = galleryData.total
        nextPage = 2
    }

    fun addData(galleryData: GalleryData) {
        galleryData.items.forEach {
            items.add(it.copy())
        }

        isLoading = false
        nextPage += 1
    }
}
