package xlab.world.xlab.view.main

import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.graphics.Color
import io.reactivex.Observable
import xlab.world.xlab.R
import xlab.world.xlab.data.adapter.*
import xlab.world.xlab.data.response.ResGoodsSearchData
import xlab.world.xlab.server.ApiURL
import xlab.world.xlab.server.provider.ApiPostProvider
import xlab.world.xlab.server.provider.ApiShopProvider
import xlab.world.xlab.utils.rx.SchedulerProvider
import xlab.world.xlab.utils.rx.with
import xlab.world.xlab.utils.support.*
import xlab.world.xlab.view.AbstractViewModel
import xlab.world.xlab.view.SingleLiveEvent
import java.net.HttpURLConnection
import java.util.*

class MainViewModel(private val apiPost: ApiPostProvider,
                    private val apiShop: ApiShopProvider,
                    private val networkCheck: NetworkCheck,
                    private val scheduler: SchedulerProvider): AbstractViewModel() {
    private val viewModelTag = "Main"

    val loadAllFeedDataEvent = SingleLiveEvent<LoadFeedDataEvent>()
    val loadExploreFeedDataEvent = SingleLiveEvent<LoadFeedDataEvent>()
    val btnActionData = SingleLiveEvent<BtnActionModel?>()
    val uiData = MutableLiveData<UIModel>()

    fun notificationBtnAction(authorization: String) {
        // login 확인
        if (SupportData.isGuest(authorization)) { // guest
            uiData.postValue(UIModel(guestMode = true))
            return
        }

        btnActionData.postValue(BtnActionModel(notification = true))
    }

    fun profileBtnAction(authorization: String) {
        // login 확인
        if (SupportData.isGuest(authorization)) { // guest
            uiData.postValue(UIModel(guestMode = true))
            return
        }

        btnActionData.postValue(BtnActionModel(profile = true))
    }

    fun uploadPostBtnAction(authorization: String, userLevel: Int) {
        // login 확인
        if (SupportData.isGuest(authorization)) { // guest
            uiData.postValue(UIModel(guestMode = true))
            return
        }

        // 유저 레벨이 admin 이면 post upload type 선택 다이얼로그 띄우기
        // 일반이면 바로 post upload 화면으로
        btnActionData.postValue(BtnActionModel(post = if (userLevel == AppConstants.ADMIN_USER_LEVEL) null else true,
                postTypeDialog = if (userLevel == AppConstants.ADMIN_USER_LEVEL) true else null))
    }

    fun loadAllFeedData(authorization: String, page: Int, topicColorList: Array<String>, loadingBar: Boolean? = true) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = networkCheck.networkErrorMsg))
            return
        }

        uiData.value = UIModel(isLoading = loadingBar)
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
                                            imageURL = feedData.goods.image,
                                            matchingPercent = matchData.percent,
                                            matchColor = matchData.color,
                                            showQuestionMark = feedData.goods.topicMatch == null,
                                            goodsCd = feedData.goods.code))
                                }
                            }
                        }

                        PrintLog.d("getAllFeed success", allFeedData.toString())
                        uiData.value = UIModel(isLoading = loadingBar?.let{_->false}, allFeedData = allFeedData)
                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(isLoading = loadingBar?.let{_->false})
                        errorData?.let {
                            PrintLog.d("getAllFeed fail", errorData.message)
                        }
                    })
        }
    }

    fun loadExploreFeedData(authorization: String, page: Int, loadingBar: Boolean? = true) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = networkCheck.networkErrorMsg))
            return
        }

        uiData.value = UIModel(isLoading = loadingBar)
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
                                            imageURL = feedData.goods!!.image,
                                            goodsCd = feedData.goods.code))
                                }
                            }
                        }

                        PrintLog.d("getExploreFeed success", exploreFeedData.toString())
                        uiData.value = UIModel(isLoading = loadingBar?.let{_->false}, exploreFeedData = exploreFeedData)
                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(isLoading = loadingBar?.let{_->false})
                        errorData?.let {
                            PrintLog.d("getExploreFeed fail", errorData.message)
                        }
                    })
        }
    }

    fun loadShopFeedData(context: Context, authorization: String, topicColorList: Array<String>, loadingBar: Boolean? = true) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = networkCheck.networkErrorMsg))
            return
        }

        uiData.value = UIModel(isLoading = loadingBar)
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
                                categoryText = context.getString(R.string.category_living),
                                categoryCode = context.getString(R.string.category_living_code),
                                goodsData = livingGoods
                        ))
                        // fashion 카테고리
                        val fashionGoods = getEachCategoryGoods(categoryGoods = it.fashionGoods, topicColorList = topicColorList)
                        shopFeedData.items.add(ShopFeedListData(
                                dataType = AppConstants.ADAPTER_CONTENT,
                                categoryText = context.getString(R.string.category_fashion),
                                categoryCode = context.getString(R.string.category_fashion_code),
                                goodsData = fashionGoods
                        ))
                        // food 카테고리
                        val foodGoods = getEachCategoryGoods(categoryGoods = it.foodGoods, topicColorList = topicColorList)
                        shopFeedData.items.add(ShopFeedListData(
                                dataType = AppConstants.ADAPTER_CONTENT,
                                categoryText = context.getString(R.string.category_food),
                                categoryCode = context.getString(R.string.category_food_code),
                                goodsData = foodGoods
                        ))

                        PrintLog.d("getShopFeed success", shopFeedData.toString())
                        uiData.value = UIModel(isLoading = loadingBar?.let{_->false}, shopFeedData = shopFeedData)
                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(isLoading = loadingBar?.let{_->false})
                        errorData?.let {
                            PrintLog.d("getShopFeed fail", errorData.message)
                        }
                    })
        }
    }

    private fun getPercentValueAndColor(topicColorList: Array<String>, matchData: ResGoodsSearchData.TopicMatch?): MatchData {
        val randIndex = Random()
        var percentValue = 0
        var percentColor = Color.parseColor("#${topicColorList[randIndex.nextInt(topicColorList.size)]}")
        matchData?.let {
            percentValue = it.match
            percentColor = Color.parseColor("#${it.topicColor}")
        }

        return MatchData(percent = percentValue, color = percentColor)
    }

    private fun getEachCategoryGoods(categoryGoods: ArrayList<ResGoodsSearchData.Goods>?, topicColorList: Array<String>): SearchGoodsData {
        val categoryGoodsData = SearchGoodsData()
        categoryGoods?.forEach { goods ->
            val matchData = getPercentValueAndColor(topicColorList = topicColorList,
                    matchData = goods.topicMatch?.let{_->goods.topicMatch.firstOrNull()}?:let { _ ->null})
            categoryGoodsData.items.add(SearchGoodsListData(
                    dataType = AppConstants.ADAPTER_CONTENT,
                    sortType = AppConstants.SORT_MATCH,
                    goodsCd = goods.code,
                    imageURL = goods.image,
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
data class BtnActionModel(val notification: Boolean? = null, val profile: Boolean? = null,
                          val post: Boolean? = null, val postTypeDialog: Boolean? = null)
data class UIModel(val isLoading: Boolean? = null, val toastMessage: String? = null,
                   val allFeedData: AllFeedData? = null,
                   val guestMode: Boolean? = null,
                   val exploreFeedData: ExploreFeedData? = null,
                   val shopFeedData: ShopFeedData? = null)