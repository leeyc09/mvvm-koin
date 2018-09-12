package xlab.world.xlab.data.response

import com.google.gson.annotations.SerializedName
import xlab.world.xlab.utils.support.AppConstants
import java.io.Serializable

data class ResShopFeedData(@SerializedName("message") val message: String,
                           @SerializedName("livingItems") val livingGoods: ArrayList<Goods>?,
                           @SerializedName("fashionItems") val fashionGoods: ArrayList<Goods>?,
                           @SerializedName("foodItems") val foodGoods: ArrayList<Goods>?): Serializable {

    data class Goods(@SerializedName("code") val code: String,
                     @SerializedName("name") val name: String,
                     @SerializedName("brandName") val brandName: String,
                     @SerializedName("price") val price: Int,
                     @SerializedName("image") val image: String,
                     @SerializedName("topicMatch") val topicMatch: ArrayList<ResFeedData.TopicMatch>?): Serializable
}
