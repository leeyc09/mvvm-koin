package xlab.world.xlab.data.adapter

import java.io.Serializable

data class GoodsDetailStatsListData(val title: String,
                                    val topicImages: ArrayList<String>?,
                                    val goodPercent: Int,
                                    val sosoPercent: Int,
                                    val badPercent: Int): Serializable
