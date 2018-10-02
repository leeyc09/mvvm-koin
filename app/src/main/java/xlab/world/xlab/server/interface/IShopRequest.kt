package xlab.world.xlab.server.`interface`

import io.reactivex.Observable
import retrofit2.http.*
import xlab.world.xlab.data.request.ReqGoodsSearchData
import xlab.world.xlab.data.response.ResDetailPostsData
import xlab.world.xlab.data.response.ResFeedData
import xlab.world.xlab.data.response.ResGoodsSearchData
import xlab.world.xlab.data.response.ResShopFeedData
import xlab.world.xlab.server.ApiURL

interface IShopRequest {

    @GET(ApiURL.SHOP_MAIN)
    fun getShopMain(@Header("Authorization") authorization: String): Observable<ResShopFeedData>

    @POST(ApiURL.SHOP_SEARCH)
    fun searchGoods(@Header("Authorization") authorization: String,
                    @Query("page") page: Int,
                    @Query("sortType") sortType: Int,
                    @Body reqGoodsSearchData: ArrayList<ReqGoodsSearchData>): Observable<ResGoodsSearchData>
}
