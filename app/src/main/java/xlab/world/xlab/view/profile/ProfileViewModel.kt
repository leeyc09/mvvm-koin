package xlab.world.xlab.view.profile

import android.arch.lifecycle.MutableLiveData
import android.content.Context
import io.reactivex.Observable
import xlab.world.xlab.R
import xlab.world.xlab.data.adapter.*
import xlab.world.xlab.server.provider.*
import xlab.world.xlab.utils.rx.SchedulerProvider
import xlab.world.xlab.utils.rx.with
import xlab.world.xlab.utils.support.AppConstants
import xlab.world.xlab.utils.support.NetworkCheck
import xlab.world.xlab.utils.support.PrintLog
import xlab.world.xlab.view.AbstractViewModel
import xlab.world.xlab.view.SingleLiveEvent

class ProfileViewModel(private val apiUser: ApiUserProvider,
                       private val apiPet: ApiPetProvider,
                       private val apiFollow: ApiFollowProvider,
                       private val apiPost: ApiPostProvider,
                       private val apiUserActivity: ApiUserActivityProvider,
                       private val networkCheck: NetworkCheck,
                       private val scheduler: SchedulerProvider): AbstractViewModel() {
    val tag = "Profile"

    val loadUserDataEvent = SingleLiveEvent<ProfileEvent>()
    val loadUserPetEvent = SingleLiveEvent<ProfileEvent>()
    val loadTopicUsedGoodsEvent = SingleLiveEvent<ProfileEvent>()
    val loadUserPostsThumbDataEvent = SingleLiveEvent<ProfileEvent>()
    val loadUserPostsDetailDataEvent = SingleLiveEvent<ProfileEvent>()
    val uiData = MutableLiveData<UIModel>()

    fun setProfileType(profileUserId: String, loginUserId: String, loadingBar: Boolean? = true) {
        uiData.value = UIModel(isLoading = loadingBar)
        launch {
            Observable.create<Int> {
                it.onNext(
                        if (profileUserId == loginUserId) AppConstants.MY_PROFILE
                        else AppConstants.OTHER_PROFILE)
                it.onComplete()
            }.with(scheduler).subscribe {
                PrintLog.d("setProfileType", it.toString())
                uiData.value = UIModel(isLoading = loadingBar?.let{_->false}, profileType = it)
            }
        }
    }

    fun loadUserTopicData(userId: String, page: Int, topicDataCount: Int, loginUserId: String, loadingBar: Boolean? = true) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = networkCheck.networkErrorMsg))
            return
        }

        uiData.value = UIModel(isLoading = loadingBar)
        loadUserPetEvent.value = ProfileEvent(status = true)
        launch {
            apiPet.getUserPetList(scheduler = scheduler, userId = userId, page = page,
                    responseData = {
                        val topicData = ProfileTopicData(total = it.total, nextPage = page + 1)
                        it.petsData?.forEach { petData ->
                            topicData.items.add(ProfileTopicListData(
                                    dataType = AppConstants.ADAPTER_CONTENT,
                                    topicId = petData.id,
                                    imageURL = petData.image))
                        }

                        val loadedTopicCount =
                                if (topicDataCount < 0) 0
                                else topicDataCount
                        val loadedTopicSize = loadedTopicCount + topicData.items.size

                        // 모든 topic 다 불러오고, 자신의 프로필 화면 -> 토픽 추가 버튼
                        if (loadedTopicSize >= topicData.total && userId == loginUserId)
                            topicData.items.add(ProfileTopicListData(dataType = AppConstants.ADAPTER_FOOTER))

                        PrintLog.d("getUserPetList success", topicData.toString())
                        uiData.value = UIModel(isLoading = loadingBar?.let{_->false}, topicData = topicData)
                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(isLoading = loadingBar?.let{_->false})
                        errorData?.let {
                            PrintLog.d("getUserPetList fail", errorData.message)
                        }
                    })
        }
    }

    fun loadUserData(context: Context, authorization: String, userId: String, loadingBar: Boolean? = true) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = networkCheck.networkErrorMsg))
            return
        }

        uiData.value = UIModel(isLoading = loadingBar)
        launch {
            apiUser.requestProfileMain(scheduler = scheduler, authorization = authorization, userId = userId,
                    responseData = {
                        PrintLog.d("requestProfileMain success", it.toString())
                        uiData.value = UIModel(isLoading = loadingBar?.let{_->false},
                                profileImage = it.profileImg,
                                nickName = it.nickName,
                                introduction = it.introduction,
                                followerCnt = it.followerCnt,
                                followingCnt = it.followingCnt,
                                followState = it.isFollowing)
                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(isLoading = loadingBar?.let{_->false}, toastMessage = context.getString(R.string.toast_no_exist_user))
                        loadUserDataEvent.postValue(ProfileEvent(status = false))
                        errorData?.let {
                            PrintLog.d("requestProfileMain fail", errorData.message)
                        }
                    })
        }
    }

    fun loadTopicUsedGoodsData(context: Context, userId: String, goodsType: Int, page: Int, loadingBar: Boolean? = true) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = networkCheck.networkErrorMsg))
            return
        }

        uiData.value = UIModel(isLoading = loadingBar)
        loadTopicUsedGoodsEvent.postValue(ProfileEvent(status = true))
        launch {
            apiUserActivity.requestTopicUsedGoods(scheduler = scheduler, userId = userId, goodsType = goodsType, page = page,
                    responseData = {
                        val topicUsedGoodsData = GoodsThumbnailData(total = it.total, nextPage = page + 1)
                        it.goods?.forEach { goods ->
                            topicUsedGoodsData.items.add(GoodsThumbnailListData(
                                    dataType = AppConstants.ADAPTER_CONTENT,
                                    goodsImage = goods.image,
                                    goodsCd = goods.code
                            ))
                        }

                        // 첫 페이지 & used goods 존재 -> 헤더 설정
                        if (topicUsedGoodsData.items.isNotEmpty() && page == 1) {
                            topicUsedGoodsData.items.add(0, GoodsThumbnailListData(
                                    dataType = AppConstants.ADAPTER_HEADER,
                                    headerTitle = context.getString(R.string.goods)))
                        }

                        PrintLog.d("requestTopicUsedGoods success", topicUsedGoodsData.toString())
                        uiData.value = UIModel(isLoading = loadingBar?.let{_->false}, topicUsedGoodsData = topicUsedGoodsData)
                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(isLoading = loadingBar?.let{_->false})
                        errorData?.let {
                            PrintLog.d("requestTopicUsedGoods fail", errorData.message)
                        }
                    })
        }
    }

    fun loadUserPostsThumbData(userId: String, page:Int, loadingBar: Boolean? = true) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = networkCheck.networkErrorMsg))
            return
        }

        uiData.value = UIModel(isLoading = loadingBar)
        loadUserPostsThumbDataEvent.postValue(ProfileEvent(status = true))
        launch {
            apiPost.requestUserPostsThumbnail(scheduler = scheduler, userId = userId, page = page,
                    responseData = {
                        val postsThumbData = PostThumbnailData(total = it.total, nextPage = page + 1)
                        it.postsData?.forEach { postData ->
                            postsThumbData.items.add(PostThumbnailListData(
                                    dataType = AppConstants.ADAPTER_CONTENT,
                                    postType = postData.postType,
                                    postId = postData.id,
                                    imageURL = postData.postFile.firstOrNull(),
                                    youTubeVideoID = postData.youTubeVideoID
                            ))
                        }

                        // posts 있고 첫 페이지 경우 -> header 추가
                        if (postsThumbData.items.isNotEmpty() && page == 1) {
                            postsThumbData.items.add(0, PostThumbnailListData(dataType = AppConstants.ADAPTER_HEADER))
                        }

                        PrintLog.d("requestUserPostsThumbnail success", postsThumbData.toString())
                        uiData.value = UIModel(isLoading = loadingBar?.let{_->false}, postsThumbData = postsThumbData)
                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(isLoading = loadingBar?.let{_->false})
                        errorData?.let {
                            PrintLog.d("requestUserPostsThumbnail fail", errorData.message)
                        }
                    })
        }
    }

    fun loadUserPostsDetailData(authorization: String, userId: String, page: Int, loginUserId: String, loadingBar: Boolean? = true) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = networkCheck.networkErrorMsg))
            return
        }

        uiData.value = UIModel(isLoading = loadingBar)
        loadUserPostsDetailDataEvent.postValue(ProfileEvent(status = true))
        launch {
            apiPost.requestUserPostsDetail(scheduler = scheduler, authorization = authorization, userId = userId, page = page,
                    responseData = {
                        val postsDetailData = PostDetailData(total = it.total, nextPage = page + 1)
                        it.postsData?.forEach { postsData ->
                            postsDetailData.items.add(PostDetailListData(
                                    dataType = AppConstants.ADAPTER_CONTENT,
                                    postType = postsData.postType,
                                    postId = postsData.id,
                                    userId = postsData.userId,
                                    userProfileURL = postsData.profileImg,
                                    userNickName = postsData.nickName,
                                    isFollowing = postsData.isFollowed,
                                    imageURL = postsData.postFile,
                                    youTubeVideoID = postsData.youTubeVideoID,
                                    likeNum = postsData.likedCount,
                                    commentsNum = postsData.commentCount,
                                    content = postsData.content,
                                    contentOrigin = postsData.content,
                                    isLike = postsData.isLiked,
                                    isSave = postsData.isSaved,
                                    uploadYear = postsData.uploadYear,
                                    uploadMonth = postsData.uploadMonth,
                                    uploadDay = postsData.uploadDay,
                                    uploadHour = postsData.uploadHour,
                                    uploadMinute = postsData.uploadMinute,
                                    goodsList = postsData.goods,
                                    isMyPost = userId == loginUserId
                            ))
                        }

                        // posts 있고 첫 페이지 경우 -> header 추가
                        if (postsDetailData.items.isNotEmpty() && page == 1) {
                            postsDetailData.items.add(0, PostDetailListData(dataType = AppConstants.ADAPTER_HEADER))
                        }

                        PrintLog.d("requestUserPostsDetail success", postsDetailData.toString())
                        uiData.value = UIModel(isLoading = loadingBar?.let{_->false}, postsDetailData = postsDetailData)
                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(isLoading = loadingBar?.let{_->false})
                        errorData?.let {
                            PrintLog.d("requestUserPostsDetail fail", errorData.message)
                        }
                    })
        }
    }

    fun userFollow(authorization: String, userId: String) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = networkCheck.networkErrorMsg))
            return
        }

        uiData.value = UIModel(isLoading = true)
        launch {
            apiFollow.requestFollow(scheduler = scheduler, authorization = authorization, userId = userId,
                    responseData = {
                        PrintLog.d("follow success", it.status.toString())
                        uiData.value = UIModel(isLoading = false, followState = it.status)
                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(isLoading = false)
                        errorData?.let {
                            PrintLog.d("follow fail", errorData.message)
                        }
                    })
        }
    }
}

data class ProfileEvent(val status: Boolean? = null)
data class UIModel(val isLoading: Boolean? = null, val toastMessage: String? = null,
                   val profileType: Int? = null, val topicData: ProfileTopicData? = null,
                   val profileImage: String? = null, val nickName: String? = null, val introduction: String? = null,
                   val followerCnt: Int? = null, val followingCnt: Int? = null, val followState: Boolean? = null,
                   val topicUsedGoodsData: GoodsThumbnailData? = null,
                   val postsThumbData: PostThumbnailData? = null, val postsDetailData: PostDetailData? = null)
