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

    private var usedGoodsData: SelectUsedGoodsData = SelectUsedGoodsData()
    private var selectUsedGoodsData: SelectUsedGoodsData = SelectUsedGoodsData()

    val loadUsedGoodsData = SingleLiveEvent<Boolean?>()
    val finishSelectData = SingleLiveEvent<FinishSelectModel>()
    val uiData = MutableLiveData<UIModel>()

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

                it.onNext(this.selectUsedGoodsData)
                it.onComplete()
            }.with(scheduler = scheduler).subscribe {
                PrintLog.d("set selectedUsedGoodsData", it.toString(), viewModelTag)
                uiData.value = UIModel(selectedLayoutVisibility = if (it.items.isEmpty()) View.GONE else View.VISIBLE,
                        selectedUsedGoodsData = it,
                        selectedUsedGoodsDataCnt = selectUsedGoodsData.items.size.toString())
            }
        }
    }

    // 사용한 제품 불러오기
    fun loadUsedGoodsData(userId: String, goodsType: Int, page: Int) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = networkCheck.networkErrorMsg))
            return
        }

        uiData.value = UIModel(isLoading = true)
        loadUsedGoodsData.value = true
        launch {
            apiUserActivity.requestTopicUsedGoods(scheduler = scheduler, userId = userId, goodsType = goodsType, page = page,
                    responseData = {
                        PrintLog.d("requestTopicUsedGoods success", it.toString(), viewModelTag)
                        PrintLog.d("selectUsedGoodsData", selectUsedGoodsData.toString(), viewModelTag)
                        val newUsedGoodsData = SelectUsedGoodsData(total = it.total, nextPage = page + 1)
                        it.goods?.forEach { goods ->
                            var isSelect = false
                            // 선택한 상품은 체크 상태로
                            run selectedUsedGoodsData@{
                                selectUsedGoodsData.items.forEach { selectedGoods ->
                                    if (goods.code == selectedGoods.goodsCode) {
                                        isSelect = true
                                        return@selectedUsedGoodsData
                                    }
                                }
                            }
                            newUsedGoodsData.items.add(SelectUsedGoodsListData(
                                    dataType = AppConstants.SELECTED_GOODS_WITH_INFO,
                                    goodsCode = goods.code,
                                    goodsName = goods.name,
                                    goodsBrand = goods.brand,
                                    imageURL = goods.image,
                                    isSelect = isSelect
                            ))
                        }
                        if (page == 1) { // 요청한 page => 첫페이지
                            this.usedGoodsData.updateData(selectUsedGoodsData = newUsedGoodsData)
                            uiData.value = UIModel(isLoading = false,
                                    usedGoodsData = this.usedGoodsData,
                                    emptyGoodsVisibility = if (this.usedGoodsData.items.isEmpty()) View.VISIBLE else View.GONE)
                        } else {
                            this.usedGoodsData.addData(selectUsedGoodsData = newUsedGoodsData)
                            uiData.value = UIModel(isLoading = false, usedGoodsUpdate = true)
                        }
                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(isLoading = false)
                        errorData?.let {
                            PrintLog.e("requestTopicUsedGoods fail", errorData.message, viewModelTag)
                        }
                    })
        }
    }

    // 사용한 제품 선택 & 해제
    fun selectUsedGoods(selectIndex: Int) {
        launch {
            Observable.create<SelectUsedGoodsData> {
                val item = usedGoodsData.items[selectIndex]
                // 제품 선택 -> 해제 / 해제 -> 선택
                item.isSelect = !item.isSelect

                if (item.isSelect) { // 사용한 제품 선택 리스트 추가
                    item.dataType = AppConstants.SELECTED_GOODS_ONLY_THUMB
                    selectUsedGoodsData.addData(selectUsedGoodsListData = item)
                } else { // 사용한 제품 선택 리스트 삭제
                    var removeIndex: Int = -1
                    run selectedUsedGoodsData@ {
                        selectUsedGoodsData.items.forEachIndexed { index, selectedGoods ->
                            if (item.goodsCode == selectedGoods.goodsCode) {
                                removeIndex = index
                                return@selectedUsedGoodsData
                            }
                        }
                    }
                    selectUsedGoodsData.removeData(index = removeIndex)
                }

                it.onNext(selectUsedGoodsData)
                it.onComplete()
            }.with(scheduler = scheduler).subscribe {
                PrintLog.d("selectedUsedGoodsData", it.toString(), viewModelTag)
                uiData.value = UIModel(usedGoodsUpdateIndex = selectIndex,
                        selectedLayoutVisibility = if (it.items.isEmpty()) View.GONE else View.VISIBLE,
                        selectedUsedGoodsDataUpdate = true,
                        selectedUsedGoodsScrollIndex =
                        if (usedGoodsData.items[selectIndex].isSelect) selectUsedGoodsData.items.size - 1 else null)
            }
        }
    }

    // 선택 제품 삭제
    fun deleteSelectedUsedGoods(selectIndex: Int) {
        launch {
            Observable.create<Int> {
                val removeItem = selectUsedGoodsData.items[selectIndex]

                var updateUsedGoodsIndex = -1
                usedGoodsData.items.forEachIndexed usedGoodsData@ { index, goods ->
                    // 사용 제품 리스트에서 선택한 제품과 같은 제품 선택 해제
                    if (removeItem.goodsCode == goods.goodsCode) {
                        goods.isSelect = false
                        updateUsedGoodsIndex = index
                    }
                }
                selectUsedGoodsData.removeData(index = selectIndex)

                it.onNext(updateUsedGoodsIndex)
                it.onComplete()
            }.with(scheduler = scheduler).subscribe {
                PrintLog.d("selectedUsedGoodsData", selectUsedGoodsData.toString(), viewModelTag)
                PrintLog.d("updateUsedGoodsIndex", it.toString(), viewModelTag)
                uiData.value = UIModel(usedGoodsUpdateIndex = if (it == -1) null else it,
                        selectedLayoutVisibility = if (selectUsedGoodsData.items.isEmpty()) View.GONE else View.VISIBLE,
                        selectedUsedGoodsDataUpdate = true,
                        selectedUsedGoodsDataCnt = selectUsedGoodsData.items.size.toString())
            }
        }
    }

    // 사용한 제품 선택 완료
    fun finishSelectUsedGoods(dataNum: Int) {
        finishSelectData.postValue(FinishSelectModel(
                selectData = if (dataNum == 1) selectUsedGoodsData.items else null,
                selectData2 = if (dataNum == 2) selectUsedGoodsData.items else null))
    }
}

data class FinishSelectModel(val selectData: ArrayList<SelectUsedGoodsListData>? = null,
                             val selectData2: ArrayList<SelectUsedGoodsListData>? = null)
data class UIModel(val isLoading: Boolean? = null, val toastMessage: String? = null,
                   val selectedLayoutVisibility: Int? = null,
                   val selectedUsedGoodsData: SelectUsedGoodsData? = null,
                   val selectedUsedGoodsDataUpdate: Boolean? = null,
                   val selectedUsedGoodsDataCnt: String? = null,
                   val updateSelectedUsedGoodsData: ArrayList<SelectUsedGoodsListData>? = null,
                   val selectedUsedGoodsScrollIndex: Int? = null,
                   val emptyGoodsVisibility: Int? = null,
                   val usedGoodsData: SelectUsedGoodsData? = null,
                   val usedGoodsUpdate: Boolean? = null,
                   val usedGoodsUpdateIndex: Int? = null)
