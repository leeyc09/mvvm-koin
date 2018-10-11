package xlab.world.xlab.data.response

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ResGoodsDetailPetData(@SerializedName("message") val message: String,
                                 @SerializedName("userPetsData") val petsData: ArrayList<PetsData>?): Serializable {

    data class PetsData(@SerializedName("id") val id: String,
                        @SerializedName("topicColor") val topicColor: String,
                        @SerializedName("name") val name: String,
                        @SerializedName("profileImg") val image: String,
                        @SerializedName("type") val type: String,
                        @SerializedName("breed") val breed: String,
                        @SerializedName("match") val match: Int,
                        @SerializedName("rating") val rating: Int): Serializable

}
