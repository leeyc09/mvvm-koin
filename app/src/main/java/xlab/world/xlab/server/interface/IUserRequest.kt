package xlab.world.xlab.server.`interface`

import io.reactivex.Observable
import io.reactivex.Single
import retrofit2.http.*
import xlab.world.xlab.data.request.ReqLoginData
import xlab.world.xlab.data.response.*
import xlab.world.xlab.server.ApiURL.USER_LOGIN

/**
 * Created by kdu01 on 2018-01-26.
 */
interface IUserRequest {
    @POST(USER_LOGIN)
    fun login(@Body reqLoginData: ReqLoginData,
              @Query("type") type: Int): Observable<ResLoginData>
}
