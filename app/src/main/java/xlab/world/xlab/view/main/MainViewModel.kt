package xlab.world.xlab.view.main

import android.arch.lifecycle.MutableLiveData
import android.graphics.Color
import xlab.world.xlab.data.adapter.*
import xlab.world.xlab.data.response.ResFeedData
import xlab.world.xlab.data.response.ResShopFeedData
import xlab.world.xlab.server.ApiURL
import xlab.world.xlab.server.provider.ApiPostProvider
import xlab.world.xlab.server.provider.ApiShopProvider
import xlab.world.xlab.utils.rx.SchedulerProvider
import xlab.world.xlab.utils.support.*
import xlab.world.xlab.view.AbstractViewModel
import xlab.world.xlab.view.SingleLiveEvent
import java.net.HttpURLConnection
import java.util.*

class MainViewModel(private val apiPost: ApiPostProvider,
                    private val apiShop: ApiShopProvider,
                    private val networkCheck: NetworkCheck,
                    private val scheduler: SchedulerProvider): AbstractViewModel() {
    val tag = "Main"

    val loadAllFeedDataEvent = SingleLiveEvent<LoadFeedDataEvent>()
    val loadFollowingFeedDataEvent = SingleLiveEvent<LoadFeedDataEvent>()
    val loadExploreFeedDataEvent = SingleLiveEvent<LoadFeedDataEvent>()
    val uiData = MutableLiveData<UIModel>()

    fun loadAllFeedData(authorization: String, page: Int, topicColorList: Array<String>) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = TextConstants.CHECK_NETWORK_CONNECT))
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
                                    val matchData = getPercentValueAndColor(topicColorList = topicColorList,
                                            matchData = feedData.goods!!.topicMatch?.firstOrNull())
                                    allFeedData.items.add(AllFeedListData(
                                            dataType = feedData.dataType,
                                            imageURL = ApiURL.GODO_IMAGE_HEADER_URL + feedData.goods.image,
                                            matchingPercent = matchData.percent,
                                            matchColor = matchData.color,
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
            uiData.postValue(UIModel(toastMessage = TextConstants.CHECK_NETWORK_CONNECT))
            return
        }

        // login 확인
        if (SupportData.isGuest(authorization)) { // guest
            uiData.value = UIModel(guestMode = true)
            return
        }

        uiData.value = UIModel(isLoading = true)
        loadFollowingFeedDataEvent.value = LoadFeedDataEvent(isLoading = true)
        launch {
            apiPost.getFollowingFeed(scheduler = scheduler, authorization = authorization, page = page,
                    responseData = {
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

                        PrintLog.d("getFollowingFeed success", followingFeedData.toString(), tag)
                        uiData.value = UIModel(isLoading = false, followingFeedData = followingFeedData)
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

    fun loadExploreFeedData(authorization: String, page: Int) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = TextConstants.CHECK_NETWORK_CONNECT))
            return
        }

        uiData.value = UIModel(isLoading = true)
        loadExploreFeedDataEvent.value = LoadFeedDataEvent(isLoading = true)
        launch {
            apiPost.getExploreFeed(scheduler = scheduler, authorization = authorization, page = page,
                    responseData = {
                        val exploreFeedData = ExploreFeedData(nextPage = page + 1)
                        it.feedData?.forEach { feedData ->
                            when (feedData.dataType) {
                                AppConstants.FEED_POST -> { // 포스트
                                    exploreFeedData.items.add(ExploreFeedListData(
                                            dataType = feedData.dataType,
                                            imageURL = feedData.posts!!.postFile.firstOrNull(),
                                            postId = feedData.posts.id,
                                            postsType = feedData.posts.postType,
                                            youTubeVideoID = feedData.posts.youTubeVideoID))
                                }
                                AppConstants.FEED_GOODS -> { // 상품
                                    exploreFeedData.items.add(ExploreFeedListData(
                                            dataType = feedData.dataType,
                                            imageURL = ApiURL.GODO_IMAGE_HEADER_URL + feedData.goods!!.image,
                                            goodsCd = feedData.goods.code))
                                }
                            }
                        }

                        PrintLog.d("getExploreFeed success", exploreFeedData.toString(), tag)
                        uiData.value = UIModel(isLoading = false, exploreFeedData = exploreFeedData)
                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(isLoading = false)
                        errorData?.let {
                            PrintLog.d("getExploreFeed fail", errorData.message, tag)
                        }
                    })
        }
    }

    fun loadShopFeedData(authorization: String, topicColorList: Array<String>) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = TextConstants.CHECK_NETWORK_CONNECT))
            return
        }

        uiData.value = UIModel(isLoading = true)
        launch {
            apiShop.getShopFeed(scheduler = scheduler, authorization = authorization,
                    responseData = {
                        val shopFeedData = ShopFeedData()
                        // 검색창
                        shopFeedData.items.add(0, ShopFeedListData(dataType = AppConstants.ADAPTER_HEADER))
                        // living 카테고리
                        val livingGoods = getEachCategoryGoods(categoryGoods = it.livingGoods, topicColorList = topicColorList)
                        shopFeedData.items.add(ShopFeedListData(
                                dataType = AppConstants.ADAPTER_CONTENT,
                                categoryText = TextConstants.CATEGORY_LIVING,
                                categoryCode = TextConstants.CATEGORY_LIVING_CODE,
                                goodsData = livingGoods
                        ))
                        // fashion 카테고리
                        val fashionGoods = getEachCategoryGoods(categoryGoods = it.fashionGoods, topicColorList = topicColorList)
                        shopFeedData.items.add(ShopFeedListData(
                                dataType = AppConstants.ADAPTER_CONTENT,
                                categoryText = TextConstants.CATEGORY_FASHION,
                                categoryCode = TextConstants.CATEGORY_FASHION_CODE,
                                goodsData = fashionGoods
                        ))
                        // food 카테고리
                        val foodGoods = getEachCategoryGoods(categoryGoods = it.foodGoods, topicColorList = topicColorList)
                        shopFeedData.items.add(ShopFeedListData(
                                dataType = AppConstants.ADAPTER_CONTENT,
                                categoryText = TextConstants.CATEGORY_FOOD,
                                categoryCode = TextConstants.CATEGORY_FOOD_CODE,
                                goodsData = foodGoods
                        ))

                        PrintLog.d("getShopFeed success", shopFeedData.toString(), tag)
                        uiData.value = UIModel(isLoading = false, shopFeedData = shopFeedData)
                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(isLoading = false)
                        errorData?.let {
                            PrintLog.d("getShopFeed fail", errorData.message, tag)
                        }
                    })
        }
    }

    private fun getPercentValueAndColor(topicColorList: Array<String>, matchData: ResFeedData.TopicMatch?): MatchData {
        val randIndex = Random()
        var percentValue = 0
        var percentColor = Color.parseColor("#${topicColorList[randIndex.nextInt(topicColorList.size)]}")
        matchData?.let {
            percentValue = it.match
            percentColor = Color.parseColor("#${it.topicColor}")
        }

        return MatchData(percent = percentValue, color = percentColor)
    }

    private fun getEachCategoryGoods(categoryGoods: ArrayList<ResShopFeedData.Goods>?, topicColorList: Array<String>): SearchGoodsData {
        val categoryGoodsData = SearchGoodsData()
        categoryGoods?.forEach { goods ->
            val matchData = getPercentValueAndColor(topicColorList = topicColorList,
                    matchData = goods.topicMatch?.let{_->goods.topicMatch.firstOrNull()}?:let { _ ->null})
            categoryGoodsData.items.add(SearchGoodsListData(
                    dataType = AppConstants.ADAPTER_CONTENT,
                    sortType = AppConstants.SORT_MATCH,
                    goodsCd = goods.code,
                    imageURL = ApiURL.GODO_IMAGE_HEADER_URL + goods.image,
                    price = goods.price,
                    title = goods.name,
                    brand = goods.brandName,
                    showQuestionMark = goods.topicMatch == null,
                    matchingPercent = matchData.percent,
                    matchColor = matchData.color
            ))
        }
        return categoryGoodsData
    }

}

data class MatchData(val percent: Int, val color: Int)
data class LoadFeedDataEvent(val isLoading: Boolean? = null)
data class UIModel(val isLoading: Boolean? = null, val toastMessage: String? = null,
                   val allFeedData: AllFeedData? = null,
                   val followingFeedData: PostDetailData? = null, val guestMode: Boolean? = null, val noFollowing: Boolean? = null,
                   val exploreFeedData: ExploreFeedData? = null,
                   val shopFeedData: ShopFeedData? = null)