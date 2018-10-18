package xlab.world.xlab.data.adapter

import android.text.SpannableString
import java.io.Serializable

data class GoodsDetailStatsListData(val title: SpannableString,
                                    val topicImages: ArrayList<String>?,
                                    val goodPercent: Int,
                                    val sosoPercent: Int,
                                    val badPercent: Int): Serializable
