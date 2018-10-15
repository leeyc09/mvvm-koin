package xlab.world.xlab.data.response

import com.google.gson.annotations.SerializedName
import xlab.world.xlab.utils.support.AppConstants
import java.io.Serializable

data class ResGoodsThumbnailData(@SerializedName("message") val message: String,
                                 @SerializedName("total") val total: Int,
                                 @SerializedName(value = "goods", alternate = ["usedItems"]) val goods: ArrayList<UsedGoods>?): Serializable {

    data class UsedGoods(@SerializedName(value = "code", alternate = ["goodsCode"]) val code: String,
                         @SerializedName(value = "image", alternate = ["goodsImage"]) val image: String,
                         @SerializedName("goodsName") val name: String,
                         @SerializedName("goodsBrand") val brand: String): Serializable
}
