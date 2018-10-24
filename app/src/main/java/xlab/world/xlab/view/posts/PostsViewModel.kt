package xlab.world.xlab.view.posts

import android.app.Activity
import android.arch.lifecycle.MutableLiveData
import android.view.View
import xlab.world.xlab.data.adapter.PostThumbnailData
import xlab.world.xlab.data.adapter.PostThumbnailListData
import xlab.world.xlab.server.provider.ApiShopProvider
import xlab.world.xlab.server.provider.ApiUserActivityProvider
import xlab.world.xlab.utils.rx.SchedulerProvider
import xlab.world.xlab.utils.support.AppConstants
import xlab.world.xlab.utils.support.NetworkCheck
import xlab.world.xlab.utils.support.PrintLog
import xlab.world.xlab.utils.support.ResultCodeData
import xlab.world.xlab.view.AbstractViewModel
import xlab.world.xlab.view.SingleLiveEvent

class PostsViewModel(private val apiShop: ApiShopProvider,
                     private val apiUserActivity: ApiUserActivityProvider,
                     private val networkCheck: NetworkCheck,
                     private val scheduler: SchedulerProvider): AbstractViewModel() {
    private val viewModelTag = "Posts"

    private var resultCode = Activity.RESULT_CANCELED

    private var postThumbnailData: PostThumbnailData = PostThumbnailData()

    val loadPostsEventData = SingleLiveEvent<PostsEvent>()
    val goodsTaggedPostsEventData = SingleLiveEvent<PostsEvent>()
    val uiData = MutableLiveData<UIModel>()

    fun setResultCode(resultCode: Int) {
        when (resultCode) {
            ResultCodeData.LOGOUT_SUCCESS -> {
                this.resultCode = resultCode
            }
            Activity.RESULT_OK -> {
                if (this.resultCode == Activity.RESULT_CANCELED)
                    this.resultCode = resultCode
            }
        }
    }

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
                        PrintLog.d("requestLikedPosts success", it.toString(), viewModelTag)
                        val likedPostsData = PostThumbnailData(total = it.total, nextPage = page + 1)
                        it.postsData?.forEach { post ->
                            likedPostsData.items.add(PostThumbnailListData(
                                    dataType = AppConstants.ADAPTER_CONTENT,
                                    postId = post.id,
                                    postType = post.postType,
                                    imageURL = post.postFile.firstOrNull(),
                                    youTubeVideoID = post.youTubeVideoID
                            ))
                        }
                        if (page == 1) {
                            this.postThumbnailData.updateData(postThumbnailData = likedPostsData)
                            uiData.value = UIModel(isLoading = loadingBar?.let{_->false},
                                    postsData = this.postThumbnailData,
                                    emptyPostVisibility = if (this.postThumbnailData.items.isEmpty()) View.VISIBLE else View.GONE)
                        } else {
                            this.postThumbnailData.addData(postThumbnailData = likedPostsData)
                            uiData.value = UIModel(isLoading = loadingBar?.let{_->false},
                                    postsDataUpdate = true)
                        }
                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(isLoading = loadingBar?.let{_->false})
                        errorData?.let {
                            PrintLog.e("requestLikedPosts fail", errorData.message, viewModelTag)
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
                        PrintLog.d("requestSavedPosts success", it.toString())
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
                            PrintLog.d("requestSavedPosts fail", errorData.message)
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
                        PrintLog.d("requestGoodsTaggedPosts success", it.toString())
                        val taggedPostsData = PostThumbnailData(total = it.total, nextPage = page + 1)

                        run postsData@ {
                            it.postsData?.forEachIndexed { index, post ->
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
                        }
                        uiData.value = UIModel(isLoading = false, postsData = taggedPostsData)
                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(isLoading = false)
                        errorData?.let {
                            PrintLog.d("requestGoodsTaggedPosts fail", errorData.message)
                        }
                    })
        }
    }

    fun backBtnAction() {
        uiData.postValue(UIModel(resultCode = resultCode))
    }
}

data class PostsEvent(val status: Boolean? = null)
data class UIModel(val isLoading: Boolean? = null, val toastMessage: String? = null, val resultCode: Int? = null,
                   val emptyPostVisibility: Int? = null,
                   val postsData: PostThumbnailData? = null, val postsDataUpdate: Boolean? = null)