package xlab.world.xlab.data.response

import com.google.gson.annotations.SerializedName
import xlab.world.xlab.utils.support.AppConstants
import java.io.Serializable

data class ResShopFeedData(@SerializedName("message") val message: String,
                           @SerializedName("livingItems") val livingGoods: ArrayList<ResGoodsSearchData.Goods>?,
                           @SerializedName("fashionItems") val fashionGoods: ArrayList<ResGoodsSearchData.Goods>?,
                           @SerializedName("foodItems") val foodGoods: ArrayList<ResGoodsSearchData.Goods>?): Serializable