package xlab.world.xlab.view.follow

import android.arch.lifecycle.MutableLiveData
import xlab.world.xlab.data.adapter.UserDefaultData
import xlab.world.xlab.data.adapter.UserDefaultListData
import xlab.world.xlab.server.provider.ApiFollowProvider
import xlab.world.xlab.utils.rx.SchedulerProvider
import xlab.world.xlab.utils.support.NetworkCheck
import xlab.world.xlab.utils.support.PetInfo
import xlab.world.xlab.utils.support.PrintLog
import xlab.world.xlab.utils.support.TextConstants
import xlab.world.xlab.view.AbstractViewModel
import xlab.world.xlab.view.SingleLiveEvent

class FollowViewModel(private val apiFollow: ApiFollowProvider,
                      private val petInfo: PetInfo,
                      private val networkCheck: NetworkCheck,
                      private val scheduler: SchedulerProvider): AbstractViewModel() {
    val tag = "Follow"

    val loadFollowerEvent = SingleLiveEvent<FollowEvent>()
    val uiData = MutableLiveData<UIModel>()

    fun loadFollower(authorization: String, userId: String, page: Int, loadingBar: Boolean? = true) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = TextConstants.CHECK_NETWORK_CONNECT))
            return
        }

        uiData.value = UIModel(isLoading = loadingBar)
        loadFollowerEvent.value = FollowEvent(status = true)
        launch {
            apiFollow.requestGetFollower(scheduler = scheduler, authorization = authorization, userId = userId, page = page,
                    responseData = {
                        val followerData = UserDefaultData(total = it.total, nextPage = page + 1)
                        it.follower?.forEach { follower ->
                            // topic(pet) 종 이름 가져오기
                            val topicBreed = ArrayList<String>()
                            follower.petInfo?.forEach { pet ->
                                val breedStr = when (pet.type) {
                                    petInfo.dogCode -> petInfo.dogBreedInfo[pet.breed.toInt()].nameKor
                                    petInfo.catCode -> petInfo.catBreedInfo[pet.breed.toInt()].nameKor
                                    else -> ""
                                }
                                if (breedStr.isNotEmpty())
                                    topicBreed.add(breedStr)
                            }
                            followerData.items.add(UserDefaultListData(userId = follower.id,
                                    profileImage = follower.profileImg,
                                    nickName = follower.nickName,
                                    topic = topicBreed,
                                    isFollowing = follower.isFollowing))
                        }

                        PrintLog.d("requestGetFollower success", followerData.toString(), tag)
                        uiData.value = UIModel(isLoading = false, followerData = followerData)
                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(isLoading = false)
                        errorData?.let {
                            PrintLog.d("requestGetFollower fail", errorData.message, tag)
                        }
                    })
        }
    }
}

data class FollowEvent(val status: Boolean? = null)
data class UIModel(val isLoading: Boolean? = null, val toastMessage: String? = null,
                   val followerData: UserDefaultData? = null)
