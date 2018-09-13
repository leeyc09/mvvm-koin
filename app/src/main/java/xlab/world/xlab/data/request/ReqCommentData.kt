package xlab.world.xlab.data.request

import com.google.gson.annotations.SerializedName
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.Serializable

data class ReqCommentData(private val builder: MultipartBody.Builder =
                                  MultipartBody.Builder().setType(MultipartBody.FORM)): Serializable {

    fun addContent(content: String) {
        builder.addFormDataPart("content", content)
    }

    fun getReqBody(): RequestBody = builder.build()
}