package xlab.world.xlab.view.topicSetting

import android.app.Activity
import android.arch.lifecycle.MutableLiveData
import android.view.View
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
    private val viewModelTag = "TopicSetting"

    private var resultCode = Activity.RESULT_CANCELED

    private var topicSettingData: TopicSettingData = TopicSettingData()

    val loadPetListData = SingleLiveEvent<Boolean?>()
    val uiData = MutableLiveData<UIModel>()

    fun setResultCode(resultCode: Int) {
        when (resultCode) {
            Activity.RESULT_OK -> {
                if (this.resultCode == Activity.RESULT_CANCELED)
                    this.resultCode = resultCode
            }
        }
    }

    fun loadUserPetsData(userId: String, page: Int) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = networkCheck.networkErrorMsg))
            return
        }

        uiData.value = UIModel(isLoading = true)
        loadPetListData.value = true
        launch {
            apiPet.getUserPetList(scheduler = scheduler, userId = userId, page = page,
                    responseData = {
                        PrintLog.d("getUserPetList success", it.toString(), viewModelTag)
                        val newTopicSettingData = TopicSettingData(total = it.total, nextPage = page + 1)
                        it.petsData?.forEach { petData ->
                            newTopicSettingData.items.add(TopicSettingListData(
                                    isHidden = petData.isHidden,
                                    topicId = petData.id,
                                    topicImage = petData.image,
                                    title = petData.name,
                                    topicColor = petData.topicColor
                            ))
                        }
                        // 토픽이 있는 경우 토픽 헤더 보여주고,
                        // 없는 경우, 토픽 추가 버튼 활성화
                        if (page == 1) {
                            this.topicSettingData.updateData(topicSettingData = newTopicSettingData)
                            uiData.value = UIModel(isLoading = false, petData = this.topicSettingData,
                                    headerVisibility = if (this.topicSettingData.items.isEmpty()) View.GONE else View.VISIBLE,
                                    addTopicVisibility = if (this.topicSettingData.items.isEmpty()) View.VISIBLE else View.GONE)
                        } else {
                            this.topicSettingData.addData(topicSettingData = newTopicSettingData)
                            uiData.value = UIModel(isLoading = false, petDataUpdate = true)
                        }
                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(isLoading = false)
                        errorData?.let {
                            PrintLog.e("getUserPetList fail", errorData.message, viewModelTag)
                        }
                    })
        }
    }

    fun changeTopicToggle(authorization: String, selectIndex: Int) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = networkCheck.networkErrorMsg))
            return
        }

        uiData.value = UIModel(isLoading = true)
        launch {
            val topicData = topicSettingData.items[selectIndex]
            apiPet.updateTopicHidden(scheduler = scheduler, authorization = authorization, petId = topicData.topicId,
                    responseData = {
                        topicData.isHidden = it.isHidden
                        uiData.value = UIModel(isLoading = false, petUpdateIndex = selectIndex)
                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(isLoading = false)
                        errorData?.let {
                            PrintLog.e("updateTopicHidden fail", errorData.message, viewModelTag)
                        }
                    })
        }
    }

    fun backBtnAction() {
        uiData.postValue(UIModel(resultCode = resultCode))
    }
}

data class UIModel(val isLoading: Boolean? = null, val toastMessage: String? = null, val resultCode: Int? = null,
                   val headerVisibility: Int? = null, val addTopicVisibility: Int? = null,
                   val petData: TopicSettingData? = null, val petDataUpdate: Boolean? = null,
                   val petUpdateIndex: Int? = null)