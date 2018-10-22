package xlab.world.xlab.server.`interface`

import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query
import xlab.world.xlab.data.response.ResExistNewData
import xlab.world.xlab.data.response.ResMessageData
import xlab.world.xlab.data.response.ResNoticeData
import xlab.world.xlab.server.ApiURL

interface INoticeRequest {
    @GET(ApiURL.NOTICE)
    fun getNotice(@Header("Authorization") authorization: String,
                  @Query("page") page: Int): Observable<ResNoticeData>

    @POST(ApiURL.NOTICE_READ)
    fun readNotice(@Header("Authorization") authorization: String,
                   @Query("noticeId") noticeId: String): Observable<ResMessageData>

    @GET(ApiURL.EXIST_NEW_NOTICE)
    fun getExistNewNotice(@Header("Authorization") authorization: String): Observable<ResExistNewData>
}
