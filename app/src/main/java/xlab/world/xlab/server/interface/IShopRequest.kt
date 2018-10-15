package xlab.world.xlab.server.`interface`

import io.reactivex.Observable
import retrofit2.http.*
import xlab.world.xlab.data.request.ReqGoodsSearchData
import xlab.world.xlab.data.response.*
import xlab.world.xlab.server.ApiURL

interface IShopRequest {

    @GET(ApiURL.SHOP_GOODS_DETAIL)
    fun getGoodsDetail(@Query("goodsCd") goodsCode: String): Observable<ResGoodsDetailData>

    @GET(ApiURL.SHOP_MAIN)
    fun getShopMain(@Header("Authorization") authorization: String): Observable<ResShopFeedData>

    @POST(ApiURL.SHOP_SEARCH)
    fun searchGoods(@Header("Authorization") authorization: String,
                    @Query("page") page: Int,
                    @Query("sortType") sortType: Int,
                    @Body reqGoodsSearchData: ArrayList<ReqGoodsSearchData>): Observable<ResGoodsSearchData>

    @GET(ApiURL.SHOP_GOODS_STATS)
    fun getGoodsStats(@Header("Authorization") authorization: String,
                      @Query("goodsCd") goodsCode: String): Observable<ResGoodsStatsData>

    @GET(ApiURL.SHOP_TAGGED_POSTS)
    fun getGoodsTaggedPosts(@Query("goodsCd") goodsCode: String,
                            @Query("page") page: Int): Observable<ResThumbnailPostsData>

    @GET(ApiURL.SHOP_VIEW_GOODS)
    fun getRecnetViewGoods(@Header("Authorization") authorization: String,
                           @Query("page") page: Int): Observable<ResGoodsThumbnailData>
}
