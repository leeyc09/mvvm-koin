package xlab.world.xlab.view.postsUpload.content

import android.arch.lifecycle.MutableLiveData
import android.graphics.Point
import android.view.View
import io.reactivex.Observable
import xlab.world.xlab.data.adapter.*
import xlab.world.xlab.server.provider.ApiHashTagProvider
import xlab.world.xlab.server.provider.ApiPostProvider
import xlab.world.xlab.utils.rx.SchedulerProvider
import xlab.world.xlab.utils.rx.with
import xlab.world.xlab.utils.support.NetworkCheck
import xlab.world.xlab.utils.support.PrintLog
import xlab.world.xlab.utils.support.SupportData
import xlab.world.xlab.utils.support.TextConstants
import xlab.world.xlab.utils.view.hashTag.HashTagHelper
import xlab.world.xlab.view.AbstractViewModel
import xlab.world.xlab.view.SingleLiveEvent

class PostContentViewModel(private val apiPost: ApiPostProvider,
                           private val apiHashTag: ApiHashTagProvider,
                           private val networkCheck: NetworkCheck,
                           private val scheduler: SchedulerProvider): AbstractViewModel() {
    val tag = "PostContent"

    private var imagePathList: ArrayList<String> = ArrayList()
    private var youTubeVideoId: String = ""
    private var recentHashTagEmpty: Boolean = false
    private var lastSearchHasTag = ""
    var hashTagStartEndPoint = Point()

    private var hashTagNowState: Int = HashTagHelper.stateNoTag

    val searchHashTagEvent = SingleLiveEvent<PostContentEventData>()
    val uiData = MutableLiveData<UIModel>()

    fun initViewModelData(imagePaths: ArrayList<String>, youTubeVideoId: String) {
        uiData.value = UIModel(isLoading = true)
        launch {
            Observable.create<Boolean> {
                this.imagePathList.clear()
                this.imagePathList.addAll(imagePaths)

                this.youTubeVideoId = youTubeVideoId

                PrintLog.d("imagePathList", imagePathList.toString(), tag)

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
        PrintLog.d("this hashTagNowState", this.hashTagNowState.toString(), tag)
        PrintLog.d("hashTagNowState", hashTagNowState.toString(), tag)
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
            uiData.postValue(UIModel(toastMessage = TextConstants.CHECK_NETWORK_CONNECT))
            return
        }

        uiData.value = UIModel(isLoading = true)
        launch {
            apiHashTag.requestRecentHashTag(scheduler = scheduler, authorization = authorization,
                    responseData = {
                        PrintLog.d("requestRecentHashTag success", it.toString(), tag)
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
                            PrintLog.d("requestRecentHashTag fail", errorData.message, tag)
                        }
                    })
        }
    }

    fun loadSearchHashTagData(authorization: String, searchText: String, page: Int) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = TextConstants.CHECK_NETWORK_CONNECT))
            return
        }

        lastSearchHasTag = searchText
        searchHashTagEvent.value = PostContentEventData(status = true)
        launch {
            apiHashTag.searchHashTagCount(scheduler = scheduler, authorization = authorization, searchText = searchText,
                    responseData = {
                        PrintLog.d("searchHashTagCount success", it.toString(), tag)
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
                            PrintLog.d("searchHashTagCount fail", errorData.message, tag)
                        }
                    })
        }
    }

    fun deleteProfileImage() {
        imagePathList.forEach { filePath ->
            PrintLog.d("deleteFile", filePath, tag)
            SupportData.deleteFile(path = filePath)
        }
    }
}

data class PostContentEventData(val status: Boolean? = null)
data class UIModel(val isLoading: Boolean? = null, val toastMessage: String? = null,
                   val normalPopupVisibility: Int? = null, val hashTagPopupVisibility: Int? = null,
                   val youtubeThumbnailVisible: Int? = null, val postUploadPictureData: PostUploadPictureData? = null,
                   val recentHashTagData: RecentHashTagData? = null,
                   val searchHashTagData: SearchHashTagData? = null)
