package xlab.world.xlab.view.search

import android.app.Activity
import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.graphics.Color
import android.view.View
import io.reactivex.Observable
import xlab.world.xlab.R
import xlab.world.xlab.data.adapter.*
import xlab.world.xlab.data.request.ReqGoodsSearchData
import xlab.world.xlab.data.response.ResGoodsSearchData
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
    private val viewModelTag = "Search"

    private var resultCode = Activity.RESULT_CANCELED

    private var searchSortType = AppConstants.SORT_MATCH
    private var isShowGoodsKeyword = true

    private var searchGoodsData: SearchGoodsData = SearchGoodsData()
    private var goodsKeywordData: GoodsKeywordData = GoodsKeywordData()
    private var searchPostsData: PostThumbnailData = PostThumbnailData()

    val changeSearchSortTypeData = SingleLiveEvent<Boolean?>()
    val searchPostsEventData = SingleLiveEvent<SearchEvent>()
    val searchUserEventData = SingleLiveEvent<SearchEvent>()
    val searchGoodsEventData = SingleLiveEvent<Boolean?>()
    val uiData = MutableLiveData<UIModel>()

    fun setResultCode(resultCode: Int) {
        this.resultCode = SupportData.setResultCode(oldResultCode = this.resultCode, newResultCode = resultCode)
    }

    fun backBtnAction() {
        uiData.postValue(UIModel(resultCode = resultCode))
    }

    fun changeSearchSortType(sortType: Int) {
        // 검색 정렬 변경
        launch {
            Observable.create<Int> {
                searchSortType = sortType
                this.searchGoodsData.items[0].sortType = sortType

                it.onNext(0)
                it.onComplete()
            }.with(scheduler = scheduler).subscribe {
                uiData.value = UIModel(searchGoodsUpdateIndex = it)
                changeSearchSortTypeData.value = true
            }
        }
    }

    fun requestCombinedSearchTotal(authorization: String, searchText: String) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = networkCheck.networkErrorMsg))
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
                            PrintLog.d("searchUser fail", errorData.message)
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
                            PrintLog.d("searchPosts fail", errorData.message)
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
                            PrintLog.d("searchGoods fail", errorData.message)
                        }
                    })
        }
    }

    fun searchPosts(searchText: String, page: Int, loadingBar: Boolean? = true) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = networkCheck.networkErrorMsg))
            return
        }

        uiData.value = UIModel(isLoading = loadingBar)
        searchPostsEventData.value = SearchEvent(status = true)
        launch {
            apiPost.requestSearchPosts(scheduler = scheduler, searchText = searchText, page = page,
                    responseData = {
                        PrintLog.d("searchPosts success", it.toString(), viewModelTag)
                        val newSearchPostsData = PostThumbnailData(total = it.total, nextPage = page + 1)
                        it.postsData?.forEach { post ->
                            newSearchPostsData.items.add(PostThumbnailListData(
                                    dataType = AppConstants.ADAPTER_CONTENT,
                                    postId = post.id,
                                    postType = post.postType,
                                    imageURL = post.postFile.firstOrNull(),
                                    youTubeVideoID = post.youTubeVideoID
                            ))
                        }

                        if (page == 1) {
                            this.searchPostsData.updateData(postThumbnailData = newSearchPostsData)
                            uiData.value = UIModel(isLoading = loadingBar?.let{_->false},
                                    searchPostsData = this.searchPostsData,
                                    searchPostsTotal = this.searchPostsData.total,
                                    noSearchDataVisibility = if (this.searchPostsData.items.isEmpty()) View.VISIBLE else View.GONE)
                        } else {
                            this.searchPostsData.addData(postThumbnailData = newSearchPostsData)
                            uiData.value = UIModel(isLoading = loadingBar?.let{_->false},
                                    searchPostsDataUpdate = true)
                        }
                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(isLoading = loadingBar?.let{_->false})
                        errorData?.let {
                            PrintLog.e("searchPosts fail", errorData.message, viewModelTag)
                        }
                    })
        }
    }

    fun searchGoods(context: Context, authorization: String, searchData: ArrayList<EditTextTagHelper.SearchData>,
                    page: Int, withHeader: Boolean = true, loadingBar: Boolean? = true) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = networkCheck.networkErrorMsg))
            return
        }

        uiData.value = UIModel(isLoading = loadingBar)
