package xlab.world.xlab.view.topicDetail

import android.app.Activity
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
import xlab.world.xlab.utils.support.ResultCodeData
import xlab.world.xlab.view.AbstractViewModel
import xlab.world.xlab.view.SingleLiveEvent

class TopicPetDetailViewModel(private val apiPet: ApiPetProvider,
                              private val petInfo: PetInfo,
                              private val networkCheck: NetworkCheck,
                              private val scheduler: SchedulerProvider): AbstractViewModel() {
    private val viewModelTag = "TopicPetDetail"

    private var resultCode = Activity.RESULT_CANCELED

    private var petData: ResUserPetData? = null
    private var petUsedGoodsData: TopicUsedGoodsData? = null

    val loadPetData = SingleLiveEvent<LoadPetModel>()
    val loadPetUsedGoodsEvent = SingleLiveEvent<TopicPetEvent>()
    val uiData = MutableLiveData<UIModel>()

    fun setResultCode(resultCode: Int) {
        when (resultCode) {
            ResultCodeData.LOGIN_SUCCESS,
            ResultCodeData.LOGOUT_SUCCESS -> {
                this.resultCode = resultCode
            }
            ResultCodeData.TOPIC_DELETE -> {
                if (this.resultCode == Activity.RESULT_CANCELED)
                    this.resultCode = resultCode
            }
            Activity.RESULT_OK -> {
                if (this.resultCode == Activity.RESULT_CANCELED||
                        this.resultCode == ResultCodeData.TOPIC_DELETE)
                    this.resultCode = resultCode
            }
        }
    }

    fun changePetData(petData: ResUserPetData?, userId: String, loginUserId: String) {
        petData?.let { _ ->
            this.petData = petData.copy()
            launch {
                Observable.create<ArrayList<Any>> {
                    val breed =
                            if (petData.type == petInfo.dogCode) petInfo.dogBreedInfo[petData.breed.toInt()].nameKor
                            else petInfo.catBreedInfo[petData.breed.toInt()].nameKor
                    val topicColor =
                            if (userId == loginUserId) Color.parseColor("#${petData.topicColor}")
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

                    // [0] - breed, [1] - topicColor, [2] - type,
                    // [3] - name, [4] - gender, [5] - age, [6] - weight
                    it.onNext(arrayListOf(breed, topicColor, type, name, gender, age,  weight))
                    it.onComplete()
                }.with(scheduler = scheduler).subscribe {
                    PrintLog.d("changePetData", it.toString())
                    uiData.value = UIModel(petBreed = it[0] as String,
                            topicColor = it[1] as Int,
                            petType = it[2] as String,
                            petName = it[3] as String,
                            petGender = it[4] as Boolean,
                            petAge = it[5] as String,
                            petWeight = it[6] as String)
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
                // 해당 펫이 로그인 한 유저 -> 수정, 추가 버튼 활성화
                it.onNext(if (userId == loginUserId) View.VISIBLE else View.GONE)
                it.onComplete()
            }.with(scheduler = scheduler).subscribe {
                PrintLog.d("setButtonVisible", it.toString(), viewModelTag)
                uiData.value = UIModel(editBtnVisibility = it, addBtnVisibility = it)
            }
        }
    }

    fun changePetTotal(total: Int) {
        launch {
            Observable.create<Int> {
                // 토탈이 1이상일 경우에만 ?/? 표시
                it.onNext(if (total > 1) View.VISIBLE else View.GONE)
                it.onComplete()
            }.with(scheduler = scheduler).subscribe {
                PrintLog.d("changePetTotal", it.toString(), viewModelTag)
                uiData.value = UIModel(petTotal = total, petCountVisibility = it)
            }
        }
    }

    // 펫 상세 (이름, 나이, 무게, 사용 제품)
    fun loadPetDetailData(userId: String, petNo: Int, reLoad: Boolean = false) {
        if (reLoad) { // 펫 상세 새로 불러오기
            loadPetData(userId = userId, petNo = petNo)
        } else { // 불러왔던 펫 상세 데이터 가져오기
            PrintLog.d("loadPetDetailData Pet Data", petData.toString(), viewModelTag)
            petData?.let {
                loadPetData.value = LoadPetModel(petData = it)
                uiData.value = UIModel(petImage = it.profileImage)
            } ?: loadPetData(userId = userId, petNo = petNo)

            PrintLog.d("loadPetDetailData Pet goods", petUsedGoodsData.toString(), viewModelTag)
            petUsedGoodsData?.let {
                loadPetData.value = LoadPetModel(petUsedGoods = it)
            }
        }
    }

    private fun loadPetData(userId: String, petNo: Int) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = networkCheck.networkErrorMsg))
            return
        }

        uiData.value = UIModel(isLoading = true)
        launch {
            apiPet.requestUserPet(scheduler = scheduler, userId = userId, petNo = petNo.toString(),
                    responseData = {
                        petData = it
                        PrintLog.d("requestUserPet success", it.toString(), viewModelTag)
                        loadPetData.value = LoadPetModel(petData = it)
                        uiData.value = UIModel(isLoading = false, petImage = it.profileImage)
                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(isLoading = false)
                        errorData?.let {
                            PrintLog.e("requestUserPet fail", errorData.message, viewModelTag)
                        }
                    })
        }
    }

    fun loadPetUsedGoodsData(page: Int) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = networkCheck.networkErrorMsg))
            return
        }

        loadPetUsedGoodsEvent.value = TopicPetEvent(status = true)
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
                        loadPetData.value = LoadPetModel(petUsedGoods = petUsedGoods)
                        PrintLog.d("requestPetUsedGoods success", petUsedGoods.toString(), viewModelTag)
                    },
                    errorData = { errorData ->
                        errorData?.let {
                            PrintLog.e("requestPetUsedGoods fail", errorData.message, viewModelTag)
                        }
                    })
        }
    }

    fun backBtnAction() {
        uiData.postValue(UIModel(resultCode = resultCode))
    }
}

data class TopicPetEvent(val status: Boolean? = null)
data class LoadPetModel(val petData: ResUserPetData? = null, val petUsedGoods: TopicUsedGoodsData? = null)
data class UIModel(val isLoading: Boolean? = null, val toastMessage: String? = null, val resultCode: Int? = null,
                   val editBtnVisibility: Int? = null,
                   val addBtnVisibility: Int? = null,
                   val petTotal: Int? = null, val petCountVisibility: Int? = null,
                   val petImage: String? = null, val petBreed: String? = null,
                   val topicColor: Int? = null, val petName: String? = null,
                   val petType: String? = null, val petGender: Boolean? = null,
                   val petAge: String? = null, val petWeight: String? = null,
                   val petUsedGoods: TopicUsedGoodsData? = null)
