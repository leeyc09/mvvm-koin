package xlab.world.xlab.view.posts

import android.app.Activity
import android.arch.lifecycle.MutableLiveData
import android.view.View
import xlab.world.xlab.data.adapter.PostThumbnailData
import xlab.world.xlab.data.adapter.PostThumbnailListData
import xlab.world.xlab.server.provider.ApiPostProvider
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
                     private val apiPost: ApiPostProvider,
                     private val apiUserActivity: ApiUserActivityProvider,
                     private val networkCheck: NetworkCheck,
                     private val scheduler: SchedulerProvider): AbstractViewModel() {
    private val viewModelTag = "Posts"

    private var resultCode = Activity.RESULT_CANCELED

    private var postThumbnailData: PostThumbnailData = PostThumbnailData()

    val loadPostsData = SingleLiveEvent<Boolean?>()
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
        loadPostsData.value = true
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
                                    postVisibility = VisibilityData(myPost = if (this.postThumbnailData.items.isEmpty()) View.VISIBLE else View.GONE, otherPost = View.GONE))
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
        loadPostsData.value = true
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

                        if (page == 1) {
                            this.postThumbnailData.updateData(postThumbnailData = postsThumbnailData)
                            uiData.value = UIModel(isLoading = loadingBar?.let{_->false}, postsData = this.postThumbnailData,
                                    postVisibility = VisibilityData(myPost = if (this.postThumbnailData.items.isEmpty()) View.VISIBLE else View.GONE, otherPost = View.GONE))
                        } else {
                            this.postThumbnailData.addData(postThumbnailData = postsThumbnailData)
                            uiData.value = UIModel(isLoading = loadingBar?.let{_->false}, postsDataUpdate = true)
                        }
                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(isLoading = loadingBar?.let{_->false})
                        errorData?.let {
                            PrintLog.d("requestSavedPosts fail", errorData.message)
                        }
                    })
        }
    }

    fun loadGoodsTaggedPosts(goodsCode: String, page: Int) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = networkCheck.networkErrorMsg))
            return
        }

        uiData.value = UIModel(isLoading = true)
        loadPostsData.value = true
        launch {
            apiShop.requestGoodsTaggedPosts(scheduler = scheduler, goodsCode = goodsCode, page = page,
                    responseData = {
                        PrintLog.d("requestGoodsTaggedPosts success", it.toString(), viewModelTag)
                        val taggedPostsData = PostThumbnailData(total = it.total, nextPage = page + 1)

                        it.postsData?.forEach { post ->
                            taggedPostsData.items.add(PostThumbnailListData(
                                    dataType = AppConstants.ADAPTER_CONTENT,
                                    postId = post.id,
                                    postType = post.postType,
                                    imageURL = post.postFile.firstOrNull(),
                                    youTubeVideoID = post.youTubeVideoID))
                        }

                        if (page == 1) {
                            this.postThumbnailData.updateData(postThumbnailData = taggedPostsData)
                            uiData.value = UIModel(isLoading = false,
                                    postsData = this.postThumbnailData,
                                    postsTotal = this.postThumbnailData.total)
                        } else {
                            // 1 페이지 이상 불러올때, 데이터 없는 경우 더이상 로딩 못하게 막음
                            if (it.postsData == null) {
                                uiData.value = UIModel(isLoading = false)
                                loadPostsData.value = true
                            } else {
                                this.postThumbnailData.addData(postThumbnailData = taggedPostsData)
                                uiData.value = UIModel(isLoading = false, postsDataUpdate = true)
                            }
                        }

                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(isLoading = false)
                        errorData?.let {
                            PrintLog.e("requestGoodsTaggedPosts fail", errorData.message, viewModelTag)
                        }
                    })
        }
    }

    fun loadUserPostsThumbData(userId: String, page:Int, loginUseId: String, loadingBar: Boolean? = true) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = networkCheck.networkErrorMsg))
            return
        }

        uiData.value = UIModel(isLoading = loadingBar)
        loadPostsData.value = true
        launch {
            apiPost.requestUserPostsThumbnail(scheduler = scheduler, userId = userId, page = page,
                    responseData = {
                        PrintLog.d("requestUserPostsThumbnail success", it.toString(), viewModelTag)

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

                        if (page == 1) {
                            // posts 있고 첫 페이지 경우 -> header 추가
                            if (postsThumbData.items.isNotEmpty()) {
                                postsThumbData.items.add(0, PostThumbnailListData(dataType = AppConstants.ADAPTER_HEADER))
                            }
                            this.postThumbnailData.updateData(postThumbnailData = postsThumbData)
                            val emptyPost = this.postThumbnailData.items.isEmpty()

                            val myPostVisibility = if (userId == loginUseId && emptyPost) View.VISIBLE else View.GONE
                            val otherPostVisibility = if (userId != loginUseId && emptyPost) View.VISIBLE else View.GONE
                            uiData.value = UIModel(isLoading = loadingBar?.let{_->false},
                                    postsData = this.postThumbnailData,
                                    postVisibility = VisibilityData(myPost = myPostVisibility, otherPost = otherPostVisibility))
                        } else {
                            this.postThumbnailData.addData(postThumbnailData = postsThumbData)
                            uiData.value = UIModel(isLoading = loadingBar?.let{_->false},
                                    postsDataUpdate = true)
                        }
                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(isLoading = loadingBar?.let{_->false})
                        errorData?.let {
                            PrintLog.e("requestUserPostsThumbnail fail", errorData.message, viewModelTag)
                        }
                    })
        }
    }

    fun backBtnAction() {
        uiData.postValue(UIModel(resultCode = resultCode))
    }
}

data class VisibilityData(val myPost: Int, val otherPost: Int)
data class UIModel(val isLoading: Boolean? = null, val toastMessage: String? = null, val resultCode: Int? = null,
                   val postVisibility: VisibilityData? = null, val postsTotal: Int? = null,
                   val postsData: PostThumbnailData? = null, val postsDataUpdate: Boolean? = null)