package xlab.world.xlab.server.`interface`

import io.reactivex.Observable
import retrofit2.http.*
import xlab.world.xlab.data.response.ResFollowData
import xlab.world.xlab.data.response.ResUserDefaultData
import xlab.world.xlab.server.ApiURL

interface IFollowRequest {

    @POST(ApiURL.FOLLOW)
    fun follow(@Header("Authorization") authorization: String,
               @Query("userId") userId: String): Observable<ResFollowData>

    @GET(ApiURL.FOLLOWER_LIST)
    fun getFollowerList(@Header("Authorization") authorization: String,
                        @Query("userId") userId: String,
                        @Query("page") page: Int): Observable<ResUserDefaultData>

    @GET(ApiURL.FOLLOWING_LIST)
    fun getFollowingList(@Header("Authorization") authorization: String,
                         @Query("userId") userId: String,
                         @Query("page") page: Int): Observable<ResUserDefaultData>
}
