package xlab.world.xlab.server.`interface`

import io.reactivex.Observable
import retrofit2.http.*
import xlab.world.xlab.data.response.*
import xlab.world.xlab.server.ApiURL

interface IGodoRequest {

    @GET(ApiURL.SHOP_PROFILE)
    fun getShopProfile(@Header("Authorization") authorization: String): Observable<ResShopProfileData>

    @POST(ApiURL.SHOP_PROFILE)
    fun updateShopProfile(@Header("Authorization") authorization: String,
                          @Query("name") name: String,
                          @Query("email") email: String): Observable<ResMessageData>

    @POST(ApiURL.SHOP_MY_CART_ADD)
    fun addMyCart(@Header("Authorization") authorization: String,
                  @Query("goodsNo") goodsNo: String,
                  @Query("count") count: Int): Observable<ResAddCartData>


    @GET(ApiURL.SHOP_ORDER_STATUS_NUM)
    fun getOrderStatusNum(@Header("Authorization") authorization: String): Observable<ResOrderStatusCntData>
}
