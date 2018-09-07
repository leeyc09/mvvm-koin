package xlab.world.xlab.server.`interface`

import io.reactivex.Observable
import retrofit2.http.*
import xlab.world.xlab.data.request.ReqLoginData
import xlab.world.xlab.data.request.ReqRegisterData
import xlab.world.xlab.data.response.*
import xlab.world.xlab.server.ApiURL

/**
 * Created by kdu01 on 2018-01-26.
 */
interface IUserRequest {

    @POST(ApiURL.CHECK_VALID_TOKEN)
    fun checkValidToken(@Header("Authorization") authorization: String,
                        @Query("fcmToken") fcmToken: String): Observable<ResCheckValidTokenData>

    @GET(ApiURL.USER_REFRESH_TOKEN)
    fun getRefreshToken(@Header("Authorization") authorization: String): Observable<ResGetRefreshTokenData>

    @POST(ApiURL.GENERATE_TOKEN)
    fun generateToken(@Header("Authorization") authorization: String): Observable<ResGenerateTokenData>

    @POST(ApiURL.USER_LOGIN)
    fun login(@Body reqLoginData: ReqLoginData,
              @Query("type") type: Int): Observable<ResUserLoginData>

    @POST(ApiURL.USER_REGISTER)
    fun register(@Body reqRegisterData: ReqRegisterData): Observable<ResUserRegisterData>
}
