package xlab.world.xlab.data.adapter

import java.io.Serializable

data class TopicSettingData(var total:Int = -1,
                            var nextPage: Int = 1,
                            val items: ArrayList<TopicSettingListData> = ArrayList()): Serializable
