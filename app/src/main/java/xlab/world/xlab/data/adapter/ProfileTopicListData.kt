package xlab.world.xlab.data.adapter

import java.io.Serializable

data class ProfileTopicListData(val dataType: Int,
                                val topicId: String = "",
                                val imageURL: String = ""): Serializable
