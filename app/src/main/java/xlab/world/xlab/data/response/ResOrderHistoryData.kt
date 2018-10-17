package xlab.world.xlab.data.response

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ResOrderHistoryData(@SerializedName("message") val message: String,
                               @SerializedName("total") val total: Int,
                               @SerializedName("orderHistory") val goodsList: ArrayList<ResOrderDetailData.Goods>?): Serializable