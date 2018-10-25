package xlab.world.xlab.view.postDetail

import android.app.Activity
import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.view.View
import xlab.world.xlab.R
import xlab.world.xlab.data.adapter.PostDetailData
import xlab.world.xlab.data.adapter.PostDetailListData
import xlab.world.xlab.server.provider.ApiFollowProvider
import xlab.world.xlab.server.provider.ApiPostProvider
import xlab.world.xlab.server.provider.ApiUserActivityProvider
import xlab.world.xlab.utils.rx.SchedulerProvider
import xlab.world.xlab.utils.support.*
import xlab.world.xlab.view.AbstractViewModel
import xlab.world.xlab.view.SingleLiveEvent
import java.net.HttpURLConnection

class PostDetailViewModel(private val apiPost: ApiPostProvider,
                          private val apiUserActivity: ApiUserActivityProvider,
                          private val apiFollow: ApiFollowProvider,
                          private val networkCheck: NetworkCheck,
                          private val scheduler: SchedulerProvider): AbstractViewModel() {
    private val viewModelTag = "PostDetail"

    private var resultCode = Activity.RESULT_CANCELED

    private var postDetailData: PostDetailData = PostDetailData()

    val postDeleteData = SingleLiveEvent<Boolean?>()
    val failLoadPostDetailData = SingleLiveEvent<Boolean?>()
    val loadPostDetailData = SingleLiveEvent<Boolean?>()
    val uiData = MutableLiveData<UIModel>()

    fun setResultCode(resultCode: Int) {
        this.resultCode = SupportData.setResultCode(oldResultCode = this.resultCode, newResultCode = resultCode)
    }

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
                        PrintLog.d("getPostDetail success", it.toString(), viewModelTag)

                        val newPostDetailData = PostDetailData()
                        newPostDetailData.items.add(PostDetailListData(
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

                        this.postDetailData.updateData(postDetailData = newPostDetailData)

                        uiData.value = UIModel(isLoading = false, postDetailData = this.postDetailData)
                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(isLoading = false, toastMessage = context.getString(R.string.toast_no_exist_post))
                        this.resultCode = Activity.RESULT_OK
                        failLoadPostDetailData.value = true
                        errorData?.let {
                            PrintLog.e("getPostDetail fail", errorData.message, viewModelTag)
                        }
                    })
        }
    }

    fun loadFollowingFeedData(authorization: String, page: Int, loadingBar: Boolean? = true) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = networkCheck.networkErrorMsg))
            return
        }

        // login 확인
        if (SupportData.isGuest(authorization)) { // guest
            uiData.value = UIModel(noLoginVisibility = View.VISIBLE)
            return
        }

        uiData.value = UIModel(isLoading = loadingBar)
        loadPostDetailData.value = true
        launch {
            apiPost.getFollowingFeed(scheduler = scheduler, authorization = authorization, page = page,
                    responseData = {
                        PrintLog.d("getFollowingFeed success", it.toString(), viewModelTag)

                        val followingFeedData = PostDetailData(total = it.total, nextPage = page + 1)
                        it.postsData?.forEach { postsData ->
                            followingFeedData.items.add(PostDetailListData(
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
                                    isMyPost = false
                            ))
                        }

                        if (page == 1) {
                            this.postDetailData.updateData(postDetailData = followingFeedData)
                            uiData.value = UIModel(isLoading = loadingBar?.let{_->false},
                                    postDetailData = this.postDetailData,
                                    noPostVisibility = if (this.postDetailData.items.isEmpty()) View.VISIBLE else View.GONE)
                        } else {
                            this.postDetailData.addData(postDetailData = followingFeedData)
                            uiData.value = UIModel(isLoading = loadingBar?.let{_->false},
                                    postDetailDataUpdate = true)
                        }
                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(isLoading = loadingBar?.let{_->false})
                        errorData?.let {
                            PrintLog.e("getFollowingFeed fail", errorData.message, viewModelTag)
                            if (errorData.errorCode == HttpURLConnection.HTTP_BAD_REQUEST) {
                                val errorMessage = errorData.message.split(ApiCallBackConstants.DELIMITER_CHARACTER)
                                if (errorMessage.size > 1)
                                    if (errorMessage[1] == ApiCallBackConstants.N0_FOLLOWING_USER) {
                                        this.postDetailData.updateData(postDetailData = PostDetailData())
                                        PrintLog.e("getFollowingFeed fail", "no following user", viewModelTag)
                                        uiData.value = UIModel(noFollowingVisibility = View.VISIBLE, postDetailDataUpdate = true)
                                    }
                            }
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
        loadPostDetailData.value = true
        launch {
            apiPost.requestUserPostsDetail(scheduler = scheduler, authorization = authorization, userId = userId, page = page,
                    responseData = {
                        PrintLog.d("requestUserPostsDetail success", it.toString(), viewModelTag)

                        val newPostsDetailData = PostDetailData(total = it.total, nextPage = page + 1)
                        it.postsData?.forEach { postsData ->
                            newPostsDetailData.items.add(PostDetailListData(
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

                        if (page == 1) {
                            // posts 있고 첫 페이지 경우 -> header 추가
                            if (newPostsDetailData.items.isNotEmpty()) {
                                newPostsDetailData.items.add(0, PostDetailListData(dataType = AppConstants.ADAPTER_HEADER))
                            }
                            this.postDetailData.updateData(postDetailData = newPostsDetailData)
                            uiData.value = UIModel(isLoading = loadingBar?.let{_->false},
                                    postDetailData = this.postDetailData,
                                    noPostVisibility = if (this.postDetailData.items.isEmpty()) View.VISIBLE else View.GONE)
                        } else {
                            this.postDetailData.addData(postDetailData = newPostsDetailData)
                            uiData.value = UIModel(isLoading = loadingBar?.let{_->false},
                                    postDetailDataUpdate= true)
                        }

                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(isLoading = loadingBar?.let{_->false})
                        errorData?.let {
                            PrintLog.e("requestUserPostsDetail fail", errorData.message, viewModelTag)
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
                        PrintLog.d("postDelete success", "", viewModelTag)
                        uiData.value = UIModel(isLoading = false)
                        postDeleteData.value = true
                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(isLoading = false)
                        errorData?.let {
                            PrintLog.e("postDelete fail", errorData.message, viewModelTag)
                        }
                    })
        }
    }

    fun likePost(authorization: String, selectIndex: Int) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = networkCheck.networkErrorMsg))
            return
        }

        uiData.value = UIModel(isLoading = true)
        launch {
            val postData = this.postDetailData.items[selectIndex]
            apiUserActivity.postLike(scheduler = scheduler, authorization = authorization, postId = postData.postId,
                    responseData = {
                        PrintLog.d("postLike success", it.status.toString(), viewModelTag)
                        postData.isLike = it.status
                        postData.likeNum +=
                                if (postData.isLike) 1
                                else -1
                        uiData.value = UIModel(isLoading = false, postDetailUpdateIndex = selectIndex)
                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(isLoading = false)
                        errorData?.let {
                            PrintLog.e("postLike fail", errorData.message, viewModelTag)
                        }
                    })
        }
    }

    fun savePost(context: Context, authorization: String, selectIndex: Int) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = networkCheck.networkErrorMsg))
            return
        }

        uiData.value = UIModel(isLoading = true)
        launch {
            val postData = this.postDetailData.items[selectIndex]
            apiUserActivity.postSave(scheduler = scheduler, authorization = authorization, postId = postData.postId,
                    responseData = {
                        PrintLog.d("postSave success", it.status.toString(), viewModelTag)
                        postData.isSave = it.status
                        val toastMessage =
                                if (postData.isSave) context.getString(R.string.toast_saved_success)
                                else null
                        uiData.value = UIModel(isLoading = false,
                                postDetailUpdateIndex = selectIndex, toastMessage = toastMessage)
                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(isLoading = false)
                        errorData?.let {
                            PrintLog.e("postSave fail", errorData.message, viewModelTag)
                        }
                    })
        }
    }

    fun userFollow(authorization: String, selectIndex: Int) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = networkCheck.networkErrorMsg))
            return
        }

        uiData.value = UIModel(isLoading = true)
        launch {
            val postData = this.postDetailData.items[selectIndex]
            apiFollow.requestFollow(scheduler = scheduler, authorization = authorization, userId = postData.userId,
                    responseData = {
                        PrintLog.d("follow success", it.status.toString(), viewModelTag)
                        postData.isFollowing = it.status
                        uiData.value = UIModel(isLoading = false, postDetailUpdateIndex = selectIndex)
                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(isLoading = false)
                        errorData?.let {
                            PrintLog.e("follow fail", errorData.message, viewModelTag)
                        }
                    })
        }
    }

    fun backBtnAction() {
        uiData.postValue(UIModel(resultCode = resultCode))
    }
}

data class UIModel(val isLoading: Boolean? = null, val toastMessage: String? = null, val resultCode: Int? = null,
                   val postDetailData: PostDetailData? = null, val postDetailDataUpdate: Boolean? = null, val postDetailUpdateIndex: Int? = null,
                   val noPostVisibility: Int? = null, val noFollowingVisibility: Int? = null, val noLoginVisibility: Int? = null)