package xlab.world.xlab.server.`interface`

import io.reactivex.Observable
import okhttp3.RequestBody
import retrofit2.http.*
import xlab.world.xlab.data.request.*
import xlab.world.xlab.data.response.*
import xlab.world.xlab.server.ApiURL

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

    @POST(ApiURL.USER_LOGOUT)
    fun logout(@Header("Authorization") authorization: String): Observable<ResMessageData>

    @POST(ApiURL.USER_REGISTER)
    fun register(@Body reqRegisterData: ReqRegisterData): Observable<ResUserRegisterData>

    @POST(ApiURL.USER_WITHDRAW)
    fun withdraw(@Header("Authorization") authorization: String,
                 @Query("content") content: String): Observable<ResMessageData>

    @POST(ApiURL.USER_AUTH_EMAIL)
    fun confirmEmail(@Body reqConfirmEmailData: ReqConfirmEmailData): Observable<ResMessageData>

    @POST(ApiURL.USER_AUTH_EMAIL_CODE)
    fun confirmEmailCode(@Body reqConfirmEmailData: ReqConfirmEmailData): Observable<ResConfirmEmailCodeData>

    @POST(ApiURL.USER_CHECK_PASSWORD)
    fun checkPassword(@Header("Authorization") authorization: String,
                      @Body reqPasswordData: ReqPasswordData): Observable<ResMessageData>

    @POST(ApiURL.USER_CHANGE_PASSWORD)
    fun changePassword(@Header("Authorization") authorization: String,
                       @Body reqPasswordData: ReqPasswordData): Observable<ResMessageData>

    @GET(ApiURL.USER_PROFILE_MAIN)
    fun profileMain(@Header("Authorization") authorization: String,
                    @Query("userId") userId: String): Observable<ResProfileMainData>

    @GET(ApiURL.USER_PROFILE_EDIT)
    fun profileEdit(@Header("Authorization") authorization: String): Observable<ResProfileEditData>

    @POST(ApiURL.USER_PROFILE_UPDATE)
    fun profileUpdate(@Header("Authorization") authorization: String,
                      @Query("userId") userId: String,
                      @Body requestBody: RequestBody): Observable<ResMessageData>

    @GET(ApiURL.USER_RECOMMEND)
    fun getRecommendUser(@Header("Authorization") authorization: String,
                         @Query("page") page: Int): Observable<ResUserDefaultData>

    @GET(ApiURL.USER_SETTING)
    fun getSetting(@Header("Authorization") authorization: String): Observable<ResSettingData>

    @POST(ApiURL.USER_SETTING_PUSH)
    fun pushSetting(@Header("Authorization") authorization: String): Observable<ResSettingPushData>

    @GET(ApiURL.USER_SEARCH)
    fun userSearch(@Header("Authorization") authorization: String,
                   @Query("query") searchText: String,
                   @Query("page") page: Int): Observable<ResUserDefaultData>

    @GET(ApiURL.USER_USED_ITEM)
    fun getGoodsUsedUser(@Query("goodsCode") goodsCode: String,
                         @Query("page") page: Int): Observable<ResGoodsUsedUserData>

    @POST(ApiURL.USER_REPORT)
    fun userReport(@Header("Authorization") authorization: String,
                   @Body reqUserReportData: ReqUserReportData): Observable<ResMessageData>
}
