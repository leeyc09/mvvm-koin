package xlab.world.xlab.view.topicDetail

import android.arch.lifecycle.MutableLiveData
import android.graphics.Color
import android.view.View
import io.reactivex.Observable
import xlab.world.xlab.data.adapter.TopicUsedGoodsData
import xlab.world.xlab.data.adapter.TopicUsedGoodsListData
import xlab.world.xlab.data.response.ResUserPetData
import xlab.world.xlab.server.provider.ApiPetProvider
import xlab.world.xlab.utils.rx.SchedulerProvider
import xlab.world.xlab.utils.rx.with
import xlab.world.xlab.utils.support.NetworkCheck
import xlab.world.xlab.utils.support.PetInfo
import xlab.world.xlab.utils.support.PrintLog
import xlab.world.xlab.utils.support.TextConstants
import xlab.world.xlab.view.AbstractViewModel
import xlab.world.xlab.view.SingleLiveEvent

class TopicPetDetailViewModel(private val apiPet: ApiPetProvider,
                              private val petInfo: PetInfo,
                              private val networkCheck: NetworkCheck,
                              private val scheduler: SchedulerProvider): AbstractViewModel() {
    val tag = "TopicPetDetail"

    private var petData: ResUserPetData? = null
    private var petUsedGoodsData: TopicUsedGoodsData? = null

    val loadPetDataEvent = SingleLiveEvent<LoadPetDataEvent>()
    val uiData = MutableLiveData<UIModel>()

    fun changePetData(petData: ResUserPetData?, isMine: Boolean) {
        petData?.let { _ ->
            this.petData = petData.copy()
            launch {
                Observable.create<PetDetailData> {
                    val breed =
                            if (petData.type == petInfo.dogCode) petInfo.dogBreedInfo[petData.breed.toInt()].nameKor
                            else petInfo.catBreedInfo[petData.breed.toInt()].nameKor
                    val topicColor =
                            if (isMine) Color.parseColor("#${petData.topicColor}")
                            else Color.parseColor("#303030")

                    val type = petInfo.petType[petData.type]!!
                    val name = petData.name
                    val gender = petData.gender == petInfo.femaleCode // true - 암컷 false - 수컷
                    val age = when (0) {
                        petData.ageYear -> String.format("%d개월", petData.ageMonth)
                        petData.ageMonth -> String.format("%d년", petData.ageYear)
                        else -> String.format("%d년 %d개월", petData.ageYear, petData.ageMonth)
                    }
                    val weight = (String.format("%.1f", petData.weight))

                    it.onNext(PetDetailData(breed = breed, topicColor = topicColor, type = type,
                            name = name, gender = gender, age = age, weight = weight))
                    it.onComplete()
                }.with(scheduler).subscribe {
                    PrintLog.d("changePetData", it.toString(), tag)
                    uiData.value = UIModel(petDetailData = it)
                }
            }
        } ?:let {
            this.petData = null
        }
    }

    fun changePetUsedGoodsData(petUsedGoods: TopicUsedGoodsData?) {
        petUsedGoods?.let {
            this.petUsedGoodsData = it
            uiData.value = UIModel(petUsedGoods = it)
        } ?: let {
            this.petUsedGoodsData = null
        }
    }

    fun setButtonVisible(userId: String, loginUserId: String) {
        launch {
            Observable.create<Int> {
                it.onNext(if (userId == loginUserId) View.VISIBLE else View.GONE)
                it.onComplete()
            }.with(scheduler).subscribe {
                PrintLog.d("setButtonVisible", it.toString(), tag)
                uiData.value = UIModel(btnVisibility = it)
            }
        }
    }

    fun changePetTotal(total: Int) {
        launch {
            Observable.create<Int> {
                it.onNext(if (total > 1) View.VISIBLE else View.GONE)
                it.onComplete()
            }.with(scheduler).subscribe {
                PrintLog.d("changePetTotal", it.toString(), tag)
                uiData.value = UIModel(petTotal = total, petCountVisibility = it)
            }
        }
    }

    fun loadPetDetailData(userId: String, petNo: Int, reLoad: Boolean = false) {
        if (reLoad) {
            loadPetData(userId = userId, petNo = petNo)
        } else {
            petData?.let {
                loadPetDataEvent.value = LoadPetDataEvent(petData = it)
                uiData.value = UIModel(petImage = it.profileImage)
            } ?: loadPetData(userId = userId, petNo = petNo)

            petUsedGoodsData?.let {
                loadPetDataEvent.value = LoadPetDataEvent(petUsedGoods = it)
            }
        }
    }

    private fun loadPetData(userId: String, petNo: Int) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = TextConstants.CHECK_NETWORK_CONNECT))
            return
        }

        uiData.value = UIModel(isLoading = true)
        launch {
            apiPet.requestUserPet(scheduler = scheduler, userId = userId, petNo = petNo,
                    responseData = {
                        petData = it
                        PrintLog.d("requestUserPet success", it.toString(), tag)
                        loadPetDataEvent.value = LoadPetDataEvent(petData = it)
                        uiData.value = UIModel(isLoading = false, petImage = it.profileImage)

                        petUsedGoodsData?: loadPetUsedGoodsData(page = 1)
                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(isLoading = false)
                        errorData?.let {
                            PrintLog.d("requestUserPet fail", errorData.message, tag)
                        }
                    })
        }
    }

    fun loadPetUsedGoodsData(page: Int) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = TextConstants.CHECK_NETWORK_CONNECT))
            return
        }

        launch {
            apiPet.requestPetUsedGoods(scheduler = scheduler, petId = petData!!.id, page = page,
                    responseData = {
                        val petUsedGoods = TopicUsedGoodsData(total = it.total, nextPage = page + 1)
                        it.goodsData?.forEach { goods ->
                            petUsedGoods.items.add(TopicUsedGoodsListData(
                                    goodsCode = goods.code,
                                    imageURL = goods.image,
                                    rating = goods.rating
                            ))
                        }
                        loadPetDataEvent.value = LoadPetDataEvent(petUsedGoods = petUsedGoods)
                        PrintLog.d("requestPetUsedGoods success", petUsedGoods.toString(), tag)
                    },
                    errorData = { errorData ->
                        errorData?.let {
                            PrintLog.d("requestPetUsedGoods fail", errorData.message, tag)
                        }
                    })
        }
    }
}

data class PetDetailData(val breed: String, val topicColor: Int,
                         val type: String, val name: String,
                         val gender: Boolean, val age: String, val weight: String)
data class LoadPetDataEvent(val petData: ResUserPetData? = null, val petUsedGoods: TopicUsedGoodsData? = null)
data class UIModel(val isLoading: Boolean? = null, val toastMessage: String? = null,
                   val btnVisibility: Int? = null,
                   val petTotal: Int? = null, val petCountVisibility: Int? = null,
                   val petImage: String? = null,
                   val petDetailData: PetDetailData? = null, val petUsedGoods: TopicUsedGoodsData? = null)