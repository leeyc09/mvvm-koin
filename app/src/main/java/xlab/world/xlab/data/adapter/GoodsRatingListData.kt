package xlab.world.xlab.data.adapter

import java.io.Serializable

data class GoodsRatingListData(val petId: String,
                               val petType: String,
                               val petBreed: String,
                               val petImage: String,
                               val petName: String,
                               var rating: Int): Serializable
