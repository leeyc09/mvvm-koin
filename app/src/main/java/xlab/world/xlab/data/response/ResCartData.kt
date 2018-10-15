package xlab.world.xlab.data.response

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ResCartData(@SerializedName("message") val message: String,
                       @SerializedName("total") val total: Int,
                       @SerializedName("cartItem") val cartData: ArrayList<Cart>?): Serializable {

    data class Cart(@SerializedName("sno") val sno: String,
                    @SerializedName("code") val code: String,
                    @SerializedName("count") val count: Int,
                    @SerializedName("name") val name: String,
                    @SerializedName("brandName") val brand: String,
                    @SerializedName("image") val image: String,
                    @SerializedName("price") val price: Int,
                    @SerializedName("deliverySno") val deliverySno: String): Serializable

}