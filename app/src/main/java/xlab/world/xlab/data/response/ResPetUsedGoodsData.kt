package xlab.world.xlab.data.response

import com.google.gson.annotations.SerializedName
import xlab.world.xlab.utils.support.AppConstants
import java.io.Serializable

data class ResPetUsedGoodsData(@SerializedName("message") val message: String,
                               @SerializedName("total") val total: Int,
                               @SerializedName("usedItems") val goodsData: ArrayList<UsedGoods>?): Serializable {

    data class UsedGoods(@SerializedName("goodsCode") val code: String,
                         @SerializedName("goodsImage") val image: String,
                         @SerializedName("rating") val rating: Int): Serializable
}
