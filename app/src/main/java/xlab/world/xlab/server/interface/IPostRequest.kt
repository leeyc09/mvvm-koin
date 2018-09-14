package xlab.world.xlab.server.`interface`

import io.reactivex.Observable
import retrofit2.http.*
import xlab.world.xlab.data.response.*
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

    @GET(ApiURL.POSTS_CHECK_MINE)
    fun checkPostsMine(@Header("Authorization") authorization: String,
                       @Query("postId") postId: String): Observable<ResCheckMyPostData>

    @GET(ApiURL.POSTS_ALBUM)
    fun getUserPostsThumb(@Query("userId") userId: String,
                          @Query("page") page: Int): Observable<ResThumbnailPostsData>

    @GET(ApiURL.POSTS_ALBUM_DETAIL)
    fun getUserPostsDetail(@Header("Authorization") authorization: String,
                           @Query("userId") userId: String,
                           @Query("page") page: Int): Observable<ResDetailPostsData>
}
