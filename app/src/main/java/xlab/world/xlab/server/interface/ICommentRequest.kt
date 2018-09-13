package xlab.world.xlab.server.`interface`

import io.reactivex.Observable
import okhttp3.RequestBody
import retrofit2.http.*
import xlab.world.xlab.data.response.ResCommentData
import xlab.world.xlab.data.response.ResFollowData
import xlab.world.xlab.data.response.ResLikeSavePostData
import xlab.world.xlab.data.response.ResMessageData
import xlab.world.xlab.server.ApiURL

interface ICommentRequest {
    @GET(ApiURL.COMMENT)
    fun getComments(@Query("postId") postId: String,
                    @Query("page") page: Int): Observable<ResCommentData>

    @POST(ApiURL.COMMENT)
    fun postComment(@Header("Authorization") authorization: String,
                    @Query("postId") postId: String,
                    @Body requestBody: RequestBody): Observable<ResMessageData>

    @DELETE(ApiURL.COMMENT)
    fun deleteComment(@Header("Authorization") authorization: String,
                       @Query("postId") postId: String,
                       @Query("index") index: Int): Observable<ResMessageData>
}
