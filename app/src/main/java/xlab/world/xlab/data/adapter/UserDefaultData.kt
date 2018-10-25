package xlab.world.xlab.data.adapter

import java.io.Serializable

data class UserDefaultData(var total:Int = -1,
                           var nextPage: Int = 1,
                           var isLoading: Boolean = true,
                           val items: ArrayList<UserDefaultListData> = ArrayList()): Serializable {

    fun updateData(userDefaultData: UserDefaultData) {
        items.clear()
        userDefaultData.items.forEach {
            items.add(it.copy())
        }

        isLoading = false
        total = userDefaultData.total
        nextPage = 2
    }

    fun addData(userDefaultData: UserDefaultData) {
        userDefaultData.items.forEach {
            items.add(it.copy())
        }

        isLoading = false
        nextPage += 1
    }
}
