package xlab.world.xlab.server.`interface`

import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query
import xlab.world.xlab.data.response.ResExistNewData
import xlab.world.xlab.data.response.ResSocialNotificationData
import xlab.world.xlab.server.ApiURL

interface INotificationRequest {
    @GET(ApiURL.SOCIAL_NOTIFICATION)
    fun getSocialNotification(@Header("Authorization") authorization: String,
                              @Query("page") page: Int): Observable<ResSocialNotificationData>

    @GET(ApiURL.EXIST_NEW_NOTIFICATION)
    fun getExistNewNotification(@Header("Authorization") authorization: String): Observable<ResExistNewData>
}
