package xlab.world.xlab.view.postDetail

import android.arch.lifecycle.MutableLiveData
import android.content.Context
import xlab.world.xlab.R
import xlab.world.xlab.data.adapter.PostDetailData
import xlab.world.xlab.data.adapter.PostDetailListData
import xlab.world.xlab.server.provider.ApiFollowProvider
import xlab.world.xlab.server.provider.ApiPostProvider
import xlab.world.xlab.server.provider.ApiUserActivityProvider
import xlab.world.xlab.utils.rx.SchedulerProvider
import xlab.world.xlab.utils.support.AppConstants
import xlab.world.xlab.utils.support.NetworkCheck
import xlab.world.xlab.utils.support.PrintLog
import xlab.world.xlab.view.AbstractViewModel
import xlab.world.xlab.view.SingleLiveEvent

class PostDetailViewModel(private val apiPost: ApiPostProvider,
                          private val apiUserActivity: ApiUserActivityProvider,
                          private val apiFollow: ApiFollowProvider,
                          private val networkCheck: NetworkCheck,
                          private val scheduler: SchedulerProvider): AbstractViewModel() {
    val tag = "PostDetail"

    val postDeleteEvent = SingleLiveEvent<PostDeleteEvent>()
    val loadPostDetailEvent = SingleLiveEvent<LoadPostDetailEvent>()
    val uiData = MutableLiveData<UIModel>()

    fun loadPostDetail(context: Context, authorization: String, postId: String, userId: String) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = networkCheck.networkErrorMsg))
            return
        }

        uiData.value = UIModel(isLoading = true)
        launch {
            apiPost.getPostDetail(scheduler = scheduler, authorization = authorization, postId = postId,
                    responseData = {
                        PrintLog.d("getPostDetail success", it.toString())

                        val postDetailData = PostDetailData()
                        postDetailData.items.add(PostDetailListData(
                                dataType = AppConstants.ADAPTER_CONTENT,
                                postType = it.postsData.postType,
                                postId = it.postsData.id,
                                userId = it.postsData.userId,
                                userProfileURL = it.postsData.profileImg,
                                userNickName = it.postsData.nickName,
                                isFollowing = it.postsData.isFollowed,
                                imageURL = it.postsData.postFile,
                                youTubeVideoID = it.postsData.youTubeVideoID,
                                likeNum = it.postsData.likedCount,
                                commentsNum = it.postsData.commentCount,
                                content = it.postsData.content,
                                contentOrigin = it.postsData.content,
                                isLike = it.postsData.isLiked,
                                isSave = it.postsData.isSaved,
                                uploadYear = it.postsData.uploadYear,
                                uploadMonth = it.postsData.uploadMonth,
                                uploadDay = it.postsData.uploadDay,
                                uploadHour = it.postsData.uploadHour,
                                uploadMinute = it.postsData.uploadMinute,
                                goodsList = it.postsData.goods,
                                isMyPost = it.postsData.userId == userId
                        ))

                        uiData.value = UIModel(isLoading = false, postDetailData = postDetailData)
                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(isLoading = false, toastMessage = context.getString(R.string.toast_no_exist_post))
                        loadPostDetailEvent.postValue(LoadPostDetailEvent(isFail = true))
                        errorData?.let {
                            PrintLog.d("getPostDetail fail", errorData.message)
                        }
                    })
        }
    }

    fun deletePost(authorization: String, postId: String) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = networkCheck.networkErrorMsg))
            return
        }

        uiData.value = UIModel(isLoading = true)
        launch {
            apiPost.postDelete(scheduler = scheduler, authorization = authorization, postId = postId,
                    responseData = {
                        PrintLog.d("postDelete success", "")
                        uiData.value = UIModel(isLoading = false)
                        postDeleteEvent.postValue(PostDeleteEvent(isSuccess = true))
                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(isLoading = false)
                        errorData?.let {
                            PrintLog.d("postDelete fail", errorData.message)
                        }
                    })
        }
    }

    fun likePost(authorization: String, position: Int, postData: PostDetailListData) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = networkCheck.networkErrorMsg))
            return
        }

        uiData.value = UIModel(isLoading = true)
        launch {
            apiUserActivity.postLike(scheduler = scheduler, authorization = authorization, postId = postData.postId,
                    responseData = {
                        PrintLog.d("postLike success", it.status.toString())
                        postData.isLike = it.status
                        postData.likeNum +=
                                if (postData.isLike) 1
                                else -1
                        uiData.value = UIModel(isLoading = false, postUpdatePosition = position)
                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(isLoading = false)
                        errorData?.let {
                            PrintLog.d("postLike fail", errorData.message)
                        }
                    })
        }
    }

    fun savePost(context: Context, authorization: String, position: Int, postData: PostDetailListData) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = networkCheck.networkErrorMsg))
            return
        }

        uiData.value = UIModel(isLoading = true)
        launch {
            apiUserActivity.postSave(scheduler = scheduler, authorization = authorization, postId = postData.postId,
                    responseData = {
                        PrintLog.d("postSave success", it.status.toString())
                        postData.isSave = it.status
                        val toastMessage =
                                if (postData.isSave) context.getString(R.string.toast_saved_success)
                                else null
                        uiData.value = UIModel(isLoading = false, postUpdatePosition = position, toastMessage = toastMessage)
                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(isLoading = false)
                        errorData?.let {
                            PrintLog.d("postSave fail", errorData.message)
                        }
                    })
        }
    }

    fun userFollow(authorization: String, position: Int, postData: PostDetailListData) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = networkCheck.networkErrorMsg))
            return
        }

        uiData.value = UIModel(isLoading = true)
        launch {
            apiFollow.requestFollow(scheduler = scheduler, authorization = authorization, userId = postData.userId,
                    responseData = {
                        PrintLog.d("follow success", it.status.toString())
                        postData.isFollowing = it.status
                        uiData.value = UIModel(isLoading = false, postUpdatePosition = position)
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

data class PostDeleteEvent(val isSuccess: Boolean? = null)
data class LoadPostDetailEvent(val isFail: Boolean? = null)
data class UIModel(val isLoading: Boolean? = null, val toastMessage: String? = null,
                   val postDetailData: PostDetailData? = null, val postUpdatePosition: Int? = null)