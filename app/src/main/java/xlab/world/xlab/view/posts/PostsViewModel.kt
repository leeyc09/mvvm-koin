package xlab.world.xlab.view.posts

import android.arch.lifecycle.MutableLiveData
import xlab.world.xlab.data.adapter.PostThumbnailData
import xlab.world.xlab.data.adapter.PostThumbnailListData
import xlab.world.xlab.server.provider.ApiPostProvider
import xlab.world.xlab.server.provider.ApiShopProvider
import xlab.world.xlab.server.provider.ApiUserActivityProvider
import xlab.world.xlab.utils.rx.SchedulerProvider
import xlab.world.xlab.utils.support.AppConstants
import xlab.world.xlab.utils.support.NetworkCheck
import xlab.world.xlab.utils.support.PrintLog
import xlab.world.xlab.view.AbstractViewModel
import xlab.world.xlab.view.SingleLiveEvent

class PostsViewModel(private val apiShop: ApiShopProvider,
                     private val apiUserActivity: ApiUserActivityProvider,
                     private val networkCheck: NetworkCheck,
                     private val scheduler: SchedulerProvider): AbstractViewModel() {
    val tag = "Posts"

    val loadPostsEventData = SingleLiveEvent<PostsEvent>()
    val goodsTaggedPostsEventData = SingleLiveEvent<PostsEvent>()
    val uiData = MutableLiveData<UIModel>()

    fun loadLikedPostsData(authorization: String, page: Int, loadingBar: Boolean? = true) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = networkCheck.networkErrorMsg))
            return
        }

        uiData.value = UIModel(isLoading = loadingBar)
        loadPostsEventData.postValue(PostsEvent(status = true))
        launch {
            apiUserActivity.requestLikedPosts(scheduler = scheduler, authorization = authorization, page = page,
                    responseData = {
                        PrintLog.d("requestLikedPosts success", it.toString(), tag)
                        val postsThumbnailData = PostThumbnailData(total = it.total, nextPage = page + 1)
                        it.postsData?.forEach { post ->
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
            uiData.postValue(UIModel(toastMessage = networkCheck.networkErrorMsg))
            return
        }

        uiData.value = UIModel(isLoading = loadingBar)
        loadPostsEventData.postValue(PostsEvent(status = true))
        launch {
            apiUserActivity.requestSavedPosts(scheduler = scheduler, authorization = authorization, page = page,
                    responseData = {
                        PrintLog.d("requestSavedPosts success", it.toString(), tag)
                        val postsThumbnailData = PostThumbnailData(total = it.total, nextPage = page + 1)
                        it.postsData?.forEach { post ->
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

    fun loadGoodsTaggedPosts(goodsCode: String, page: Int, limitCnt: Int? = null) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = networkCheck.networkErrorMsg))
            return
        }

        uiData.value = UIModel(isLoading = true)
        goodsTaggedPostsEventData.value = PostsEvent(status = true)
        launch {
            apiShop.requestGoodsTaggedPosts(scheduler = scheduler, goodsCode = goodsCode, page = page,
                    responseData = {
                        PrintLog.d("requestGoodsTaggedPosts success", it.toString(), tag)
                        val taggedPostsData = PostThumbnailData(total = it.total, nextPage = page + 1)

                        it.postsData?.forEachIndexed postsData@ { index, post ->
                            limitCnt?.let { limitCnt ->
                                if (index > limitCnt) return@postsData
                            }
                            taggedPostsData.items.add(PostThumbnailListData(
                                    dataType = AppConstants.ADAPTER_CONTENT,
                                    postId = post.id,
                                    postType = post.postType,
                                    imageURL = post.postFile.firstOrNull(),
                                    youTubeVideoID = post.youTubeVideoID))
                        }
                        uiData.value = UIModel(isLoading = false, postsData = taggedPostsData)
                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(isLoading = false)
                        errorData?.let {
                            PrintLog.d("requestGoodsTaggedPosts fail", errorData.message, tag)
                        }
                    })
        }
    }
}

data class PostsEvent(val status: Boolean? = null)
data class UIModel(val isLoading: Boolean? = null, val toastMessage: String? = null,
                   val postsData: PostThumbnailData? = null)