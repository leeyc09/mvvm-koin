package xlab.world.xlab.server.`interface`

import io.reactivex.Observable
import retrofit2.http.*
import xlab.world.xlab.data.response.ResUserPetsData
import xlab.world.xlab.data.response.ResUpdateTopicToggleData
import xlab.world.xlab.server.ApiURL

/**
 * Created by kdu01 on 2018-01-26.
 */
interface IPetRequest {

    @GET(ApiURL.PETS_LIST)
    fun getUserPetList(@Query("userId") userId: String,
                       @Query("page") page: Int): Observable<ResUserPetsData>

    @PUT(ApiURL.PETS_TOGGLE)
    fun updateTopicHidden(@Header("Authorization") authorization: String,
                          @Query("petId") petId: String): Observable<ResUpdateTopicToggleData>
}
