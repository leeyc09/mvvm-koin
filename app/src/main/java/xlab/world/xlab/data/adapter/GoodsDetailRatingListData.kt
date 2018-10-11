package xlab.world.xlab.data.adapter

import java.io.Serializable

data class GoodsDetailRatingListData(val petId: String,
                                     val petType: String,
                                     val petBreed: String,
                                     val petName: String,
                                     val topicColor: Int,
                                     var rating: Int): Serializable
