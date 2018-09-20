package xlab.world.xlab.data.response

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ResUserPetData(@SerializedName("message") val message: String = "",
                          @SerializedName("id") val id: String = "",
                          @SerializedName("topicColor") var topicColor: String = "",
                          @SerializedName("profileImg") val profileImage: String = "",
                          @SerializedName("type") var type: String = "",
                          @SerializedName("name") var name: String = "",
                          @SerializedName("gender") var gender: String = "",
                          @SerializedName("isNeutered") var isNeutered: Boolean = false,
                          @SerializedName("breed") var breed: String = "",
                          @SerializedName("size") var size: ArrayList<String>? = null,
                          @SerializedName("hairType") var hairType: String = "",
                          @SerializedName("hairColor") var hairColor :ArrayList<String>? = null,
                          @SerializedName("birthYear") var birthYear: Int = -1,
                          @SerializedName("birthMonth") var birthMonth: Int = -1,
                          @SerializedName("birthDay") var birthDay: Int = -1,
                          @SerializedName("ageYear") var ageYear: Int = -1,
                          @SerializedName("ageMonth") var ageMonth: Int = -1,
                          @SerializedName("weight") var weight: Float = -1f): Serializable {
    fun valueCopy(): ResUserPetData {
        val newData = this.copy()
        size?.let {
            val newSize = ArrayList<String>()
            newSize.addAll(it)
            newData.size = newSize
        }
        hairColor?.let {
            val newHairColor = ArrayList<String>()
            newHairColor.addAll(it)
            newData.hairColor = newHairColor
        }
        return newData
    }

    fun isFillData(): Boolean {
        return topicColor.isNotEmpty() &&
                type.isNotEmpty() &&
                name.isNotEmpty() &&
                gender.isNotEmpty() &&
                breed.isNotEmpty() &&
                size!!.isNotEmpty() &&
                hairType.isNotEmpty() &&
                hairColor!!.isNotEmpty() &&
                birthYear != -1 &&
                birthMonth != -1 &&
                birthDay != -1 &&
                weight != -1f
    }
}