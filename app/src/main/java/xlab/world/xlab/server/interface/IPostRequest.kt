package xlab.world.xlab.server.`interface`

import io.reactivex.Observable
import retrofit2.http.*
import xlab.world.xlab.data.response.ResDetailPostData
import xlab.world.xlab.data.response.ResDetailPostsData
import xlab.world.xlab.data.response.ResFeedData
import xlab.world.xlab.data.response.ResMessageData
import xlab.world.xlab.server.ApiURL

interface IPostRequest {

    @GET(ApiURL.POSTS_MAIN)
    fun getAllFeed(@Header("Authorization") authorization: String,
                   @Query("page") page: Int): Observable<ResFeedData>

    @GET(ApiURL.POSTS_FOLLOWING)
    fun getFollowingFeed(@Header("Authorization") authorization: String,
                         @Query("page") page: Int): Observable<ResDetailPostsData>

    @GET(ApiURL.POSTS_EXPLORE)
    fun getExploreFeed(@Header("Authorization") authorization: String,
                       @Query("page") page: Int): Observable<ResFeedData>

    @GET(ApiURL.POSTS)
    fun getPostDetail(@Header("Authorization") authorization: String,
                      @Query("postId") postId: String): Observable<ResDetailPostData>

    @DELETE(ApiURL.POSTS)
    fun deletePost(@Header("Authorization") authorization: String,
                   @Query("postId") postId: String): Observable<ResMessageData>
}
