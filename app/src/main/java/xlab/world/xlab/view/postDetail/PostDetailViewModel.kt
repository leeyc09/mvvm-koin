package xlab.world.xlab.view.postDetail

import android.arch.lifecycle.MutableLiveData
import xlab.world.xlab.data.adapter.PostDetailData
import xlab.world.xlab.data.adapter.PostDetailListData
import xlab.world.xlab.server.provider.ApiPostProvider
import xlab.world.xlab.utils.rx.SchedulerProvider
import xlab.world.xlab.utils.support.AppConstants
import xlab.world.xlab.utils.support.NetworkCheck
import xlab.world.xlab.utils.support.PrintLog
import xlab.world.xlab.utils.support.TextConstants
import xlab.world.xlab.view.AbstractViewModel
import xlab.world.xlab.view.SingleLiveEvent

class PostDetailViewModel(private val apiPost: ApiPostProvider,
                          private val networkCheck: NetworkCheck,
                          private val scheduler: SchedulerProvider): AbstractViewModel() {
    val tag = "PostDetail"

    val postDeleteEvent = SingleLiveEvent<PostDeleteEvent>()
    val loadPostDetailEvent = SingleLiveEvent<LoadPostDetailEvent>()
    val uiData = MutableLiveData<UIModel>()

    fun loadPostDetail(authorization: String, postId: String, userId: String) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = TextConstants.CHECK_NETWORK_CONNECT))
            return
        }

        uiData.value = UIModel(isLoading = true)
        launch {
            apiPost.getPostDetail(scheduler = scheduler, authorization = authorization, postId = postId,
                    responseData = {
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

                        PrintLog.d("getPostDetail success", "", tag)
                        uiData.value = UIModel(isLoading = false, postDetailData = postDetailData)
                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(isLoading = false, toastMessage = TextConstants.NO_EXIST_POST)
                        loadPostDetailEvent.postValue(LoadPostDetailEvent(isFail = true))
                        errorData?.let {
                            PrintLog.d("getPostDetail fail", errorData.message, tag)
                        }
                    })
        }
    }

    fun deletePost(authorization: String, postId: String) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = TextConstants.CHECK_NETWORK_CONNECT))
            return
        }

        uiData.value = UIModel(isLoading = true)
        launch {
            apiPost.postDelete(scheduler = scheduler, authorization = authorization, postId = postId,
                    responseData = {
                        PrintLog.d("postDelete success", "", tag)
                        uiData.value = UIModel(isLoading = false)
                        postDeleteEvent.postValue(PostDeleteEvent(isSuccess = true))
                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(isLoading = false)
                        errorData?.let {
                            PrintLog.d("postDelete fail", errorData.message, tag)
                        }
                    })
        }
    }
}

data class PostDeleteEvent(val isSuccess: Boolean? = null)
data class LoadPostDetailEvent(val isFail: Boolean? = null)
data class UIModel(val isLoading: Boolean? = null, val toastMessage: String? = null,
                   val postDetailData: PostDetailData? = null)