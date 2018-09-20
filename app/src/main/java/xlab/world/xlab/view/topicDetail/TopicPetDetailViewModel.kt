package xlab.world.xlab.view.topicDetail

import android.arch.lifecycle.MutableLiveData
import xlab.world.xlab.server.provider.ApiPetProvider
import xlab.world.xlab.utils.rx.SchedulerProvider
import xlab.world.xlab.utils.support.NetworkCheck
import xlab.world.xlab.utils.support.PetInfo
import xlab.world.xlab.utils.support.PrintLog
import xlab.world.xlab.utils.support.TextConstants
import xlab.world.xlab.view.AbstractViewModel

class TopicPetDetailViewModel(private val apiPet: ApiPetProvider,
                              private val petInfo: PetInfo,
                              private val networkCheck: NetworkCheck,
                              private val scheduler: SchedulerProvider): AbstractViewModel() {
    val tag = "TopicPetDetail"

    val uiData = MutableLiveData<UIModel>()

//    fun checkMy

    fun loadPetDetailData(userId: String, petNo: Int) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = TextConstants.CHECK_NETWORK_CONNECT))
            return
        }

        uiData.value = UIModel(isLoading = true)
        launch {
            apiPet.requestUserPet(scheduler = scheduler, userId = userId, petNo = petNo,
                    responseData = {
                        PrintLog.d("requestUserPet success", it.toString(), tag)
                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(isLoading = false)
                        errorData?.let {
                            PrintLog.d("requestUserPet fail", errorData.message, tag)
                        }
                    })
        }
    }
}

data class UIModel(val isLoading: Boolean? = null, val toastMessage: String? = null)