package xlab.world.xlab.data.adapter

import java.io.Serializable

data class ProfileTopicData(var total:Int = -1,
                            var nextPage: Int = 1,
                            var isLoading: Boolean = true,
                            val items: ArrayList<ProfileTopicListData> = ArrayList()): Serializable {

    fun updateData(profileTopicData: ProfileTopicData) {
        items.clear()
        profileTopicData.items.forEach {
            items.add(it.copy())
        }

        isLoading = false
        total = profileTopicData.total
        nextPage = 2
    }

    fun addData(profileTopicData: ProfileTopicData) {
        profileTopicData.items.forEach {
            items.add(it.copy())
        }

        isLoading = false
        nextPage += 1
    }
}
