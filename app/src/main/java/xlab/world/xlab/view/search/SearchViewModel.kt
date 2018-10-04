package xlab.world.xlab.view.search

import android.arch.lifecycle.MutableLiveData
import android.graphics.Color
import android.view.View
import io.reactivex.Observable
import xlab.world.xlab.data.adapter.*
import xlab.world.xlab.data.request.ReqGoodsSearchData
import xlab.world.xlab.data.response.ResGoodsSearchData
import xlab.world.xlab.data.response.ResUserDefaultData
import xlab.world.xlab.server.ApiURL
import xlab.world.xlab.server.provider.ApiPostProvider
import xlab.world.xlab.server.provider.ApiShopProvider
import xlab.world.xlab.server.provider.ApiUserProvider
import xlab.world.xlab.utils.rx.SchedulerProvider
import xlab.world.xlab.utils.rx.with
import xlab.world.xlab.utils.support.*
import xlab.world.xlab.utils.view.hashTag.EditTextTagHelper
import xlab.world.xlab.view.AbstractViewModel
import xlab.world.xlab.view.SingleLiveEvent
import java.util.*

class SearchViewModel(private val apiShop: ApiShopProvider,
                      private val apiPost: ApiPostProvider,
                      private val apiUser: ApiUserProvider,
                      private val petInfo: PetInfo,
                      private val networkCheck: NetworkCheck,
                      private val scheduler: SchedulerProvider): AbstractViewModel() {
    val tag = "Search"

    private var searchSortType = AppConstants.SORT_MATCH

    val changeSearchSortTypeEventData = SingleLiveEvent<SearchEvent>()
    val searchPostsEventData = SingleLiveEvent<SearchEvent>()
    val searchUserEventData = SingleLiveEvent<SearchEvent>()
    val searchGoodsEventData = SingleLiveEvent<SearchEvent>()
    val uiData = MutableLiveData<UIModel>()

    fun changeSearchSortType(goodsData: SearchGoodsListData, sortType: Int) {
        launch {
            Observable.create<Int> {
                searchSortType = sortType
                goodsData.sortType = sortType

                it.onNext(0)
                it.onComplete()
            }.with(scheduler).subscribe {
                uiData.value = UIModel(searchGoodsUpdatePosition = it)
                changeSearchSortTypeEventData.value = SearchEvent(status = true)
            }
        }
    }

    fun requestCombinedSearchTotal(authorization: String, searchText: String) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = TextConstants.CHECK_NETWORK_CONNECT))
            return
        }

        launch {
            // post total 가져오기
            apiPost.requestSearchPosts(scheduler = scheduler, searchText = searchText, page = 1,
                    responseData = {
                        uiData.value = UIModel(searchPostsTotal = it.total)
                    },
                    errorData = { errorData ->
                        errorData?.let {
                            PrintLog.d("searchUser fail", errorData.message, tag)
                        }
                    })
            // user total 가져오기
            apiUser.requestSearchUser(scheduler = scheduler, authorization = authorization, searchText = searchText, page = 1,
                    responseData = {
                        uiData.value = UIModel(searchUserTotal = it.total)
                    },
                    errorData = {
                        errorData ->
                        errorData?.let {
                            PrintLog.d("searchPosts fail", errorData.message, tag)
                        }
                    })
            // goods total 가져오기
            val reqSearchData = ArrayList<ReqGoodsSearchData>()
            reqSearchData.add(ReqGoodsSearchData(text = searchText, code = ""))
            apiShop.requestSearchGoods(scheduler = scheduler, authorization = authorization, reqGoodsSearchData = reqSearchData,
                    page = 1, sortType = searchSortType,
                    responseData = {
                        uiData.value = UIModel(searchGoodsTotal = it.goodsTotal)
                    },
                    errorData = { errorData ->
                        errorData?.let {
                            PrintLog.d("searchGoods fail", errorData.message, tag)
                        }
                    })
        }
    }

    fun searchPosts(searchText: String, page: Int, loadingBar: Boolean? = true) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = TextConstants.CHECK_NETWORK_CONNECT))
            return
        }

        uiData.value = UIModel(isLoading = loadingBar)
        searchPostsEventData.value = SearchEvent(status = true)
        launch {
            apiPost.requestSearchPosts(scheduler = scheduler, searchText = searchText, page = page,
                    responseData = {
                        PrintLog.d("searchPosts success", it.toString(), tag)
                        val postsData = PostThumbnailData(total = it.total, nextPage = page + 1)
                        it.searchPosts?.forEach { post ->
                            postsData.items.add(PostThumbnailListData(
                                    dataType = AppConstants.ADAPTER_CONTENT,
                                    postId = post.id,
                                    postType = post.postType,
                                    imageURL = post.postFile.firstOrNull(),
                                    youTubeVideoID = post.youTubeVideoID
                            ))
                        }
                        if (page == 1)
                            uiData.value = UIModel(scrollUpBtnVisibility = if(postsData.items.isEmpty()) View.GONE else View.VISIBLE)
                        uiData.value = UIModel(isLoading = loadingBar?.let{_->false}, searchPostsData = postsData)
                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(isLoading = loadingBar?.let{_->false})
                        errorData?.let {
                            PrintLog.d("searchPosts fail", errorData.message, tag)
                        }
                    })
        }
    }

    fun searchUsers(authorization: String, searchText: String, page: Int, loadingBar: Boolean? = true) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = TextConstants.CHECK_NETWORK_CONNECT))
            return
        }

        uiData.value = UIModel(isLoading = loadingBar)
        searchUserEventData.value = SearchEvent(status = true)
        launch {
            PrintLog.d("authorization", authorization, tag)
            apiUser.requestSearchUser(authorization = authorization, scheduler = scheduler, searchText = searchText, page = page,
                    responseData = {
                        PrintLog.d("searchUser success", it.toString(), tag)
                        val userData = UserDefaultData(total = it.total, nextPage = page + 1)
                        it.userData?.forEach { user ->
                            val topicBreed = petInfo.getTopicBreed(user.petData)
                            userData.items.add(UserDefaultListData(userId = user.id,
                                    profileImage = user.profileImg,
                                    nickName = user.nickName,
                                    topic = topicBreed,
                                    isFollowing = user.isFollowing))
                        }
                        if (page == 1)
                            uiData.value = UIModel(scrollUpBtnVisibility = if(userData.items.isEmpty()) View.GONE else View.VISIBLE)
                        uiData.value = UIModel(isLoading = loadingBar?.let{_->false}, searchUserData = userData)
                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(isLoading = loadingBar?.let{_->false})
                        errorData?.let {
                            PrintLog.d("searchUser fail", errorData.message, tag)
                        }
                    })
        }
    }

    fun searchGoods(authorization: String, searchData: ArrayList<EditTextTagHelper.SearchData>,
                    page: Int, topicColorList: Array<String>, loadingBar: Boolean? = true) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = TextConstants.CHECK_NETWORK_CONNECT))
            return
        }

        uiData.value = UIModel(isLoading = loadingBar)
        searchGoodsEventData.value = SearchEvent(status = true)
        launch {
            val reqSearchData = ArrayList<ReqGoodsSearchData>()
            searchData.forEach {
                reqSearchData.add(ReqGoodsSearchData(text = it.text, code = it.code))
            }
            apiShop.requestSearchGoods(scheduler = scheduler, authorization = authorization, reqGoodsSearchData = reqSearchData,
                    page = page, sortType = searchSortType,
                    responseData = {
                        PrintLog.d("searchGoods success", it.toString(), tag)
                        // keyword data
                        val keywordData = GoodsKeywordData(total = it.keywordTotal)
                        it.keywordList?.let { keywordList ->
                            keywordList.forEach { keyword ->
                                keywordData.items.add(GoodsKeywordListData(
                                        dataType = AppConstants.ADAPTER_CONTENT,
                                        keywordText = keyword.keyword,
                                        keywordCode = keyword.code))
                            }
                        }
                        // goods data
                        val goodsData = SearchGoodsData(total = it.goodsTotal, nextPage = page + 1)
                        it.goodsList?.let { goodsList ->
                            goodsList.forEach { goods ->
                                val matchData = getPercentValueAndColor(topicColorList = topicColorList,
                                        matchData = goods.topicMatch?.let{_->goods.topicMatch.firstOrNull()}?:let{_->null})
                                goodsData.items.add(SearchGoodsListData(
                                        dataType = AppConstants.ADAPTER_CONTENT,
                                        sortType = searchSortType,
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
                        }

                        if (page == 1)
                            goodsData.items.add(0, SearchGoodsListData(
                                    dataType = AppConstants.ADAPTER_HEADER,
                                    sortType = searchSortType)
                            )

                        uiData.value = UIModel(isLoading = loadingBar?.let{_->false},
                                keywordData = keywordData, searchGoodsData = goodsData)
                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(isLoading = loadingBar?.let{_->false})
                        errorData?.let {
                            PrintLog.d("searchGoods fail", errorData.message, tag)
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
}

data class MatchData(val percent: Int, val color: Int)
data class SearchEvent(val status: Boolean? = null)
data class UIModel(val isLoading: Boolean? = null, val toastMessage: String? = null,
                   val keywordData: GoodsKeywordData? = null, val searchGoodsData: SearchGoodsData? = null,
                   val searchGoodsUpdatePosition: Int? = null, val scrollUpBtnVisibility: Int? = null,
                   val searchPostsData: PostThumbnailData? = null,
                   val searchUserData: UserDefaultData? = null,
                   val searchPostsTotal: Int? = null, val searchUserTotal: Int? = null, val searchGoodsTotal: Int? = null)
