package xlab.world.xlab.data.response

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ResUserPetsData(@SerializedName("message") val message: String,
                           @SerializedName("total") val total: Int,
                           @SerializedName("petsData") val petsData:  ArrayList<PetsData>?): Serializable {

    data class PetsData(@SerializedName("id") val id: String,
                        @SerializedName("topicColor") val topicColor: String,
                        @SerializedName("name") val name: String,
                        @SerializedName("type") val type: String,
                        @SerializedName("breed") val breed: String,
                        @SerializedName("profileImg") val image: String,
                        @SerializedName("isHidden") val isHidden: Boolean): Serializable

}
