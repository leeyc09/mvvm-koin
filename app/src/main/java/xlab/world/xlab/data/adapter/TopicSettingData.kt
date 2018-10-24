package xlab.world.xlab.data.adapter

import java.io.Serializable

data class TopicSettingData(var total:Int = -1,
                            var nextPage: Int = 1,
                            var isLoading: Boolean = true,
                            val items: ArrayList<TopicSettingListData> = ArrayList()): Serializable {

    fun updateData(topicSettingData: TopicSettingData) {
        items.clear()
        topicSettingData.items.forEach {
            items.add(it.copy())
        }

        isLoading = false
        total = topicSettingData.total
        nextPage = 2
    }

    fun addData(topicSettingData: TopicSettingData) {
        topicSettingData.items.forEach {
            items.add(it.copy())
        }

        isLoading = false
        nextPage += 1
    }
}
