package xlab.world.xlab.view.main

import android.arch.lifecycle.MutableLiveData
import android.graphics.Color
import xlab.world.xlab.R
import xlab.world.xlab.data.adapter.AllFeedData
import xlab.world.xlab.data.adapter.AllFeedListData
import xlab.world.xlab.server.ApiURL
import xlab.world.xlab.server.provider.ApiPostProvider
import xlab.world.xlab.utils.rx.SchedulerProvider
import xlab.world.xlab.utils.support.*
import xlab.world.xlab.view.AbstractViewModel
import xlab.world.xlab.view.SingleLiveEvent
import java.net.HttpURLConnection
import java.util.*

class MainViewModel(private val apiPost: ApiPostProvider,
                    private val networkCheck: NetworkCheck,
                    private val scheduler: SchedulerProvider): AbstractViewModel() {
    val tag = "Main"

    val loadAllFeedDataEvent = SingleLiveEvent<LoadFeedDataEvent>()
    val loadFollowingFeedDataEvent = SingleLiveEvent<LoadFeedDataEvent>()
    val uiData = MutableLiveData<UIModel>()

    fun loadAllFeedData(authorization: String, page: Int, topicColorList: Array<String>) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = MessageConstants.CHECK_NETWORK_CONNECT))
            return
        }

        uiData.value = UIModel(isLoading = true)
        loadAllFeedDataEvent.value = LoadFeedDataEvent(isLoading = true)
        launch {
            apiPost.getAllFeed(scheduler = scheduler, authorization = authorization, page = page,
                    responseData = {
                        val allFeedData = AllFeedData(nextPage = page + 1)
                        it.feedData?.forEach { feedData ->
                            when (feedData.dataType) {
                                AppConstants.FEED_POST -> { // 포스트
                                    allFeedData.items.add(AllFeedListData(
                                            dataType = feedData.dataType,
                                            imageURL = feedData.posts!!.postFile.firstOrNull(),
                                            postId = feedData.posts.id,
                                            postsType = feedData.posts.postType,
                                            youTubeVideoID = feedData.posts.youTubeVideoID))
                                }
                                AppConstants.FEED_GOODS -> { // 상품
                                    val randIndex = Random()
                                    var percentValue = 0
                                    var percentColor = Color.parseColor("#${topicColorList[randIndex.nextInt(topicColorList.size)]}")
                                    feedData.goods!!.topicMatch?.let { topicMatch ->
                                        percentValue = topicMatch[0].match
                                        percentColor = Color.parseColor("#${topicMatch[0].topicColor}")
                                    }
                                    allFeedData.items.add(AllFeedListData(
                                            dataType = feedData.dataType,
                                            imageURL = ApiURL.GODO_IMAGE_HEADER_URL + feedData.goods.image,
                                            matchingPercent = percentValue,
                                            matchColor = percentColor,
                                            showQuestionMark = feedData.goods.topicMatch == null,
                                            goodsCd = feedData.goods.code))
                                }
                            }
                        }

                        PrintLog.d("getAllFeed success", allFeedData.toString(), tag)
                        uiData.value = UIModel(isLoading = false, allFeedData = allFeedData)
                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(isLoading = false)
                        errorData?.let {
                            PrintLog.d("getAllFeed fail", errorData.message, tag)
                        }
                    })
        }
    }

    fun loadFollowingFeedData(authorization: String, page: Int) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = MessageConstants.CHECK_NETWORK_CONNECT))
            return
        }

        // login 확인
        if (isGuest(authorization)) { // guest
            uiData.value = UIModel(guestMode = true)
            return
        }

        uiData.value = UIModel(isLoading = true)
        loadFollowingFeedDataEvent.value = LoadFeedDataEvent(isLoading = true)
        launch {
            apiPost.getFollowingFeed(scheduler = scheduler, authorization = authorization, page = page,
                    responseData = {
                        uiData.value = UIModel(isLoading = false)
                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(isLoading = false)
                        errorData?.let {
                            PrintLog.d("getFollowingFeed fail", errorData.message, tag)
                            if (errorData.errorCode == HttpURLConnection.HTTP_BAD_REQUEST) {
                                val errorMessage = errorData.message.split(ApiCallBackConstants.DELIMITER_CHARACTER)
                                if (errorMessage.size > 1)
                                    if (errorMessage[1] == ApiCallBackConstants.N0_FOLLOWING_USER) {
                                        PrintLog.d("getFollowingFeed fail", "no following user", tag)
                                        uiData.value = UIModel(noFollowing = true)
                                    }
                            }
                        }
                    })
        }
    }

    private fun isGuest(authorization: String): Boolean {
        return authorization.replace("Bearer", "").trim().isEmpty()
    }
}

data class LoadFeedDataEvent(val isLoading: Boolean? = null)
data class UIModel(val isLoading: Boolean? = null, val toastMessage: String? = null,
                   val allFeedData: AllFeedData? = null,
                   val guestMode: Boolean? = null, val noFollowing: Boolean? = null)