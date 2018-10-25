package xlab.world.xlab.data.adapter

import java.io.Serializable

data class UserRecommendData(var total:Int = -1,
                             var nextPage: Int = 1,
                             var isLoading: Boolean = true,
                             val items: ArrayList<UserRecommendListData> = ArrayList()): Serializable {

    fun updateData(userRecommendData: UserRecommendData) {
        items.clear()
        userRecommendData.items.forEach {
            items.add(it.copy())
        }

        isLoading = false
        total = userRecommendData.total
        nextPage = 2
    }

    fun addData(userRecommendData: UserRecommendData) {
        userRecommendData.items.forEach {
            items.add(it.copy())
        }

        isLoading = false
        nextPage += 1
    }
}