//        this.searchGoodsData.isLoading = true
        searchGoodsEventData.value = true
        launch {
            // 상품 검색 데이터 추가
            val reqSearchData = ArrayList<ReqGoodsSearchData>()
            searchData.forEach {
                reqSearchData.add(ReqGoodsSearchData(text = it.text, code = it.code))
            }
            apiShop.requestSearchGoods(scheduler = scheduler, authorization = authorization, reqGoodsSearchData = reqSearchData,
                    page = page, sortType = searchSortType,
                    responseData = {
                        PrintLog.d("searchGoods success", it.toString(), viewModelTag)
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
                                val topicColorList = context.resources.getStringArray(R.array.topicColorStringList)
                                val matchData = getPercentValueAndColor(topicColorList = topicColorList,
                                        matchData = goods.topicMatch?.let{_->goods.topicMatch.firstOrNull()}?:let{_->null})
                                goodsData.items.add(SearchGoodsListData(
                                        dataType = AppConstants.ADAPTER_CONTENT,
                                        sortType = searchSortType,
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
                        }

                        if (page == 1) {
                            if (withHeader) {
                                goodsData.items.add(0, SearchGoodsListData(
                                        dataType = AppConstants.ADAPTER_HEADER,
                                        sortType = searchSortType))
                            }
                            this.searchGoodsData.updateData(searchGoodsData = goodsData)
                            this.goodsKeywordData.updateData(goodsKeywordData = keywordData)

                            isShowGoodsKeyword = this.goodsKeywordData.items.isNotEmpty()
                            uiData.value = UIModel(isLoading = loadingBar?.let{_->false},
                                    keywordData = this.goodsKeywordData,
                                    keywordVisibility = if (isShowGoodsKeyword) View.VISIBLE else View.GONE,
                                    keywordArrowRotation = if (isShowGoodsKeyword) 180f else 0f,
                                    searchGoodsData = this.searchGoodsData,
                                    noSearchDataVisibility = if (this.searchGoodsData.items.isEmpty()) View.VISIBLE else View.GONE)
                        } else {
                            this.searchGoodsData.addData(searchGoodsData = goodsData)
                            uiData.value = UIModel(isLoading = loadingBar?.let{_->false},
                                    searchGoodsDataUpdate = true)
                        }
                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(isLoading = loadingBar?.let{_->false})
                        errorData?.let {
                            PrintLog.e("searchGoods fail", errorData.message, viewModelTag)
                        }
                    })
        }
    }

    // 해당 상품 % 와 인기율 설정
    private fun getPercentValueAndColor(topicColorList: Array<String>, matchData: ResGoodsSearchData.TopicMatch?): MatchData {
        val randIndex = Random()
        var percentValue = 0
        var percentColor = Color.parseColor("#${topicColorList[randIndex.nextInt(topicColorList.size)]}")
        // match 데이터 없을 경우 -> 기본값
        matchData?.let {
            percentValue = it.match
            percentColor = Color.parseColor("#${it.topicColor}")
        }

        return MatchData(percent = percentValue, color = percentColor)
    }

    fun keywordVisibilityChange() {
        launch {
            Observable.create<ArrayList<Any>> {
                // 추천 키워드 숨긴상태 -> 보이기
                // 보이는 상태 -> 숨기기
                // [0] -> 키워드 레이아웃 보이기 & 숨기기
                // [1] -> 키워드 화살표
                isShowGoodsKeyword = !isShowGoodsKeyword
                it.onNext(arrayListOf(
                        if(isShowGoodsKeyword) View.VISIBLE else View.GONE,
                        if(isShowGoodsKeyword) 180f else 0f
                ))
                it.onComplete()
            }.with(scheduler = scheduler).subscribe {
                uiData.value = UIModel(keywordVisibility = it[0] as Int,
                        keywordArrowRotation = it[1] as Float)
            }
        }
    }
}

data class MatchData(val percent: Int, val color: Int)
data class SearchEvent(val status: Boolean? = null)
data class UIModel(val isLoading: Boolean? = null, val toastMessage: String? = null, val resultCode: Int? = null,
                   val keywordData: GoodsKeywordData? = null, val keywordVisibility: Int? = null, val keywordArrowRotation: Float? = null,
                   val searchGoodsData: SearchGoodsData? = null, val searchGoodsDataUpdate: Boolean? = null, val searchGoodsUpdateIndex: Int? = null,
                   val searchPostsData: PostThumbnailData? = null, val searchPostsDataUpdate: Boolean? = null,
                   val searchUserData: UserDefaultData? = null,
                   val searchPostsTotal: Int? = null, val searchUserTotal: Int? = null, val searchGoodsTotal: Int? = null,
                   val noSearchDataVisibility: Int? = null)
