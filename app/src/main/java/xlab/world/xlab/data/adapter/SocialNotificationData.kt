package xlab.world.xlab.data.adapter

import java.io.Serializable

data class SocialNotificationData(var total:Int = -1,
                                  var nextPage: Int = 1,
                                  var isLoading: Boolean = true,
                                  val items: ArrayList<SocialNotificationListData> = ArrayList()): Serializable {

    fun updateData(socialNotificationData: SocialNotificationData) {
        items.clear()
        socialNotificationData.items.forEach {
            items.add(it.copy())
        }

        isLoading = false
        total = socialNotificationData.total
        nextPage = 2
    }

    fun addData(socialNotificationData: SocialNotificationData) {
        socialNotificationData.items.forEach {
            items.add(it.copy())
        }

        isLoading = false
        nextPage += 1
    }
}
