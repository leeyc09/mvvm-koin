package xlab.world.xlab.view.postsUpload.goods

import android.arch.lifecycle.MutableLiveData
import android.view.View
import io.reactivex.Observable
import xlab.world.xlab.data.adapter.SelectUsedGoodsData
import xlab.world.xlab.data.adapter.SelectUsedGoodsListData
import xlab.world.xlab.server.provider.ApiUserActivityProvider
import xlab.world.xlab.utils.rx.SchedulerProvider
import xlab.world.xlab.utils.rx.with
import xlab.world.xlab.utils.support.AppConstants
import xlab.world.xlab.utils.support.NetworkCheck
import xlab.world.xlab.utils.support.PrintLog
import xlab.world.xlab.view.AbstractViewModel
import xlab.world.xlab.view.SingleLiveEvent

class PostUsedGoodsViewModel(private val apiUserActivity: ApiUserActivityProvider,
                             private val networkCheck: NetworkCheck,
                             private val scheduler: SchedulerProvider): AbstractViewModel() {
    private val viewModelTag = "PostGoods"

    private val selectedUsedGoodsData = ArrayList<SelectUsedGoodsListData>()

    private var usedGoodsData: SelectUsedGoodsData = SelectUsedGoodsData()
    private var selectUsedGoodsData: SelectUsedGoodsData = SelectUsedGoodsData()

    val loadUsedGoodsEventData = SingleLiveEvent<PostUsedGoodsEvent>()
    val uiData = MutableLiveData<UIModel>()

    fun getSelectedUsedGoodsData(): ArrayList<SelectUsedGoodsListData> = selectedUsedGoodsData

    // 선택한 제품 데이터 세팅
    fun setSelectedUsedGoodsData(selectedData: ArrayList<SelectUsedGoodsListData>, dataType: Int) {
        launch {
            Observable.create<SelectUsedGoodsData> {
                val newSelectUsedGoodsData = SelectUsedGoodsData()
                selectedData.forEach { data ->
                    data.dataType = dataType
                    newSelectUsedGoodsData.items.add(data.copy())
                }
                this.selectUsedGoodsData.updateData(selectUsedGoodsData = newSelectUsedGoodsData)

//                selectedUsedGoodsData.clear()
//                selectedData.forEach { data ->
//                    data.dataType = dataType
//                    selectedUsedGoodsData.add(data)
//                }

                it.onNext(this.selectUsedGoodsData)
                it.onComplete()
            }.with(scheduler = scheduler).subscribe {
                PrintLog.d("set selectedUsedGoodsData", it.toString(), viewModelTag)
                uiData.value = UIModel(selectedLayoutVisibility = if (it.items.isEmpty()) View.GONE else View.VISIBLE,
                        selectedUsedGoodsData = it)
            }
        }
    }

    fun loadUsedGoodsData(userId: String, goodsType: Int, page: Int) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = networkCheck.networkErrorMsg))
            return
        }

        uiData.value = UIModel(isLoading = true)
        loadUsedGoodsEventData.postValue(PostUsedGoodsEvent(status = true))
        launch {
            apiUserActivity.requestTopicUsedGoods(scheduler = scheduler, userId = userId, goodsType = goodsType, page = page,
                    responseData = {
                        PrintLog.d("requestTopicUsedGoods success", it.toString())
                        PrintLog.d("selectedUsedGoodsData", selectedUsedGoodsData.toString())
                        val usedGoodsData = SelectUsedGoodsData(total = it.total, nextPage = page + 1)
                        it.goods?.forEach { goods ->
                            var isSelect = false
                            selectedUsedGoodsData.forEach selectedUsedGoodsData@ { selectedGoods ->
                                if (goods.code == selectedGoods.goodsCode) {
                                    isSelect = true
                                    return@selectedUsedGoodsData
                                }
                            }
                            usedGoodsData.items.add(SelectUsedGoodsListData(
                                    dataType = AppConstants.SELECTED_GOODS_WITH_INFO,
                                    goodsCode = goods.code,
                                    goodsName = goods.name,
                                    goodsBrand = goods.brand,
                                    imageURL = goods.image,
                                    isSelect = isSelect
                            ))
                        }
                        uiData.value = UIModel(isLoading = false, usedGoodsData = usedGoodsData)
                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(isLoading = false)
                        errorData?.let {
                            PrintLog.d("requestTopicUsedGoods fail", errorData.message)
                        }
                    })
        }
    }

    // 사용한 제품 선택 & 해제
    fun selectUsedGoods(selectIndex: Int) {
        launch {
            Observable.create<ArrayList<SelectUsedGoodsListData>> {
                val item = usedGoodsData.items[selectIndex]
                // 제품 선택 -> 해제 / 해제 -> 선택
                item.isSelect = !item.isSelect

                if (item.isSelect) { // 사용한 제품 선택 리스트 추가
                    val copyData = usedGoodsData.copy()
                    copyData.dataType = AppConstants.SELECTED_GOODS_ONLY_THUMB
                    selectedUsedGoodsData.add(copyData)
                } else { // 사용한 제품 선택 리스트 삭제
                    var removeItem: SelectUsedGoodsListData? = null
                    selectedUsedGoodsData.forEach selectedUsedGoodsData@ { selectedGoods ->
                        if (usedGoodsData.goodsCode == selectedGoods.goodsCode) {
                            removeItem = selectedGoods
                            return@selectedUsedGoodsData
                        }
                    }
                    selectedUsedGoodsData.remove(removeItem)
                }

                it.onNext(selectedUsedGoodsData)
                it.onComplete()
            }.with(scheduler = scheduler).subscribe {
                PrintLog.d("update selectedUsedGoodsData", selectedUsedGoodsData.toString())
                uiData.value = UIModel(usedGoodsUpdateIndex = position,
                        updateSelectedUsedGoodsData = selectedUsedGoodsData,
                        selectedUsedGoodsScrollIndex = if (usedGoodsData.isSelect) selectedUsedGoodsData.size - 1 else null)
            }
        }
    }

    fun deleteSelectedUsedGoods(selectedGoodsPosition: Int, selectedUsedGoods: HashMap<Int, SelectUsedGoodsListData>?) {
        launch {
            Observable.create<Int> {
                val removeItem = selectedUsedGoodsData[selectedGoodsPosition]
                var updateUsedGoodsIndex = -1

                selectedUsedGoods?.forEach { (key, value) ->
                    if (value.goodsCode == removeItem.goodsCode) {
                        value.isSelect = false
                        updateUsedGoodsIndex = key
                    }
                }
                selectedUsedGoodsData.remove(removeItem)

                it.onNext(updateUsedGoodsIndex)
                it.onComplete()
            }.with(scheduler = scheduler).subscribe {
                PrintLog.d("update selectedUsedGoodsData", selectedUsedGoodsData.toString())
                PrintLog.d("usedGoodsUpdateIndex", it.toString())
                uiData.value = UIModel(usedGoodsUpdateIndex = if (it == -1) null else it,
                        updateSelectedUsedGoodsData = selectedUsedGoodsData)
            }
        }
    }
}

data class PostUsedGoodsEvent(val status: Boolean? = null)
data class UIModel(val isLoading: Boolean? = null, val toastMessage: String? = null,
                   val selectedLayoutVisibility: Int? = null,
                   val selectedUsedGoodsData: SelectUsedGoodsData? = null,
                   val updateSelectedUsedGoodsData: ArrayList<SelectUsedGoodsListData>? = null,
                   val selectedUsedGoodsScrollIndex: Int? = null,
                   val usedGoodsData: SelectUsedGoodsData? = null,
                   val usedGoodsUpdateIndex: Int? = null)
