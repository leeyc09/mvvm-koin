package xlab.world.xlab.view.topicEdit

import android.arch.lifecycle.MutableLiveData
import android.view.View
import io.reactivex.Observable
import okhttp3.MediaType
import okhttp3.RequestBody
import xlab.world.xlab.data.adapter.PetHairFeatureData
import xlab.world.xlab.data.adapter.PetHairFeatureListData
import xlab.world.xlab.data.request.ReqPetData
import xlab.world.xlab.data.response.ResUserPetData
import xlab.world.xlab.server.provider.ApiPetProvider
import xlab.world.xlab.utils.rx.SchedulerProvider
import xlab.world.xlab.utils.rx.with
import xlab.world.xlab.utils.support.*
import xlab.world.xlab.view.AbstractViewModel
import xlab.world.xlab.view.SingleLiveEvent
import java.io.File

class TopicPetEditViewModel(private val apiPet: ApiPetProvider,
                            private val petInfo: PetInfo,
                            private val networkCheck: NetworkCheck,
                            private val scheduler: SchedulerProvider): AbstractViewModel() {
    val tag = "TopicPetEdit"

    private var initPetData = ResUserPetData(size = ArrayList(), hairColor = ArrayList())
    private var recentPetData = ResUserPetData(size = ArrayList(), hairColor = ArrayList())
    private val newPetImage: ArrayList<String> = ArrayList()

    val saveDeletePetEvent = SingleLiveEvent<TopicEditEventData>()
    val enableSaveDataEvent = SingleLiveEvent<TopicEditEventData>()
    val uiData = MutableLiveData<UIModel>()

    var isAddPet: Boolean = true

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
                        initPetData = it.valueCopy()
                        recentPetData = it.valueCopy()

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

                        setBreedDetailData(breedIndex = it.breed, petData = recentPetData.copy())
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
    fun enableSaveData(topicColor: String? = null, petType: String? = null, petName: String? = null,
                         petGender: String? = null, petNeutered: Boolean? = null, petWeight: Float? = null) {
        launch {
            Observable.create<Boolean> {
                topicColor?.let {_-> recentPetData.topicColor = topicColor}
                petType?.let {_->
                    uiData.postValue(UIModel(petType = petType == petInfo.dogCode))
                    recentPetData.type = petType
                    recentPetData.hairType = ""
                    recentPetData.hairColor?.clear()
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

                PrintLog.d("isAddPet", isAddPet.toString(), tag)
                PrintLog.d("initPetData", initPetData.toString(), tag)
                PrintLog.d("recentData", recentPetData.toString(), tag)
                PrintLog.d("newPetImage", newPetImage.toString(), tag)

                val result =
                        if(isAddPet) newPetImage.isNotEmpty() && recentPetData.isFillData()
                        else recentPetData.isFillData() && ((initPetData != recentPetData) || newPetImage.isNotEmpty())

                enableSaveDataEvent.postValue(TopicEditEventData(
                        status = (initPetData != recentPetData) || newPetImage.isNotEmpty()))

                it.onNext(result)
                it.onComplete()
            }.with(scheduler).subscribe { resultData ->
                PrintLog.d("existChangedData", resultData.toString(), tag)
                uiData.value = UIModel(saveEnable = resultData)
                petType?.let {
                    uiData.value = UIModel(breedName = TextConstants.SELET_PET_BREED, isBreedSelect = false,
                            breedDetailVisibility = View.GONE,
                            hairTypeVisibility = View.GONE, hairColorVisibility = View.GONE)
                }
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

    fun setBreedDetailData(breedIndex: String, petData: ResUserPetData?) {
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
                    petData?:let {_->
                        recentPetData.hairType = ""
                        recentPetData.hairColor?.clear()
                    }

                    breedData.breedName = String.format("%s(%s)",petBreedData.nameKor, petBreedData.nameEn)

                    // 단일 헤어 타입
                    if (petBreedData.hairType.size == 1) {
                        recentPetData.hairType = petBreedData.hairType.last()
                        breedData.hairTypeData = null
                    } else {
                        val hairTypeData = PetHairFeatureData()
                        PrintLog.d("petBreedData.hairTypeCode", petBreedData.hairType.toString(), tag)
                        petData?.let{_-> PrintLog.d("petData.hairType", petData.hairType, tag) }
                        PrintLog.d("recentPetData.hairType", recentPetData.hairType, tag)
                        petBreedData.hairType.forEach { hairTypeCode ->
                            val isSelect = petData?.let {_->hairTypeCode == petData.hairType} ?:let {_->false}
                            hairTypeData.items.add(PetHairFeatureListData(
                                    hairFeatureCode = hairTypeCode,
                                    isSelect = isSelect))
                        }
                        breedData.hairTypeData = hairTypeData
                    }

                    // 단일 색상 타입
                    if (petBreedData.hairColor.size == 1) {
                        recentPetData.hairColor?.clear()
                        recentPetData.hairColor?.add(petBreedData.hairColor.last())
                        breedData.hairColorData = null
                    } else {
                        val hairColorData = PetHairFeatureData()
                        PrintLog.d("petBreedData.hairColor", petBreedData.hairColor.toString(), tag)
                        petData?.let{_-> PrintLog.d("petData.hairColor", petData.hairColor.toString(), tag) }
                        PrintLog.d("recentPetData.hairColor", recentPetData.hairColor.toString(), tag)
                        petBreedData.hairColor.forEach { hairColorCode ->
                            val filterData = petData?.let {_->petData.hairColor?.filter { c -> c == hairColorCode }} ?:ArrayList()
//                            val filterData = recentPetData.hairColor?.filter { c -> c == hairColorCode }
                            hairColorData.items.add(PetHairFeatureListData(
                                    hairFeatureCode = hairColorCode,
                                    isSelect = filterData.isNotEmpty()
                            ))
                        }
                        breedData.hairColorData = hairColorData
                    }
                    it.onNext(breedData)
                    it.onComplete()
                }
            }.with(scheduler).subscribe {
                PrintLog.d("setBreedDetailData", it.toString(), tag)
                uiData.value = UIModel(breedName = it.breedName, isBreedSelect = true,
                        breedDetailVisibility = if (it.hairTypeData == null && it.hairColorData == null) View.GONE else View.VISIBLE,
                        hairTypeData = it.hairTypeData, hairTypeVisibility = it.hairTypeData?.let{_->View.VISIBLE}?:let{_->View.GONE},
                        hairColorData = it.hairColorData, hairColorVisibility = it.hairColorData?.let{_->View.VISIBLE}?:let{_->View.GONE})

                enableSaveData()
            }
        }
    }

    fun hairTypeSelect(hairType: String) {
        launch {
            Observable.create<String> {
                recentPetData.hairType = hairType

                it.onNext(hairType)
                it.onComplete()
            }.with(scheduler).subscribe {
                PrintLog.d("hairColorSelect", it, tag)
                uiData.value = UIModel(hairTypeUpdateValue = it)
            }
        }
    }

    fun hairColorSelect(position: Int, hairColorData: PetHairFeatureListData) {
        launch {
            Observable.create<Int> {
                hairColorData.isSelect = !hairColorData.isSelect
                if (hairColorData.isSelect)
                    recentPetData.hairColor?.add(hairColorData.hairFeatureCode)
                else
                    recentPetData.hairColor?.remove(hairColorData.hairFeatureCode)

                it.onNext(position)
                it.onComplete()
            }.with(scheduler).subscribe {
                PrintLog.d("petColorSelect", hairColorData.toString(), tag)
                uiData.value = UIModel(hairColorUpdateIndex = it)
            }
        }
    }

    fun deleteProfileImage() {
        newPetImage.forEach { filePath ->
            SupportData.deleteFile(path = filePath)
        }
    }

    fun savePet(authorization: String) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = TextConstants.CHECK_NETWORK_CONNECT))
            return
        }

        uiData.value = UIModel(isLoading = true)
        launch {
            val reqPetData = ReqPetData()
            reqPetData.addTopicColor(topicColor = recentPetData.topicColor)
            if (newPetImage.isNotEmpty()) {
                val imageFile = File(newPetImage.last())
                val requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), imageFile)
                reqPetData.addPetImage(fileName = imageFile.name, requestBody = requestBody)
            }
            reqPetData.addPetType(type = recentPetData.type)
            reqPetData.addPetName(name = recentPetData.name)
            reqPetData.addPetGender(gender = recentPetData.gender)
            reqPetData.addPetNeutered(isNeutered = recentPetData.isNeutered)
            reqPetData.addPetBreed(breed = recentPetData.breed)
            recentPetData.size?.forEach {
                reqPetData.addPetSize(size = it)
            }
            reqPetData.addPetHairType(hairType = recentPetData.hairType)
            recentPetData.hairColor?.let {
                it.sort()
                it.forEach { color->
                    reqPetData.addPetHairColor(hairColor = color)
                }
            }
            reqPetData.addPetBirthYear(birthYear = recentPetData.birthYear)
            reqPetData.addPetBirthMonth(birthMonth = recentPetData.birthMonth)
            reqPetData.addPetBirthDay(birthDay = recentPetData.birthDay)
            reqPetData.addPetWeight(weight = recentPetData.weight)

            if (isAddPet)
                apiPet.requestAddPet(scheduler = scheduler, authorization = authorization, requestBody = reqPetData.getReqBody(),
                        responseData = {
                            PrintLog.d("requestAddPet success", "", tag)
                            saveDeletePetEvent.value = TopicEditEventData(status = true)
                            uiData.value = UIModel(isLoading = false)
                        },
                        errorData = { errorData ->
                            uiData.value = UIModel(isLoading = false)
                            errorData?.let {
                                PrintLog.d("requestAddPet fail", errorData.message, tag)
                            }
                        })
            else
                apiPet.requestUpdatePet(scheduler = scheduler, authorization = authorization, petId = recentPetData.id, requestBody = reqPetData.getReqBody(),
                        responseData = {
                            PrintLog.d("requestAddPet success", "", tag)
                            saveDeletePetEvent.value = TopicEditEventData(status = true)
                            uiData.value = UIModel(isLoading = false)
                        },
                        errorData = { errorData ->
                            uiData.value = UIModel(isLoading = false)
                            errorData?.let {
                                PrintLog.d("requestUpdatePet fail", errorData.message, tag)
                            }
                        })
        }
    }

    fun deletePet(authorization: String) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = TextConstants.CHECK_NETWORK_CONNECT))
            return
        }

        uiData.value = UIModel(isLoading = true)
        launch {
            apiPet.requestDeletePet(scheduler = scheduler, authorization = authorization, petId = recentPetData.id,
                    responseData = {
                        PrintLog.d("requestDeletePet success", "", tag)
                        saveDeletePetEvent.value = TopicEditEventData(status = false)
                        uiData.value = UIModel(isLoading = false)
                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(isLoading = false)
                        errorData?.let {
                            PrintLog.d("requestDeletePet fail", errorData.message, tag)
                        }
                    })
        }
    }
}

data class TopicColorData(val topicColor: String, val colorIndex: Int)
data class BreedData(var breedName: String = "",
                     var hairTypeData: PetHairFeatureData? = PetHairFeatureData(),
                     var hairColorData: PetHairFeatureData? = PetHairFeatureData())
data class TopicEditEventData(val status: Boolean? = null)
data class UIModel(val isLoading: Boolean? = null, val toastMessage: String? = null,
                   val topicColorData: TopicColorData? = null, val petImage: String? = null,
                   val petType: Boolean? = null, val petName: String? = null,
                   val petGender: Boolean? = null, val petNeutered: Boolean? = null,
                   val breedName: String? = null, val isBreedSelect: Boolean? = null,
                   val breedDetailVisibility: Int? = null,
                   val hairTypeData: PetHairFeatureData? = null, val hairTypeVisibility: Int? = null,
                   val hairTypeUpdateValue: String? = null,
                   val hairColorData: PetHairFeatureData? = null, val hairColorVisibility: Int? = null,
                   val hairColorUpdateIndex: Int? = null,
                   val petBirth: String? = null, val birthRegexVisibility: Int? = null,
                   val petWeight: String? = null,
                   val saveEnable: Boolean? = null)
