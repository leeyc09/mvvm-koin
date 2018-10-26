package xlab.world.xlab.server.`interface`

import io.reactivex.Observable
import okhttp3.RequestBody
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

    @GET(ApiURL.SHOP_MY_CART)
    fun getMyCart(@Header("Authorization") authorization: String): Observable<ResCartData>

    @GET(ApiURL.SHOP_MY_CART_COUNT)
    fun getMyCartCnt(@Header("Authorization") authorization: String): Observable<ResCartCntData>

    @POST(ApiURL.SHOP_MY_CART_ADD)
    fun addMyCart(@Header("Authorization") authorization: String,
                  @Query("goodsNo") goodsNo: String,
                  @Query("count") count: Int): Observable<ResAddCartData>

    @POST(ApiURL.SHOP_MY_CART_UPDATE)
    fun updateMyCart(@Header("Authorization") authorization: String,
                     @Query("sno") sno: String,
                     @Query("count") count: Int): Observable<ResMessageData>

    @POST(ApiURL.SHOP_MY_CART_DELETE)
    fun deleteMyCart(@Header("Authorization") authorization: String,
                     @Query("sno") sno: String): Observable<ResMessageData>

    @GET(ApiURL.SHOP_ORDER_STATUS_NUM)
    fun getOrderStateCnt(@Header("Authorization") authorization: String): Observable<ResOrderStateCntData>


    @GET(ApiURL.SHOP_ORDER_STATUS_HISTORY)
    fun getOrderStateHistory(@Header("Authorization") authorization: String,
                             @Query("status") state: Int): Observable<ResOrderHistoryData>

    @GET(ApiURL.SHOP_ORDER)
    fun getOrderDetail(@Header("Authorization") authorization: String,
                       @Query("orderNo") orderNo: String): Observable<ResOrderDetailData>

    @GET(ApiURL.SHOP_ORDER_HISTORY)
    fun getOrderHistory(@Header("Authorization") authorization: String): Observable<ResOrderHistoryData>

    @POST(ApiURL.SHOP_ORDER_REFUND_RETURN_CHANGE)
    fun orderCRR(@Header("Authorization") authorization: String,
                 @Body requestBody: RequestBody): Observable<ResMessageData>

    @GET(ApiURL.SHOP_ORDER_CRR_DETAIL)
    fun getCRRDetail(@Header("Authorization") authorization: String,
                     @Query("sno") handleSno: String): Observable<ResCRRDetailData>

    @POST(ApiURL.SHOP_ORDER_CANCEL)
    fun orderCancel(@Header("Authorization") authorization: String,
                    @Query("orderNo") orderNo: String): Observable<ResMessageData>

    @POST(ApiURL.SHOP_ORDER_RECEIVE_CONFIRM)
    fun orderReceiveConfirm(@Header("Authorization") authorization: String,
                            @Query("orderNo") orderNo: String,
                            @Query("sno") sno: String): Observable<ResMessageData>

    @POST(ApiURL.SHOP_ORDER_DECIDE)
    fun buyDecide(@Header("Authorization") authorization: String,
                  @Query("orderNo") orderNo: String,
                  @Query("sno") sno: String): Observable<ResMessageData>
}
