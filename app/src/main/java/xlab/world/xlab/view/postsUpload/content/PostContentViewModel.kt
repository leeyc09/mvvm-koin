package xlab.world.xlab.view.postsUpload.content

import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.graphics.Point
import android.view.View
import io.reactivex.Observable
import xlab.world.xlab.R
import xlab.world.xlab.data.adapter.*
import xlab.world.xlab.data.request.ReqPostUploadData
import xlab.world.xlab.server.provider.ApiHashTagProvider
import xlab.world.xlab.server.provider.ApiPostProvider
import xlab.world.xlab.utils.rx.SchedulerProvider
import xlab.world.xlab.utils.rx.with
import xlab.world.xlab.utils.support.*
import xlab.world.xlab.utils.view.hashTag.HashTagHelper
import xlab.world.xlab.view.AbstractViewModel
import xlab.world.xlab.view.SingleLiveEvent

class PostContentViewModel(private val apiPost: ApiPostProvider,
                           private val apiHashTag: ApiHashTagProvider,
                           private val networkCheck: NetworkCheck,
                           private val scheduler: SchedulerProvider): AbstractViewModel() {
    val tag = "PostContent"

    private var postId: String = ""
    private var isPostUpload: Boolean = true // false - 포스트 업데이트
    private var imagePathList: ArrayList<String> = ArrayList()
    private var youTubeVideoId: String = ""
    private var recentHashTagEmpty: Boolean = false
    private var lastSearchHasTag = ""
    var hashTagStartEndPoint = Point()

    private var hashTagNowState: Int = HashTagHelper.stateNoTag

    val loadPostEvent = SingleLiveEvent<LoadPostEventData>()
    val setPostIdEvent = SingleLiveEvent<PostContentEventData>()
    val savePostEvent = SingleLiveEvent<PostContentEventData>()
    val searchHashTagEvent = SingleLiveEvent<PostContentEventData>()
    val uiData = MutableLiveData<UIModel>()

    fun setPostId(postId: String) {
        this.postId = postId
        isPostUpload = postId.isEmpty()

        setPostIdEvent.value = PostContentEventData(status = isPostUpload)
    }

    fun initViewModelData(imagePaths: ArrayList<String>, youTubeVideoId: String) {
        uiData.value = UIModel(isLoading = true)
        launch {
            Observable.create<Boolean> {
                this.imagePathList.clear()
                this.imagePathList.addAll(imagePaths)

                this.youTubeVideoId = youTubeVideoId

                PrintLog.d("imagePathList", imagePathList.toString())

                it.onNext(this.youTubeVideoId.isNotEmpty())
                it.onComplete()
            }.with(scheduler).subscribe { isYouTubePost ->
                val postUploadPictureData = PostUploadPictureData()
                this.imagePathList.forEach { imagePath ->
                    postUploadPictureData.items.add(PostUploadPictureListData(imagePath = imagePath))
                }
                uiData.value = UIModel(isLoading = false, youtubeThumbnailVisible = if (isYouTubePost) View.VISIBLE else View.GONE,
                        postUploadPictureData = postUploadPictureData)
            }
        }
    }

    fun setHashTagStartEndPoint(start: Int, end: Int) {
        hashTagStartEndPoint.x = start
        hashTagStartEndPoint.y = end
    }

    fun changeHashTagState(hashTagNowState: Int) {
        PrintLog.d("this hashTagNowState", this.hashTagNowState.toString())
        PrintLog.d("hashTagNowState", hashTagNowState.toString())
        if (this.hashTagNowState != hashTagNowState) {
            launch {
                Observable.create<ArrayList<Int>> {
                    this.hashTagNowState = hashTagNowState
                    val visibilityList = ArrayList<Int>()
                    if (hashTagNowState == HashTagHelper.stateNoTag) { // 일반 텍스트 입력 타입
                        visibilityList.add(View.VISIBLE) // normal popup
                        visibilityList.add(View.GONE) // hash tag popup
                    } else {
                        if (hashTagNowState == HashTagHelper.stateOnlyTag && recentHashTagEmpty) {
                            visibilityList.add(View.VISIBLE) // normal popup
                            visibilityList.add(View.GONE) // hash tag popup
                        } else { // 해시 태그 목록 보여주기
                            visibilityList.add(View.GONE) // normal popup
                            visibilityList.add(View.VISIBLE) // hash tag popup
                        }
                    }

                    it.onNext(visibilityList)
                    it.onComplete()
                }.with(scheduler).subscribe { visibilityList ->
                    uiData.value = UIModel(normalPopupVisibility = visibilityList[0],
                            hashTagPopupVisibility = visibilityList[1])
                }
            }
        }
    }

    fun loadRecentHashTagData(authorization: String) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = networkCheck.networkErrorMsg))
            return
        }

        uiData.value = UIModel(isLoading = true)
        launch {
            apiHashTag.requestRecentHashTag(scheduler = scheduler, authorization = authorization,
                    responseData = {
                        PrintLog.d("requestRecentHashTag success", it.toString())
                        val recentHashTagData = RecentHashTagData()
                        it.hashTagList?.forEach { hashTag ->
                            recentHashTagData.items.add(RecentHashTagListData(hashTag = hashTag))
                        }
                        recentHashTagEmpty = recentHashTagData.items.isEmpty()
                        uiData.value = UIModel(isLoading = false, recentHashTagData = recentHashTagData)
                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(isLoading = false)
                        errorData?.let {
                            PrintLog.d("requestRecentHashTag fail", errorData.message)
                        }
                    })
        }
    }

    fun loadSearchHashTagData(authorization: String, searchText: String, page: Int) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = networkCheck.networkErrorMsg))
            return
        }

        lastSearchHasTag = searchText
        searchHashTagEvent.value = PostContentEventData(status = true)
        launch {
            apiHashTag.searchHashTagCount(scheduler = scheduler, authorization = authorization, searchText = searchText,
                    responseData = {
                        PrintLog.d("searchHashTagCount success", it.toString())
                        if (lastSearchHasTag == searchText) { // 최종 검색 텍스트와 같을 경우만 데이터 반환
                            val searchHashTagData = SearchHashTagData(total = it.total, nextPage = page + 1, searchText = searchText)
                            it.hashTagData?.forEach { hashTagData ->
                                searchHashTagData.items.add(SearchHashTagListData(hashTag = hashTagData.tagName, hashTagCnt = hashTagData.count))
                            } ?: searchHashTagData.items.add(SearchHashTagListData(hashTag = lastSearchHasTag, hashTagCnt = 0))

                            uiData.value = UIModel(searchHashTagData = searchHashTagData)
                        }
                    },
                    errorData = { errorData ->
                        errorData?.let {
                            PrintLog.d("searchHashTagCount fail", errorData.message)
                        }
                    })
        }
    }

    fun loadPost(context: Context, authorization: String) {
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
                        uiData.value = UIModel(isLoading = false)

                        val goodsData = ArrayList<SelectUsedGoodsListData>()
                        it.postsData.goods.forEach { goods ->
                            goodsData.add(SelectUsedGoodsListData(
                                    dataType = AppConstants.SELECTED_GOODS_WITH_INFO,
                                    goodsCode = goods.code,
                                    imageURL = goods.image,
                                    goodsName = goods.name,
                                    goodsBrand = goods.brand
                            ))
                        }
                        loadPostEvent.value = LoadPostEventData(content = it.postsData.content, goodsData = goodsData)

                        initViewModelData(imagePaths = it.postsData.postFile,
                                youTubeVideoId = it.postsData.youTubeVideoID)
                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(isLoading = false, toastMessage = context.getString(R.string.toast_no_exist_post))
                        loadPostEvent.value = LoadPostEventData(isFail = true)
                        errorData?.let {
                            PrintLog.d("getPostDetail fail", errorData.message)
                        }
                    })
        }
    }

    fun savePost(authorization: String, content: String, hashTags: ArrayList<String>,
                 goodsData: ArrayList<SelectUsedGoodsListData>, imagePaths: ArrayList<String>) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = networkCheck.networkErrorMsg))
            return
        }

        uiData.value = UIModel(isLoading = true)
        launch {
            val postType =
                    if (youTubeVideoId.isNotEmpty()) AppConstants.POSTS_YOUTUBE_LINK
                    else AppConstants.POSTS_IMAGE

            val reqPostUploadData = ReqPostUploadData()
            // set post type
            reqPostUploadData.addPostType(postType = postType)
            // set content
            reqPostUploadData.addContent(content = content)
            // set hash tag
            reqPostUploadData.addTag(hashTags = hashTags)
            // set goods
            reqPostUploadData.addGoods(goodsData = goodsData)
            // set post image file
            reqPostUploadData.addPostFiles(imagePaths = imagePaths)
            // set youtube video id
            reqPostUploadData.addYouTubeVideoId(youTubeVideoId = youTubeVideoId)

            if (isPostUpload) // 포스트 업로드
                apiPost.requestUploadPost(scheduler = scheduler, authorization = authorization, requestBody = reqPostUploadData.getReqBody(),
                        responseData = {
                            PrintLog.d("requestUploadPost success", "")
                            uiData.value = UIModel(isLoading = false)
                            savePostEvent.value = PostContentEventData(status = true)
                        },
                        errorData = { errorData ->
                            uiData.value = UIModel(isLoading = false)
                            errorData?.let {
                                PrintLog.d("requestUploadPost fail", errorData.message)
                            }
                        })
            else // 포스트 업데이트
                apiPost.requestUpdatePost(scheduler = scheduler, authorization = authorization, postId = postId, requestBody = reqPostUploadData.getReqBody(),
                        responseData = {
                            PrintLog.d("requestUpdatePost success", "")
                            uiData.value = UIModel(isLoading = false)
                            savePostEvent.value = PostContentEventData(status = true)
                        },
                        errorData = { errorData ->
                            uiData.value = UIModel(isLoading = false)
                            errorData?.let {
                                PrintLog.d("requestUpdatePost fail", errorData.message)
                            }
                        })
        }
    }

    fun deleteProfileImage() {
        imagePathList.forEach { filePath ->
            PrintLog.d("deleteFile", filePath)
            SupportData.deleteFile(path = filePath)
        }
    }
}

data class LoadPostEventData(val content: String? = null, val goodsData: ArrayList<SelectUsedGoodsListData>? = null,
                             val isFail: Boolean? = null)
data class PostContentEventData(val status: Boolean? = null)
data class UIModel(val isLoading: Boolean? = null, val toastMessage: String? = null,
                   val normalPopupVisibility: Int? = null, val hashTagPopupVisibility: Int? = null,
                   val youtubeThumbnailVisible: Int? = null, val postUploadPictureData: PostUploadPictureData? = null,
                   val recentHashTagData: RecentHashTagData? = null,
                   val searchHashTagData: SearchHashTagData? = null)
