package xlab.world.xlab.view.topicSetting

import android.arch.lifecycle.MutableLiveData
import xlab.world.xlab.data.adapter.TopicSettingData
import xlab.world.xlab.data.adapter.TopicSettingListData
import xlab.world.xlab.server.provider.ApiPetProvider
import xlab.world.xlab.utils.rx.SchedulerProvider
import xlab.world.xlab.utils.support.MessageConstants
import xlab.world.xlab.utils.support.NetworkCheck
import xlab.world.xlab.utils.support.PrintLog
import xlab.world.xlab.view.AbstractViewModel

class TopicSettingViewModel(private val apiPet: ApiPetProvider,
                            private val networkCheck: NetworkCheck,
                            private val scheduler: SchedulerProvider): AbstractViewModel() {
    val tag = "TopicSetting"

    val uiData = MutableLiveData<UIModel>()

    fun loadUserPetList(userId: String, page: Int) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = MessageConstants.CHECK_NETWORK_CONNECT))
            return
        }

        uiData.value = UIModel(isLoading = true)
        launch {
            apiPet.getUserPetList(scheduler = scheduler, userId = userId, page = page,
                    responseData = {
                        PrintLog.d("getUserPetList success", "", tag)
                        val topicSettingData = TopicSettingData(nextPage = page + 1)
                        it.petsData?.forEach { petData ->
                            topicSettingData.items.add(TopicSettingListData(
                                    isHidden = petData.isHidden,
                                    topicId = petData.id,
                                    topicImage = petData.image,
                                    title = petData.name,
                                    topicColor = petData.topicColor
                            ))
                        }

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

    fun changeTopicToggle(authorization: String, petId: String, position: Int) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = MessageConstants.CHECK_NETWORK_CONNECT))
            return
        }

        uiData.value = UIModel(isLoading = true)
        launch {
            apiPet.updateTopicHidden(scheduler = scheduler, authorization = authorization, petId = petId,
                    responseData = {
                        uiData.value = UIModel(isLoading = false, topicToggleData = TopicToggleData(position = position, result = it.isHidden))
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

data class TopicToggleData(val position: Int, val result: Boolean)
data class UIModel(val isLoading: Boolean? = null, val toastMessage: String? = null, val petData: TopicSettingData? = null, val topicToggleData: TopicToggleData? = null)