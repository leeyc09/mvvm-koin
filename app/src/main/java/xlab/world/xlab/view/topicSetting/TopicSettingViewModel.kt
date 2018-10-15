package xlab.world.xlab.view.topicSetting

import android.arch.lifecycle.MutableLiveData
import xlab.world.xlab.data.adapter.TopicSettingData
import xlab.world.xlab.data.adapter.TopicSettingListData
import xlab.world.xlab.server.provider.ApiPetProvider
import xlab.world.xlab.utils.rx.SchedulerProvider
import xlab.world.xlab.utils.support.NetworkCheck
import xlab.world.xlab.utils.support.PrintLog
import xlab.world.xlab.view.AbstractViewModel
import xlab.world.xlab.view.SingleLiveEvent

class TopicSettingViewModel(private val apiPet: ApiPetProvider,
                            private val networkCheck: NetworkCheck,
                            private val scheduler: SchedulerProvider): AbstractViewModel() {
    val tag = "TopicSetting"

    val loadUserPetsListDataEvent = SingleLiveEvent<LoadUserPetsListDataEvent>()
    val uiData = MutableLiveData<UIModel>()

    fun loadUserPetsData(userId: String, page: Int) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = networkCheck.networkErrorMsg))
            return
        }

        uiData.value = UIModel(isLoading = true)
        loadUserPetsListDataEvent.value = LoadUserPetsListDataEvent(isLoading = true)
        launch {
            apiPet.getUserPetList(scheduler = scheduler, userId = userId, page = page,
                    responseData = {
                        val topicSettingData = TopicSettingData(total = it.total, nextPage = page + 1)
                        it.petsData?.forEach { petData ->
                            topicSettingData.items.add(TopicSettingListData(
                                    isHidden = petData.isHidden,
                                    topicId = petData.id,
                                    topicImage = petData.image,
                                    title = petData.name,
                                    topicColor = petData.topicColor
                            ))
                        }

                        PrintLog.d("getUserPetList success", topicSettingData.toString(), tag)
                        uiData.value = UIModel(isLoading = false, petData = topicSettingData)
                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(isLoading = false)
                        errorData?.let {
                            PrintLog.d("getUserPetList fail", errorData.message, tag)
                        }
                    })
        }
    }

    fun changeTopicToggle(authorization: String, position: Int, topicData: TopicSettingListData) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = networkCheck.networkErrorMsg))
            return
        }

        uiData.value = UIModel(isLoading = true)
        launch {
            apiPet.updateTopicHidden(scheduler = scheduler, authorization = authorization, petId = topicData.topicId,
                    responseData = {
                        topicData.isHidden = it.isHidden
                        uiData.value = UIModel(isLoading = false, topicPosition = position)
                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(isLoading = false)
                        errorData?.let {
                            PrintLog.d("updateTopicHidden fail", errorData.message, tag)
                        }
                    })
        }
    }
}

data class LoadUserPetsListDataEvent(val isLoading: Boolean? = null)
data class UIModel(val isLoading: Boolean? = null, val toastMessage: String? = null, val petData: TopicSettingData? = null,
                   val topicPosition: Int? = null)