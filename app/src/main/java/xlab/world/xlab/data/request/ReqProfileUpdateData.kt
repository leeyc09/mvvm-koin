package xlab.world.xlab.data.request

import com.google.gson.annotations.SerializedName
import okhttp3.MultipartBody
import okhttp3.RequestBody
import xlab.world.xlab.utils.support.PrintLog
import java.io.Serializable

data class ReqProfileUpdateData(private val builder: MultipartBody.Builder =
                                  MultipartBody.Builder().setType(MultipartBody.FORM)): Serializable {
    private val tag = "ReqProfileUpdateData"

    fun addProfileImage(fileName: String, requestBody: RequestBody) {
        builder.addFormDataPart("profileImage", fileName, requestBody)
        PrintLog.d("Profile Image File", fileName)
    }
    fun addNickName(nickName: String) {
        builder.addFormDataPart("nickName", nickName)
        PrintLog.d("Nick Name", nickName)
    }
    fun addIntroduction(introduction: String) {
        builder.addFormDataPart("introduction", introduction)
        PrintLog.d("Introduction", introduction)
    }
    fun addGender(gender: Int) {
        builder.addFormDataPart("gender", gender.toString())
        PrintLog.d("Gender", gender.toString())
    }
    fun addBirthYear(birthYear: String) {
        builder.addFormDataPart("birthYear", birthYear)
        PrintLog.d("BirthYear", birthYear)
    }

    fun getReqBody(): RequestBody = builder.build()
}