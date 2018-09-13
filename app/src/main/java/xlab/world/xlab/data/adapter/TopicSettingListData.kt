package xlab.world.xlab.data.adapter

import java.io.Serializable

data class TopicSettingListData(var isHidden: Boolean,
                                val topicId: String,
                                val topicImage: String,
                                val title: String ,
                                val topicColor: String): Serializable
