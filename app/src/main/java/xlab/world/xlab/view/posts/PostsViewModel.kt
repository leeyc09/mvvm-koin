package xlab.world.xlab.view.posts

import android.arch.lifecycle.MutableLiveData
import xlab.world.xlab.data.adapter.PostThumbnailData
import xlab.world.xlab.data.adapter.PostThumbnailListData
import xlab.world.xlab.server.provider.ApiPostProvider
import xlab.world.xlab.server.provider.ApiUserActivityProvider
import xlab.world.xlab.utils.rx.SchedulerProvider
import xlab.world.xlab.utils.support.AppConstants
import xlab.world.xlab.utils.support.NetworkCheck
import xlab.world.xlab.utils.support.PrintLog
import xlab.world.xlab.utils.support.TextConstants
import xlab.world.xlab.view.AbstractViewModel
import xlab.world.xlab.view.SingleLiveEvent

class PostsViewModel(private val apiPost: ApiPostProvider,
                     private val apiUserActivity: ApiUserActivityProvider,
                     private val networkCheck: NetworkCheck,
                     private val scheduler: SchedulerProvider): AbstractViewModel() {
    val tag = "Posts"

    val loadPostsEventData = SingleLiveEvent<PostsEvent>()
    val uiData = MutableLiveData<UIModel>()

    fun loadLikedPostsData(authorization: String, page: Int, loadingBar: Boolean? = true) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = TextConstants.CHECK_NETWORK_CONNECT))
            return
        }

        uiData.value = UIModel(isLoading = loadingBar)
        loadPostsEventData.postValue(PostsEvent(status = true))
        launch {
            apiUserActivity.requestLikedPosts(scheduler = scheduler, authorization = authorization, page = page,
                    responseData = {
                        PrintLog.d("requestLikedPosts success", it.toString(), tag)
                        val postsThumbnailData = PostThumbnailData(total = it.total, nextPage = page + 1)
                        it.likedPostsData?.forEach { post ->
                            postsThumbnailData.items.add(PostThumbnailListData(
                                    dataType = AppConstants.ADAPTER_CONTENT,
                                    postId = post.id,
                                    postType = post.postType,
                                    imageURL = post.postFile.firstOrNull(),
                                    youTubeVideoID = post.youTubeVideoID
                            ))
                        }
                        uiData.value = UIModel(isLoading = loadingBar?.let{_->false}, postsData = postsThumbnailData)
                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(isLoading = loadingBar?.let{_->false})
                        errorData?.let {
                            PrintLog.d("requestLikedPosts fail", errorData.message, tag)
                        }
                    })
        }
    }

    fun loadSavedPostsData(authorization: String, page: Int, loadingBar: Boolean? = true) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = TextConstants.CHECK_NETWORK_CONNECT))
            return
        }

        uiData.value = UIModel(isLoading = loadingBar)
        loadPostsEventData.postValue(PostsEvent(status = true))
        launch {
            apiUserActivity.requestSavedPosts(scheduler = scheduler, authorization = authorization, page = page,
                    responseData = {
                        PrintLog.d("requestSavedPosts success", it.toString(), tag)
                        val postsThumbnailData = PostThumbnailData(total = it.total, nextPage = page + 1)
                        it.likedPostsData?.forEach { post ->
                            postsThumbnailData.items.add(PostThumbnailListData(
                                    dataType = AppConstants.ADAPTER_CONTENT,
                                    postId = post.id,
                                    postType = post.postType,
                                    imageURL = post.postFile.firstOrNull(),
                                    youTubeVideoID = post.youTubeVideoID
                            ))
                        }
                        uiData.value = UIModel(isLoading = loadingBar?.let{_->false}, postsData = postsThumbnailData)
                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(isLoading = loadingBar?.let{_->false})
                        errorData?.let {
                            PrintLog.d("requestSavedPosts fail", errorData.message, tag)
                        }
                    })
        }
    }
}

data class PostsEvent(val status: Boolean? = null)
data class UIModel(val isLoading: Boolean? = null, val toastMessage: String? = null,
                   val postsData: PostThumbnailData? = null)