package xlab.world.xlab.data.adapter

import java.io.Serializable

data class GoodsDetailTopicMatchListData(val petId: String,
                                         val petName: String,
                                         val imageURL: String,
                                         val matchingPercent: Int,
                                         val matchColor: Int): Serializable
