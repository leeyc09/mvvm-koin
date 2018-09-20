package xlab.world.xlab.data.request

import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.Serializable

data class ReqPetData(private val builder: MultipartBody.Builder =
                              MultipartBody.Builder().setType(MultipartBody.FORM)): Serializable {

    fun addTopicColor(topicColor: String) {
        builder.addFormDataPart("topicColor", topicColor)
    }
    fun addPetImage(fileName: String, requestBody: RequestBody) {
        builder.addFormDataPart("profile", fileName, requestBody)
    }
    fun addPetType(type: String) {
        builder.addFormDataPart("type", type)
    }
    fun addPetName(name: String) {
        builder.addFormDataPart("name", name)
    }
    fun addPetGender(gender: String) {
        builder.addFormDataPart("gender", gender)
    }
    fun addPetNeutered(isNeutered: Boolean) {
        builder.addFormDataPart("isNeutered", isNeutered.toString())
    }
    fun addPetBreed(breed: String) {
        builder.addFormDataPart("breed", breed)
    }
    fun addPetSize(size: String) {
        builder.addFormDataPart("size", size)
    }
    fun addPetHairType(hairType: String) {
        builder.addFormDataPart("hairType", hairType)
    }
    fun addPetHairColor(hairColor: String) {
        builder.addFormDataPart("hairColor", hairColor)
    }
    fun addPetBirthYear(birthYear: Int) {
        builder.addFormDataPart("birthYear", birthYear.toString())
    }
    fun addPetBirthMonth(birthMonth: Int) {
        builder.addFormDataPart("birthMonth", birthMonth.toString())
    }
    fun addPetBirthDay(birthDay: Int) {
        builder.addFormDataPart("birthDay", birthDay.toString())
    }
    fun addPetWeight(weight: Float) {
        builder.addFormDataPart("weight", weight.toString())
    }

    fun getReqBody(): RequestBody = builder.build()
}
