package xlab.world.xlab.data.adapter

import java.io.Serializable

data class PostUploadPictureData(val items: ArrayList<PostUploadPictureListData> = ArrayList()): Serializable {

    fun updateData(postUploadPictureData: PostUploadPictureData) {
        items.clear()
        postUploadPictureData.items.forEach {
            items.add(it.copy())
        }
    }
}
