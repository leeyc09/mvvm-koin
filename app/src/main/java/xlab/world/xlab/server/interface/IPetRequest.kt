package xlab.world.xlab.server.`interface`

import io.reactivex.Observable
import okhttp3.RequestBody
import retrofit2.http.*
import xlab.world.xlab.data.response.*
import xlab.world.xlab.server.ApiURL

interface IPetRequest {

    @GET(ApiURL.PETS)
    fun getUserPet(@Query("userId") userId: String,
                   @Query("page") petNo: String): Observable<ResUserPetData>

    @POST(ApiURL.PETS)
    fun addPet(@Header("Authorization") authorization: String,
               @Body requestBody: RequestBody): Observable<ResMessageData>

    @PUT(ApiURL.PETS)
    fun updatePet(@Header("Authorization") authorization: String,
                  @Query("petId") petId: String,
                  @Body requestBody: RequestBody): Observable<ResMessageData>

    @DELETE(ApiURL.PETS)
    fun deletePet(@Header("Authorization") authorization: String,
                  @Query("petId") petId: String): Observable<ResMessageData>

    @GET(ApiURL.PETS_LIST)
    fun getUserPetList(@Query("userId") userId: String,
                       @Query("page") page: Int): Observable<ResUserPetsData>

    @PUT(ApiURL.PETS_TOGGLE)
    fun updateTopicHidden(@Header("Authorization") authorization: String,
                          @Query("petId") petId: String): Observable<ResUpdateTopicToggleData>

    @GET(ApiURL.PET_USED_ITEMS)
    fun getPetUsedGoods(@Query("page") page: Int,
                        @Query("petId") petId: String): Observable<ResPetUsedGoodsData>

    @GET(ApiURL.PET_GOODS_DETAIL)
    fun getGoodsDetailPet(@Header("Authorization") authorization: String,
                          @Query("goodsCode") goodsCode: String): Observable<ResGoodsDetailPetData>
}
