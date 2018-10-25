package xlab.world.xlab.view.follow

import android.app.Activity
import android.arch.lifecycle.MutableLiveData
import android.view.View
import xlab.world.xlab.data.adapter.UserDefaultData
import xlab.world.xlab.data.adapter.UserDefaultListData
import xlab.world.xlab.data.adapter.UserRecommendData
import xlab.world.xlab.data.adapter.UserRecommendListData
import xlab.world.xlab.server.provider.ApiFollowProvider
import xlab.world.xlab.server.provider.ApiUserProvider
import xlab.world.xlab.utils.rx.SchedulerProvider
import xlab.world.xlab.utils.support.NetworkCheck
import xlab.world.xlab.utils.support.PetInfo
import xlab.world.xlab.utils.support.PrintLog
import xlab.world.xlab.utils.support.SupportData
import xlab.world.xlab.view.AbstractViewModel
import xlab.world.xlab.view.SingleLiveEvent

class FollowViewModel(private val apiFollow: ApiFollowProvider,
                      private val apiUser: ApiUserProvider,
                      private val petInfo: PetInfo,
                      private val networkCheck: NetworkCheck,
                      private val scheduler: SchedulerProvider): AbstractViewModel() {
    enum class UserType { DEFAULT, RECOMMEND }

    private val viewModelTag = "Follow"

    private var resultCode = Activity.RESULT_CANCELED
    private var followingCnt = 0

    private var userDefaultData: UserDefaultData = UserDefaultData()
    private var userRecommendData: UserRecommendData = UserRecommendData()

    val loadRecommendUserData = SingleLiveEvent<Boolean?>()
    val loadDefaultUserData = SingleLiveEvent<Boolean?>()
    val uiData = MutableLiveData<UIModel>()

    fun setResultCode(resultCode: Int) {
        this.resultCode = SupportData.setResultCode(oldResultCode = this.resultCode, newResultCode = resultCode)
    }

    fun userFollow(authorization: String, selectIndex: Int, userType: UserType) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = networkCheck.networkErrorMsg))
            return
        }

        uiData.value = UIModel(isLoading = true)
        launch {
            // 유저 타입에 따라서 data set
            val userData = when (userType) {
                UserType.DEFAULT -> this.userDefaultData.items[selectIndex]
                UserType.RECOMMEND -> this.userRecommendData.items[selectIndex]
            }
            val userId: String = when (userData) {
                is UserDefaultListData -> userData.userId
                is UserRecommendListData -> userData.userId
                else -> ""
            }

            apiFollow.requestFollow(scheduler = scheduler, authorization = authorization, userId = userId,
                    responseData = {
                        PrintLog.d("follow success", it.status.toString())
                        when (userData) {
                            is UserDefaultListData -> userData.isFollowing = it.status
                            is UserRecommendListData -> userData.isFollowing = it.status
                        }
                        uiData.value = UIModel(isLoading = false,
                                defaultUserUpdateIndex = when (userType) {
                                    UserType.DEFAULT -> selectIndex
                                    UserType.RECOMMEND -> null
                                },
                                recommendUserUpdateIndex = when (userType) {
                                    UserType.DEFAULT -> null
                                    UserType.RECOMMEND -> selectIndex
                                })
                        this.followingCnt += if (it.status) 1 else -1
                        uiData.value = UIModel(followingCnt = SupportData.countFormat(count = this.followingCnt))
                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(isLoading = false)
                        errorData?.let {
                            PrintLog.d("follow fail", errorData.message)
                        }
                    })
        }
    }

    fun loadFollower(authorization: String, userId: String, page: Int, loadingBar: Boolean? = true) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = networkCheck.networkErrorMsg))
            return
        }

        uiData.value = UIModel(isLoading = loadingBar)
        loadDefaultUserData.value = true
        launch {
            apiFollow.requestGetFollower(scheduler = scheduler, authorization = authorization, userId = userId, page = page,
                    responseData = {
                        PrintLog.d("requestGetFollower success", it.toString(), viewModelTag)

                        val newFollowerData = UserDefaultData(total = it.total, nextPage = page + 1)
                        it.userData?.forEach { follower ->
                            val topicBreed = petInfo.getTopicBreed(follower.petData)
                            newFollowerData.items.add(UserDefaultListData(userId = follower.id,
                                    profileImage = follower.profileImg,
                                    nickName = follower.nickName,
                                    topic = topicBreed,
                                    isFollowing = follower.isFollowing))
                        }

                        if (page == 1) {
                            this.userDefaultData.updateData(userDefaultData = newFollowerData)
                            uiData.value = UIModel(isLoading = false,
                                    defaultUserData = this.userDefaultData,
                                    emptyDefaultUserVisibility = if (this.userDefaultData.items.isEmpty()) View.VISIBLE else View.GONE,
                                    followerCnt = SupportData.countFormat(count = this.userDefaultData.total))
                        } else {
                            this.userDefaultData.addData(userDefaultData = newFollowerData)
                            uiData.value = UIModel(isLoading = false, defaultUserUpdate = true)
                        }
                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(isLoading = false)
                        errorData?.let {
                            PrintLog.e("requestGetFollower fail", errorData.message, viewModelTag)
                        }
                    })
        }
    }

    fun loadFollowing(authorization: String, userId: String, page: Int, loadingBar: Boolean? = true) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = networkCheck.networkErrorMsg))
            return
        }

        uiData.value = UIModel(isLoading = loadingBar)
        loadDefaultUserData.value = true
        launch {
            apiFollow.requestGetFollowing(scheduler = scheduler, authorization = authorization, userId = userId, page = page,
                    responseData = {
                        PrintLog.d("requestGetFollowing success", it.toString(), viewModelTag)
                        val newFollowingData = UserDefaultData(total = it.total, nextPage = page + 1)
                        it.userData?.forEach { following ->
                            val topicBreed = petInfo.getTopicBreed(following.petData)
                            newFollowingData.items.add(UserDefaultListData(userId = following.id,
                                    profileImage = following.profileImg,
                                    nickName = following.nickName,
                                    topic = topicBreed,
                                    isFollowing = following.isFollowing))
                        }

                        if (page == 1) {
                            this.followingCnt = newFollowingData.total
                            this.userDefaultData.updateData(userDefaultData = newFollowingData)
                            uiData.value = UIModel(isLoading = false,
                                    defaultUserData = this.userDefaultData,
                                    emptyDefaultUserVisibility = if (this.userDefaultData.items.isEmpty()) View.VISIBLE else View.GONE,
                                    followingCnt = SupportData.countFormat(count = this.followingCnt))
                        } else {
                            this.userDefaultData.addData(userDefaultData = newFollowingData)
                            uiData.value = UIModel(isLoading = false, defaultUserUpdate = true)
                        }
                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(isLoading = false)
                        errorData?.let {
                            PrintLog.e("requestGetFollowing fail", errorData.message, viewModelTag)
                        }
                    })
        }
    }

    fun loadRecommendUser(authorization: String, page: Int, loadingBar: Boolean? = true) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = networkCheck.networkErrorMsg))
            return
        }

        uiData.value = UIModel(isLoading = loadingBar)
        loadRecommendUserData.value = true
        launch {
            apiUser.requestRecommendUser(scheduler = scheduler, authorization = authorization, page = page,
                    responseData = {
                        PrintLog.d("requestRecommendUser success", it.toString(), viewModelTag)

                        val newRecommendUserData = UserRecommendData(total = it.total, nextPage = page + 1)
                        it.userData?.forEach { recommend ->
                            newRecommendUserData.items.add(UserRecommendListData(userId = recommend.id,
                                    profileImage = recommend.profileImg,
                                    nickName = recommend.nickName,
                                    isFollowing = recommend.isFollowing))
                        }

                        if (page == 1) {
                            this.userRecommendData.updateData(userRecommendData = newRecommendUserData)
                            uiData.value = UIModel(isLoading = false,
                                    recommendUserData = this.userRecommendData,
                                    recommendLayoutVisibility = if (this.userRecommendData.items.isNotEmpty()) View.VISIBLE else View.GONE)
                        } else {
                            this.userRecommendData.addData(userRecommendData = newRecommendUserData)
                            uiData.value = UIModel(isLoading = false, recommendUserUpdate = true)
                        }
                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(isLoading = false)
                        errorData?.let {
                            PrintLog.e("requestRecommendUser fail", errorData.message, viewModelTag)
                        }
                    })
        }
    }

    fun backBtnAction() {
        uiData.postValue(UIModel(resultCode = resultCode))
    }
}

data class UIModel(val isLoading: Boolean? = null, val toastMessage: String? = null, val resultCode: Int? = null,
                   val recommendLayoutVisibility: Int? = null,
                   val recommendUserData: UserRecommendData? = null, val recommendUserUpdate: Boolean? = null, val recommendUserUpdateIndex: Int? = null,
                   val emptyDefaultUserVisibility: Int? = null,
                   val defaultUserData: UserDefaultData? = null, val defaultUserUpdate: Boolean? = null, val defaultUserUpdateIndex: Int? = null,
                   val followerCnt: String? = null, val followingCnt: String? = null)
