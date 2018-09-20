package xlab.world.xlab.server.provider

import io.reactivex.disposables.Disposable
import okhttp3.RequestBody
import xlab.world.xlab.data.response.ResUserPetsData
import xlab.world.xlab.data.response.*
import xlab.world.xlab.server.`interface`.IPetRequest
import xlab.world.xlab.server.errorHandle
import xlab.world.xlab.utils.rx.SchedulerProvider
import xlab.world.xlab.utils.rx.with

interface ApiPetProvider {
    // pet data 가져오기
    fun requestUserPet(scheduler: SchedulerProvider, userId: String, petNo: Int,
                       responseData: (ResUserPetData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable

    // pet 추가하기
    fun requestAddPet(scheduler: SchedulerProvider, authorization: String, requestBody: RequestBody,
                      responseData: (ResMessageData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable

    // pet 수정하기
    fun requestUpdatePet(scheduler: SchedulerProvider, authorization: String, petId: String, requestBody: RequestBody,
                         responseData: (ResMessageData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable

    // pet 삭제하기
    fun requestDeletePet(scheduler: SchedulerProvider, authorization: String, petId: String,
                         responseData: (ResMessageData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable

    // pet list 가져오기
    fun getUserPetList(scheduler: SchedulerProvider, userId: String, page: Int,
                       responseData: (ResUserPetsData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable

    // topic hidden setting 변경
    fun updateTopicHidden(scheduler: SchedulerProvider, authorization: String, petId: String,
                          responseData: (ResUpdateTopicToggleData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable
}

class ApiPet(private val iPetRequest: IPetRequest): ApiPetProvider {
    override fun requestUserPet(scheduler: SchedulerProvider, userId: String, petNo: Int,
                                responseData: (ResUserPetData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable {
        return iPetRequest.getUserPet(userId = userId, petNo = petNo)
                .with(scheduler)
                .subscribe({
                    responseData(it)
                }, {
                    errorData(errorHandle<ResMessageErrorData>(it))
                })
    }

    override fun requestAddPet(scheduler: SchedulerProvider, authorization: String, requestBody: RequestBody,
                               responseData: (ResMessageData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable {
        return iPetRequest.addPet(authorization = authorization, requestBody = requestBody)
                .with(scheduler)
                .subscribe({
                    responseData(it)
                }, {
                    errorData(errorHandle<ResMessageErrorData>(it))
                })
    }

    override fun requestUpdatePet(scheduler: SchedulerProvider, authorization: String, petId: String, requestBody: RequestBody,
                                  responseData: (ResMessageData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable {
        return iPetRequest.updatePet(authorization = authorization, petId = petId, requestBody = requestBody)
        .with(scheduler)
                .subscribe({
                    responseData(it)
                }, {
                    errorData(errorHandle<ResMessageErrorData>(it))
                })
    }

    override fun requestDeletePet(scheduler: SchedulerProvider, authorization: String, petId: String,
                                  responseData: (ResMessageData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable {
        return iPetRequest.deletePet(authorization = authorization, petId = petId)
                .with(scheduler)
                .subscribe({
                    responseData(it)
                }, {
                    errorData(errorHandle<ResMessageErrorData>(it))
                })
    }

    override fun getUserPetList(scheduler: SchedulerProvider, userId: String, page: Int,
                                responseData: (ResUserPetsData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable {
        return iPetRequest.getUserPetList(userId = userId, page = page)
                .with(scheduler)
                .subscribe({
                    responseData(it)
                }, {
                    errorData(errorHandle<ResMessageErrorData>(it))
                })
    }

    override fun updateTopicHidden(scheduler: SchedulerProvider, authorization: String, petId: String,
                                   responseData: (ResUpdateTopicToggleData) -> Unit, errorData: (ResMessageErrorData?) -> Unit): Disposable {
        return iPetRequest.updateTopicHidden(authorization = authorization, petId = petId)
                .with(scheduler)
                .subscribe({
                    responseData(it)
                }, {
                    errorData(errorHandle<ResMessageErrorData>(it))
                })
    }
}