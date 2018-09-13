package xlab.world.xlab.data.response

import com.google.gson.annotations.SerializedName
import xlab.world.xlab.utils.support.AppConstants
import java.io.Serializable

data class ResUsedGoodsData(@SerializedName("message") val message: String,
                            @SerializedName("total") val total: Int,
                            @SerializedName("usedItems") val usedGoods: ArrayList<UsedGoods>?): Serializable {

    data class UsedGoods(@SerializedName("goodsCode") val code: String,
                         @SerializedName("goodsName") val name: String,
                         @SerializedName("goodsBrand") val brand: String,
                         @SerializedName("goodsImage") val image: String): Serializable
}
