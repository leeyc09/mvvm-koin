package xlab.world.xlab.data.request

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ReqRecentViewGoodsData(@SerializedName("goodsCode") val code: String,
                                  @SerializedName("goodsImage") val image: String): Serializable