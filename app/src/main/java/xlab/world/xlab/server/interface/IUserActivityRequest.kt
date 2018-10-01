package xlab.world.xlab.server.`interface`

import io.reactivex.Observable
import retrofit2.http.*
import xlab.world.xlab.data.response.ResLikeSavePostData
import xlab.world.xlab.data.response.ResThumbnailPostsData
import xlab.world.xlab.data.response.ResUsedGoodsData
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

    @GET(ApiURL.ACTIVITY_USED_ITEM)
    fun getUsedGoods(@Query("userId") userId: String,
                     @Query("goodsType") goodsType: Int,
                     @Query("page") page: Int): Observable<ResUsedGoodsData>
}
