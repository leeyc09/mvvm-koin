package xlab.world.xlab.data.request

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ReqUsedGoodsData(@SerializedName("usedType") val usedType: Int,
                            @SerializedName("goodsCode") val goodsCode:String,
                            @SerializedName("goodsName") val goodsName:String,
                            @SerializedName("goodsBrand") val goodsBrand:String,
                            @SerializedName("goodsImage") val goodsImage: String,
                            @SerializedName("goodsType") val goodsType: Int,
                            @SerializedName("topic") val topic: Topic): Serializable {

    data class Topic(@SerializedName("id") val id:String,
                     @SerializedName("type") val type:String,
                     @SerializedName("breed") val breed:String,
                     @SerializedName("rating") val rating: Int): Serializable
}
