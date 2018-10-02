package xlab.world.xlab.data.response

import com.google.gson.annotations.SerializedName
import xlab.world.xlab.utils.support.AppConstants
import java.io.Serializable

data class ResGoodsSearchData(@SerializedName("message") val message: String,
                              @SerializedName("total") val goodsTotal: Int,
                              @SerializedName("codeTotal") val keywordTotal: Int,
                              @SerializedName("codeList") val keywordList: ArrayList<Keyword>?,
                              @SerializedName("shopItems") val goodsList: ArrayList<Goods>?): Serializable {

    data class Keyword(@SerializedName("keyword") val keyword: String,
                       @SerializedName("code") val code: String,
                       @SerializedName("num") val num: Int): Serializable

    data class Goods(@SerializedName("code") val code: String,
                     @SerializedName("name") val name: String,
                     @SerializedName("brandName") val brandName: String,
                     @SerializedName("price") val price: Int,
                     @SerializedName("image") val image: String,
                     @SerializedName("topicMatch") val topicMatch: ArrayList<TopicMatch>?): Serializable

    data class TopicMatch(@SerializedName("topicId") val topicId: String,
                          @SerializedName("topicColor") val topicColor: String,
                          @SerializedName("match") val match: Int): Serializable
}
