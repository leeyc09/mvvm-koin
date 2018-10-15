package xlab.world.xlab.server.`interface`

import io.reactivex.Observable
import retrofit2.http.*
import xlab.world.xlab.data.request.ReqUsedGoodsData
import xlab.world.xlab.data.response.*
import xlab.world.xlab.server.ApiURL

interface IUserActivityRequest {

    @POST(ApiURL.ACTIVITY_POST_LIKE)
    fun likePost(@Header("Authorization") authorization: String,
                 @Query("postId") postId: String): Observable<ResLikeSavePostData>

    @GET(ApiURL.ACTIVITY_POST_LIKE)
    fun getLikedPosts(@Header("Authorization") authorization: String,
                      @Query("page") page: Int): Observable<ResThumbnailPostsData>

    @POST(ApiURL.ACTIVITY_POST_SAVE)
    fun savePost(@Header("Authorization") authorization: String,
                 @Query("postId") postId: String): Observable<ResLikeSavePostData>

    @GET(ApiURL.ACTIVITY_POST_SAVE)
    fun getSavedPosts(@Header("Authorization") authorization: String,
                      @Query("page") page: Int): Observable<ResThumbnailPostsData>

    @POST(ApiURL.ACTIVITY_USED_ITEM)
    fun postUsedGoods(@Header("Authorization") authorization: String,
                      @Body reqUsedItemData: ReqUsedGoodsData): Observable<ResMessageData>

    @DELETE(ApiURL.ACTIVITY_USED_ITEM)
    fun deleteUsedGoods(@Header("Authorization") authorization: String,
                        @Query("goodsCode") goodsCode: String,
                        @Query("topicId") topicId: String): Observable<ResMessageData>

    @GET(ApiURL.ACTIVITY_USED_ITEM)
    fun getUsedGoods(@Query("userId") userId: String,
                     @Query("goodsType") goodsType: Int,
                     @Query("page") page: Int): Observable<ResGoodsThumbnailData>
}
