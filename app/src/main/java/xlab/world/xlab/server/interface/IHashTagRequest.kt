package xlab.world.xlab.server.`interface`

import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query
import xlab.world.xlab.data.response.ResCountHashTagData
import xlab.world.xlab.data.response.ResRecentHashTagData
import xlab.world.xlab.data.response.ResMessageData
import xlab.world.xlab.server.ApiURL

interface IHashTagRequest {
    @GET(ApiURL.HASH_TAG_RECENT)
    fun recentHashTag(@Header("Authorization") authorization: String): Observable<ResRecentHashTagData>

    @GET(ApiURL.HASH_TAG_COUNT)
    fun searchHashTagCount(@Header("Authorization") authorization: String,
                           @Query("query") query: String): Observable<ResCountHashTagData>

    @POST(ApiURL.USER_HASH_TAG_UPDATE)
    fun updateUserHashTag(@Header("Authorization") authorization: String,
                          @Query("hashTag") hashTag: String): Observable<ResMessageData>
}
