package xlab.world.xlab.view.postsUpload.goods

import android.arch.lifecycle.MutableLiveData
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
    val tag = "PostGoods"

    private val selectedUsedGoodsData = ArrayList<SelectUsedGoodsListData>()

    val loadUsedGoodsEventData = SingleLiveEvent<PostUsedGoodsEvent>()
    val uiData = MutableLiveData<UIModel>()

    fun getSelectedUsedGoodsData(): ArrayList<SelectUsedGoodsListData> = selectedUsedGoodsData

    fun setSelectedUsedGoodsData(selectedData: ArrayList<SelectUsedGoodsListData>, dataType: Int) {
        launch {
            Observable.create<ArrayList<SelectUsedGoodsListData>> {
                selectedUsedGoodsData.clear()
                selectedData.forEach { data ->
                    data.dataType = dataType
                    selectedUsedGoodsData.add(data)
                }

                it.onNext(selectedUsedGoodsData)
                it.onComplete()
            }.with(scheduler).subscribe {
                PrintLog.d("set selectedUsedGoodsData", it.toString(), tag)
                uiData.value = UIModel(selectedUsedGoodsData = it)
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
                        PrintLog.d("requestTopicUsedGoods success", it.toString(), tag)
                        PrintLog.d("selectedUsedGoodsData", selectedUsedGoodsData.toString(), tag)
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
                            PrintLog.d("requestTopicUsedGoods fail", errorData.message, tag)
                        }
                    })
        }
    }

    fun selectUsedGoods(position: Int, usedGoodsData: SelectUsedGoodsListData) {
        launch {
            Observable.create<ArrayList<SelectUsedGoodsListData>> {
                usedGoodsData.isSelect = !usedGoodsData.isSelect

                if (usedGoodsData.isSelect) { // 사용한 제품 선택 리스트 추가
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
            }.with(scheduler).subscribe {
                PrintLog.d("update selectedUsedGoodsData", selectedUsedGoodsData.toString(), tag)
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
            }.with(scheduler).subscribe {
                PrintLog.d("update selectedUsedGoodsData", selectedUsedGoodsData.toString(), tag)
                PrintLog.d("usedGoodsUpdateIndex", it.toString(), tag)
                uiData.value = UIModel(usedGoodsUpdateIndex = if (it == -1) null else it,
                        updateSelectedUsedGoodsData = selectedUsedGoodsData)
            }
        }
    }
}

data class PostUsedGoodsEvent(val status: Boolean? = null)
data class UIModel(val isLoading: Boolean? = null, val toastMessage: String? = null,
                   val selectedUsedGoodsData: ArrayList<SelectUsedGoodsListData>? = null,
                   val updateSelectedUsedGoodsData: ArrayList<SelectUsedGoodsListData>? = null,
                   val selectedUsedGoodsScrollIndex: Int? = null,
                   val usedGoodsData: SelectUsedGoodsData? = null,
                   val usedGoodsUpdateIndex: Int? = null)
