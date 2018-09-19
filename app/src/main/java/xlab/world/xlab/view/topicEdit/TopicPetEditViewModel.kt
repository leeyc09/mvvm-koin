package xlab.world.xlab.view.topicEdit

import android.arch.lifecycle.MutableLiveData
import android.view.View
import io.reactivex.Observable
import xlab.world.xlab.data.adapter.PetHairTypeData
import xlab.world.xlab.data.adapter.PetHairTypeListData
import xlab.world.xlab.data.response.ResUserPetData
import xlab.world.xlab.server.provider.ApiPetProvider
import xlab.world.xlab.utils.rx.SchedulerProvider
import xlab.world.xlab.utils.rx.with
import xlab.world.xlab.utils.support.*
import xlab.world.xlab.view.AbstractViewModel

class TopicPetEditViewModel(private val apiPet: ApiPetProvider,
                            private val petInfo: PetInfo,
                            private val networkCheck: NetworkCheck,
                            private val scheduler: SchedulerProvider): AbstractViewModel() {
    val tag = "TopicPetEdit"

    private var initPetData: ResUserPetData? = null
    private var recentPetData = ResUserPetData(size = ArrayList(), hairColor = ArrayList())
    private val newPetImage: ArrayList<String> = ArrayList()

    val uiData = MutableLiveData<UIModel>()

    fun setNewProfileImage(petImage: String) {
        newPetImage.add(petImage)
        uiData.value = UIModel(petImage = newPetImage.last())
    }

    fun loadPetData(userId: String, petNo: Int, topicColorList: Array<String>) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = TextConstants.CHECK_NETWORK_CONNECT))
            return
        }

        uiData.value = UIModel(isLoading = true)
        launch {
            apiPet.requestUserPet(scheduler = scheduler, userId = userId, petNo = petNo,
                    responseData = {
                        initPetData = it.copy()
                        recentPetData = it.copy()

                        PrintLog.d("requestUserPet success", it.toString(), tag)

                        var colorIndex = 0
                        topicColorList.takeWhile { color -> it.topicColor != color}.forEach{ _-> colorIndex++ }

                        uiData.value = UIModel(isLoading = false,
                                topicColorData = TopicColorData(topicColor = it.topicColor, colorIndex = colorIndex),
                                petImage = it.profileImage,
                                petType = it.type == petInfo.dogCode, // true -> 강아지 false -> 고양이
                                petName = it.name,
                                petGender = it.gender == petInfo.femaleCode, // true -> 암컷 false -> 수컷
                                petNeutered = it.isNeutered,
                                petBirth = SupportData.birthDayForm(year = it.birthYear, month = it.birthMonth, day = it.birthDay),
                                petWeight = String.format("%.1f", it.weight))

                        setBreedDetailData(breedIndex = it.breed)
                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(isLoading = false)
                        errorData?.let {
                            PrintLog.d("requestUserPet fail", errorData.message, tag)
                        }
                    })
        }
    }

    // 변경된 데이터 있는지 확인
    fun existChangedData(isAddPet: Boolean,
                         topicColor: String? = null, petType: String? = null, petName: String? = null,
                         petGender: String? = null, petNeutered: Boolean? = null, petWeight: Float? = null) {
        launch {
            Observable.create<Boolean> {
                topicColor?.let {_-> recentPetData.topicColor = topicColor}
                petType?.let {_->
                    uiData.postValue(UIModel(petType = petType == petInfo.dogCode))
                    recentPetData.type = petType
                }
                petName?.let {_-> recentPetData.name = petName}
                petGender?.let {_->
                    uiData.postValue(UIModel(petGender = petGender == petInfo.femaleCode))
                    recentPetData.gender = petGender
                }
                petNeutered?.let {_->
                    uiData.postValue(UIModel(petNeutered = !petNeutered))
                    recentPetData.isNeutered = !petNeutered
                }
                petWeight?.let {_->
                    recentPetData.weight = petWeight
                }

                PrintLog.d("initPetData", initPetData.toString(), tag)
                PrintLog.d("recentData", recentPetData.toString(), tag)

                val result =
                        if(isAddPet) newPetImage.isNotEmpty() && recentPetData.isFillData()
                        else initPetData?.let { _ ->
                            recentPetData.isFillData() && ((initPetData != recentPetData) || newPetImage.isNotEmpty()) }
                                ?:let { _ ->false}

                it.onNext(result)
                it.onComplete()
            }.with(scheduler).subscribe { resultData ->
                PrintLog.d("existChangedData", resultData.toString(), tag)
                uiData.value = UIModel(saveEnable = resultData)
            }
        }
    }

    fun birthRegex(petBirth: String) {
        launch {
            Observable.create<Boolean> {
                it.onNext(DataRegex.birthRegex(birthday = petBirth))
                it.onComplete()
            }.with(scheduler).subscribe {
                PrintLog.d("birthRegex", it.toString(), tag)
                if (it) {
                    val birthSplit = petBirth.split("-")
                    recentPetData.birthYear = birthSplit[0].toInt()
                    recentPetData.birthMonth = birthSplit[1].toInt()
                    recentPetData.birthDay = birthSplit[2].toInt()
                } else {
                    recentPetData.birthYear = -1
                    recentPetData.birthMonth = -1
                    recentPetData.birthDay = -1
                }
                uiData.value = UIModel(birthRegexVisibility = if (it) View.INVISIBLE else View.VISIBLE)
            }
        }
    }

    fun setBreedDetailData(breedIndex: String) {
        launch {
            Observable.create<BreedData> {
                val petBreedData = when (recentPetData.type) {
                    petInfo.dogCode -> petInfo.dogBreedInfo.elementAt(breedIndex.toInt())
                    petInfo.catCode -> petInfo.catBreedInfo.elementAt(breedIndex.toInt())
                    else -> null
                }

                petBreedData?.let { _ ->
                    val breedData = BreedData()

                    recentPetData.breed = breedIndex
                    recentPetData.size?.clear()
                    recentPetData.size?.addAll(petBreedData.size)
                    // 예전 정보 삭제
//                    recentPetData.hairType = ""
//                    recentPetData.hairColor?.clear()

                    breedData.breedName = String.format("%s(%s)",petBreedData.nameKor, petBreedData.nameEn)

                    // 단일 헤어 타입
                    if (petBreedData.hairType.size == 1) {
                        recentPetData.hairType = petBreedData.hairType.last()
                        breedData.hairTypeData = null
                    } else {
                        val hairTypeData = PetHairTypeData()
                        petBreedData.hairType.forEach { hairTypeCode ->
                            PrintLog.d("hairTypeCode", hairTypeCode, tag)
                            PrintLog.d("recentPetData.hairType", recentPetData.hairType, tag)
                            hairTypeData.items.add(PetHairTypeListData(
                                    hairTypeCode = hairTypeCode,
                                    isSelect = hairTypeCode == recentPetData.hairType))
                        }
                        breedData.hairTypeData = hairTypeData
                    }

                    // 단일 색상 타입
                    if (petBreedData.hairColor.size == 1) {

                    } else {

                    }
                    it.onNext(breedData)
                    it.onComplete()
                }
            }.with(scheduler).subscribe {
                PrintLog.d("setBreedDetailData", it.toString(), tag)
                uiData.value = UIModel(breedName = it.breedName, isBreedSelect = true,
                        breedDetailVisibility = if (it.hairTypeData == null) View.GONE else View.VISIBLE,
                        hairTypeData = it.hairTypeData, hairTypeVisibility = it?.let{_->View.VISIBLE}?:let{_->View.GONE})
//                if (it) {
//                    val birthSplit = petBirth.split("-")
//                    recentPetData.birthYear = birthSplit[0].toInt()
//                    recentPetData.birthMonth = birthSplit[1].toInt()
//                    recentPetData.birthDay = birthSplit[2].toInt()
//                } else {
//                    recentPetData.birthYear = -1
//                    recentPetData.birthMonth = -1
//                    recentPetData.birthDay = -1
//                }
            }
        }
    }

    fun deleteProfileImage() {
        newPetImage.forEach { filePath ->
            SupportData.deleteFile(path = filePath)
        }
    }
}

data class TopicColorData(val topicColor: String, val colorIndex: Int)
data class BreedData(var breedName: String = "", var hairTypeData: PetHairTypeData? = PetHairTypeData())
data class UIModel(val isLoading: Boolean? = null, val toastMessage: String? = null,
                   val topicColorData: TopicColorData? = null, val petImage: String? = null,
                   val petType: Boolean? = null, val petName: String? = null,
                   val petGender: Boolean? = null, val petNeutered: Boolean? = null,
                   val breedName: String? = null, val isBreedSelect: Boolean? = null,
                   val breedDetailVisibility: Int? = null,
                   var hairTypeData: PetHairTypeData? = null, val hairTypeVisibility: Int? = null,
                   val petBirth: String? = null, val birthRegexVisibility: Int? = null,
                   val petWeight: String? = null,
                   val saveEnable: Boolean? = null)
