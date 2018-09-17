package xlab.world.xlab.view.follow

import android.arch.lifecycle.MutableLiveData
import xlab.world.xlab.data.adapter.UserDefaultData
import xlab.world.xlab.data.adapter.UserDefaultListData
import xlab.world.xlab.data.adapter.UserRecommendData
import xlab.world.xlab.data.adapter.UserRecommendListData
import xlab.world.xlab.data.response.ResUserDefaultData
import xlab.world.xlab.server.provider.ApiFollowProvider
import xlab.world.xlab.server.provider.ApiUserProvider
import xlab.world.xlab.utils.rx.SchedulerProvider
import xlab.world.xlab.utils.support.NetworkCheck
import xlab.world.xlab.utils.support.PetInfo
import xlab.world.xlab.utils.support.PrintLog
import xlab.world.xlab.utils.support.TextConstants
import xlab.world.xlab.view.AbstractViewModel
import xlab.world.xlab.view.SingleLiveEvent

class FollowViewModel(private val apiFollow: ApiFollowProvider,
                      private val apiUser: ApiUserProvider,
                      private val petInfo: PetInfo,
                      private val networkCheck: NetworkCheck,
                      private val scheduler: SchedulerProvider): AbstractViewModel() {
    val tag = "Follow"

    val loadRecommendUserEvent = SingleLiveEvent<FollowEvent>()
    val loadFollowEvent = SingleLiveEvent<FollowEvent>()
    val uiData = MutableLiveData<UIModel>()

    fun userFollow(authorization: String, position: Int,
                   userData: UserDefaultListData?, recommendUserData: UserRecommendListData?, followCnt: Int? = null) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = TextConstants.CHECK_NETWORK_CONNECT))
            return
        }

        uiData.value = UIModel(isLoading = true)
        launch {
            val userId = userData?.userId ?: recommendUserData?.userId
            apiFollow.requestFollow(scheduler = scheduler, authorization = authorization, userId = userId!!,
                    responseData = {
                        PrintLog.d("follow success", it.status.toString(), tag)
                        userData?.isFollowing = it.status
                        recommendUserData?.isFollowing = it.status
                        uiData.value = UIModel(isLoading = false, userUpdatePosition = position)
                        followCnt?.let { followCnt ->
                            uiData.value = UIModel(followCnt =
                            if (it.status) followCnt + 1
                            else followCnt - 1)
                        }
                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(isLoading = false)
                        errorData?.let {
                            PrintLog.d("follow fail", errorData.message, tag)
                        }
                    })
        }
    }

    fun loadFollower(authorization: String, userId: String, page: Int, loadingBar: Boolean? = true) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = TextConstants.CHECK_NETWORK_CONNECT))
            return
        }

        uiData.value = UIModel(isLoading = loadingBar)
        loadFollowEvent.value = FollowEvent(status = true)
        launch {
            apiFollow.requestGetFollower(scheduler = scheduler, authorization = authorization, userId = userId, page = page,
                    responseData = {
                        val followerData = UserDefaultData(total = it.total, nextPage = page + 1)
                        it.follower?.forEach { follower ->
                            val topicBreed = getTopicBreed(follower.petData)
                            followerData.items.add(UserDefaultListData(userId = follower.id,
                                    profileImage = follower.profileImg,
                                    nickName = follower.nickName,
                                    topic = topicBreed,
                                    isFollowing = follower.isFollowing))
                        }

                        PrintLog.d("requestGetFollower success", followerData.toString(), tag)
                        uiData.value = UIModel(isLoading = false, userData = followerData, followCnt = it.total)
                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(isLoading = false)
                        errorData?.let {
                            PrintLog.d("requestGetFollower fail", errorData.message, tag)
                        }
                    })
        }
    }

    fun loadFollowing(authorization: String, userId: String, page: Int, loadingBar: Boolean? = true) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = TextConstants.CHECK_NETWORK_CONNECT))
            return
        }

        uiData.value = UIModel(isLoading = loadingBar)
        loadFollowEvent.value = FollowEvent(status = true)
        launch {
            apiFollow.requestGetFollowing(scheduler = scheduler, authorization = authorization, userId = userId, page = page,
                    responseData = {
                        val followingData = UserDefaultData(total = it.total, nextPage = page + 1)
                        it.following?.forEach { following ->
                            val topicBreed = getTopicBreed(following.petData)
                            followingData.items.add(UserDefaultListData(userId = following.id,
                                    profileImage = following.profileImg,
                                    nickName = following.nickName,
                                    topic = topicBreed,
                                    isFollowing = following.isFollowing))
                        }

                        PrintLog.d("requestGetFollowing success", followingData.toString(), tag)
                        uiData.value = UIModel(isLoading = false, userData = followingData, followCnt = it.total)
                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(isLoading = false)
                        errorData?.let {
                            PrintLog.d("requestGetFollowing fail", errorData.message, tag)
                        }
                    })
        }
    }

    fun loadRecommendUser(authorization: String, page: Int, loadingBar: Boolean? = true) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = TextConstants.CHECK_NETWORK_CONNECT))
            return
        }

        uiData.value = UIModel(isLoading = loadingBar)
        loadRecommendUserEvent.value = FollowEvent(status = true)
        launch {
            apiUser.requestRecommendUser(scheduler = scheduler, authorization = authorization, page = page,
                    responseData = {
                        val recommendUserData = UserRecommendData(total = it.total, nextPage = page + 1)
                        it.recommend?.forEach { recommend ->
                            recommendUserData.items.add(UserRecommendListData(userId = recommend.id,
                                    profileImage = recommend.profileImg,
                                    nickName = recommend.nickName,
                                    isFollowing = recommend.isFollowing))
                        }

                        PrintLog.d("requestRecommendUser success", recommendUserData.toString(), tag)
                        uiData.value = UIModel(isLoading = false, recommendUserData = recommendUserData)
                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(isLoading = false)
                        errorData?.let {
                            PrintLog.d("requestRecommendUser fail", errorData.message, tag)
                        }
                    })
        }
    }

    private fun getTopicBreed(petData: ArrayList<ResUserDefaultData.FollowUserPetInfo>?): ArrayList<String>{
        // topic(pet) 종 이름 가져오기
        val topicBreed = ArrayList<String>()
        petData?.forEach { pet ->
            val breedStr = when (pet.type) {
                petInfo.dogCode -> petInfo.dogBreedInfo[pet.breed.toInt()].nameKor
                petInfo.catCode -> petInfo.catBreedInfo[pet.breed.toInt()].nameKor
                else -> ""
            }
            if (breedStr.isNotEmpty())
                topicBreed.add(breedStr)
        }
        return topicBreed
    }
}

data class FollowEvent(val status: Boolean? = null)
data class UIModel(val isLoading: Boolean? = null, val toastMessage: String? = null,
                   val userUpdatePosition: Int? = null, val recommendUserData: UserRecommendData? = null,
                   val userData: UserDefaultData? = null, val followCnt: Int? = null)
