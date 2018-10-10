package xlab.world.xlab.data.request

import com.google.gson.annotations.SerializedName
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import xlab.world.xlab.data.adapter.SelectUsedGoodsListData
import java.io.File
import java.io.Serializable

data class ReqPostUploadData(private val builder: MultipartBody.Builder =
                                  MultipartBody.Builder().setType(MultipartBody.FORM)): Serializable {

    fun addPostType(postType: Int) {
        builder.addFormDataPart("postType", postType.toString())
    }
    fun addPostFiles(imagePaths: ArrayList<String>) {
        imagePaths.forEach { imagePath ->
            val imageFile = File(imagePath)
            val requestBody = RequestBody.create(MediaType.parse("multipart/form-dat"), imageFile)
            builder.addFormDataPart("postFiles", imageFile.name, requestBody)
        }
    }
    fun addYouTubeVideoId(youTubeVideoId: String) {
        builder.addFormDataPart("youTubeVideoID", youTubeVideoId)
    }
    fun addContent(content: String) {
        builder.addFormDataPart("content", content)
    }
    fun addTag(hashTags: ArrayList<String>) {
        hashTags.forEach { tag ->
            builder.addFormDataPart("tags", tag)
        }
    }
    fun addGoods(goodsData: ArrayList<SelectUsedGoodsListData>) {
        goodsData.forEach { goods ->
            builder.addFormDataPart("goodsCode", goods.goodsCode)
            builder.addFormDataPart("goodsImage", goods.imageURL)
            builder.addFormDataPart("goodsName", goods.goodsName)
            builder.addFormDataPart("goodsBrand", goods.goodsBrand)
        }
    }

    fun getReqBody(): RequestBody = builder.build()
}