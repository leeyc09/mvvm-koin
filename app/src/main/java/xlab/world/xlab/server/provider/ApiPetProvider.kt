package xlab.world.xlab.server.provider

import io.reactivex.disposables.Disposable
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