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
        PrintLog.d("Profile Image File", fileName, tag)
    }
    fun addNickName(nickName: String) {
        builder.addFormDataPart("nickName", nickName)
        PrintLog.d("Nick Name", nickName, tag)
    }
    fun addIntroduction(introduction: String) {
        builder.addFormDataPart("introduction", introduction)
        PrintLog.d("Introduction", introduction, tag)
    }
    fun addGender(gender: String) {
        builder.addFormDataPart("gender", gender)
        PrintLog.d("Gender", gender, tag)
    }
    fun addBirthYear(birthYear: String) {
        builder.addFormDataPart("birthYear", birthYear)
        PrintLog.d("BirthYear", birthYear, tag)
    }

    fun getReqBody(): RequestBody = builder.build()
}