package xlab.world.xlab.view.topicEdit

import android.app.Activity
import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.view.View
import io.reactivex.Observable
import okhttp3.MediaType
import okhttp3.RequestBody
import xlab.world.xlab.R
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
    private enum class EditMode { ADD, EDIT }

    private val viewModelTag = "TopicPetEdit"

    private var petNo: String = ""
    private var editMode: EditMode = EditMode.ADD
    private var existChangeData: Boolean = false
    private val newPetImage: ArrayList<String> = ArrayList()

    private var initPetData = ResUserPetData(size = ArrayList(), hairColor = ArrayList())
    private var recentPetData = ResUserPetData(size = ArrayList(), hairColor = ArrayList())

    private var petHairTypeData: PetHairFeatureData = PetHairFeatureData()
    private var petHairColorData: PetHairFeatureData = PetHairFeatureData()

    val setEditModeData = SingleLiveEvent<Boolean?>()
    val loadPetData = SingleLiveEvent<ResUserPetData?>()
    val setBreedData = SingleLiveEvent<Boolean?>()
    val uiData = MutableLiveData<UIModel>()

    fun setEditMode(context: Context, petNo: String?) {
        launch {
            Observable.create<ArrayList<Any?>> {
                // pet no 가 있으면 edit 모드, 없으면 add 모드
                editMode = petNo?.let{_->EditMode.EDIT}?:let{_->EditMode.ADD}
                this.petNo = petNo?.let{_->petNo}?:let{_->""}

                PrintLog.d("setEditMode editMode", editMode.toString(), viewModelTag)
                PrintLog.d("setEditMode petNo", this.petNo, viewModelTag)

                // edit 모드 설정에 따른 값
                // [0] - 타이틀, [1] - topic delete btn 활성 & 비활성, [2] - breed detail 활성 & 비활성
                val resultArrayList = ArrayList<Any?>()
                if (editMode == EditMode.EDIT) { // 수정 모드
                    resultArrayList.addAll(arrayListOf(context.getString(R.string.topic_pet_edit), // 타이틀
                            View.VISIBLE, // topic delete btn 활성 & 비활성
                            View.VISIBLE)) // breed detail 활성 & 비활성
                } else if (editMode == EditMode.ADD) { // 추가 모드
                    resultArrayList.addAll(arrayListOf(context.getString(R.string.topic_pet_add), // 타이틀
                            View.GONE, // topic delete btn 활성 & 비활성
                            View.GONE)) // breed detail 활성 & 비활성
                }

                it.onNext(resultArrayList)
                it.onComplete()
            }.with(scheduler = scheduler).subscribe {
                uiData.value = UIModel(titleStr = it[0] as String,
                        deleteBtnVisibility = it[1] as Int,
                        breedDetailVisibility = it[2] as Int)

                setEditModeData.value = editMode == EditMode.ADD // 추가모드 - true, 수정모드 - false
            }
        }
    }

    fun setNewProfileImage(petImage: String) {
        launch {
            Observable.create<String> {
                // 새로운 토픽 프로필 이미지 추가
                newPetImage.add(petImage)

                it.onNext(newPetImage.last())
                it.onComplete()
            }.with(scheduler = scheduler).subscribe {
                PrintLog.d("setNewProfileImage", it, viewModelTag)
                uiData.value = UIModel(petImage = it)
            }
        }
    }

    // 수정 할 펫 데이터
    fun loadPetData(context: Context, userId: String) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = networkCheck.networkErrorMsg))
            return
        }

        uiData.value = UIModel(isLoading = true)
        launch {
            apiPet.requestUserPet(scheduler = scheduler, userId = userId, petNo = petNo,
                    responseData = {
                        initPetData = it.valueCopy()
                        recentPetData = it.valueCopy()

                        PrintLog.d("requestUserPet success", it.toString(), viewModelTag)

                        // 토픽 컬러의 index 찾기
                        var colorIndex = 0
                        val topicColorList = context.resources.getStringArray(R.array.topicColorStringList)
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

                        loadPetData.value = recentPetData.copy()
                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(isLoading = false)
                        errorData?.let {
                            PrintLog.e("requestUserPet fail", errorData.message, viewModelTag)
                        }
                    })
        }
    }

    // 변경된 데이터 있는지 확인
    fun enableSaveData(context: Context, topicColor: String? = null, petType: String? = null, petName: String? = null,
                         petGender: String? = null, petNeutered: Boolean? = null, petWeight: Float? = null) {
        launch {
            Observable.create<Boolean> {
                // 변경 된 값이 있는경우 -> recent data update
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

                PrintLog.d("editMode", editMode.toString(), viewModelTag)
                PrintLog.d("initPetData", initPetData.toString(), viewModelTag)
                PrintLog.d("recentData", recentPetData.toString(), viewModelTag)
                PrintLog.d("newPetImage", newPetImage.toString(), viewModelTag)

                this.existChangeData = (initPetData != recentPetData) || newPetImage.isNotEmpty()
                PrintLog.d("existChangeData", existChangeData.toString(), viewModelTag)

                val result =
                        if(editMode == EditMode.ADD) newPetImage.isNotEmpty() && recentPetData.isFillData()
                        else recentPetData.isFillData() && this.existChangeData

                it.onNext(result)
                it.onComplete()
            }.with(scheduler = scheduler).subscribe {
                PrintLog.d("enable save btn", it.toString(),  viewModelTag)
                uiData.value = UIModel(saveEnable = it)
                petType?.let {_->
                    uiData.value = UIModel(breedName = context.getString(R.string.pet_breed_select), isBreedSelect = false,
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
            }.with(scheduler = scheduler).subscribe {
                PrintLog.d("birthRegex", it.toString(), viewModelTag)
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

    // 품종 상세 (털 색상, 털길이)
    fun setBreedDetailData(breedIndex: String, petData: ResUserPetData?) {
        launch {
            Observable.create<ArrayList<Any?>> {
                val petBreedData = when (recentPetData.type) {
                    petInfo.dogCode -> petInfo.dogBreedInfo.elementAt(breedIndex.toInt())
                    petInfo.catCode -> petInfo.catBreedInfo.elementAt(breedIndex.toInt())
                    else -> null
                }

                petBreedData?.let { _ ->
                    // 품종 데이터
                    // [0] - 이름, [1] - 헤어 타입, [2] - 헤어 색상
                    val breedData = ArrayList<Any?>()

                    recentPetData.breed = breedIndex
                    recentPetData.size?.clear()
                    recentPetData.size?.addAll(petBreedData.size)
                    // 예전 정보 삭제
                    petData?:let {_->
                        recentPetData.hairType = ""
                        recentPetData.hairColor?.clear()
                    }

                    // 품종 이름 추가
                    breedData.add(String.format("%s(%s)",petBreedData.nameKor, petBreedData.nameEn))

                    // 단일 헤어 타입
                    if (petBreedData.hairType.size == 1) {
                        recentPetData.hairType = petBreedData.hairType.last()
                        breedData.add(null)
                    } else {
                        val hairTypeData = PetHairFeatureData()
                        PrintLog.d("petBreedData.hairType", petBreedData.hairType.toString(), viewModelTag)
                        petData?.let{_-> PrintLog.d("petData.hairType", petData.hairType, viewModelTag) }
                        PrintLog.d("recentPetData.hairType", recentPetData.hairType, viewModelTag)
                        petBreedData.hairType.forEach { hairTypeCode ->
                            val isSelect = petData?.let {_->hairTypeCode == petData.hairType} ?:let {_->false}
                            hairTypeData.items.add(PetHairFeatureListData(
                                    hairFeatureCode = hairTypeCode,
                                    isSelect = isSelect))
                        }
                        this.petHairTypeData.updateData(petHairFeatureData = hairTypeData)
                        breedData.add(this.petHairTypeData)
                    }

                    // 단일 색상 타입
                    if (petBreedData.hairColor.size == 1) {
                        recentPetData.hairColor?.clear()
                        recentPetData.hairColor?.add(petBreedData.hairColor.last())
                        breedData.add(null)
                    } else {
                        val hairColorData = PetHairFeatureData()
                        PrintLog.d("petBreedData.hairColor", petBreedData.hairColor.toString(), viewModelTag)
                        petData?.let{_-> PrintLog.d("petData.hairColor", petData.hairColor.toString(), viewModelTag) }
                        PrintLog.d("recentPetData.hairColor", recentPetData.hairColor.toString(), viewModelTag)
                        petBreedData.hairColor.forEach { hairColorCode ->
                            val filterData = petData?.let {_->petData.hairColor?.filter { c -> c == hairColorCode }} ?:ArrayList()
                            hairColorData.items.add(PetHairFeatureListData(
                                    hairFeatureCode = hairColorCode,
                                    isSelect = filterData.isNotEmpty()
                            ))
                        }
                        this.petHairColorData.updateData(petHairFeatureData = hairColorData)
                        breedData.add(this.petHairColorData)
                    }

                    it.onNext(breedData)
                    it.onComplete()
                }
            }.with(scheduler = scheduler).subscribe {
                // [0] - 이름, [1] - 헤어 타입, [2] - 헤어 색상
                PrintLog.d("setBreedDetailData", it.toString(), viewModelTag)
                uiData.value = UIModel(
                        breedName = it[0] as String,
                        isBreedSelect = true,
                        breedDetailVisibility = if (it[1] == null && it[2] == null) View.GONE else View.VISIBLE,
                        hairTypeData = it[1] as PetHairFeatureData?,
                        hairTypeVisibility = it[1]?.let{_->View.VISIBLE}?:let{_->View.GONE},
                        hairColorData = it[2] as PetHairFeatureData?,
                        hairColorVisibility = it[2]?.let{_->View.VISIBLE}?:let{_->View.GONE})

                setBreedData.value = true
            }
        }
    }

    // 헤어 타입 선택
    fun hairTypeSelect(selectIndex: Int) {
        launch {
            Observable.create<Int> {
                recentPetData.hairType = this.petHairTypeData.items[selectIndex].hairFeatureCode

                this.petHairTypeData.items.forEach { data ->
                    data.isSelect = recentPetData.hairType == data.hairFeatureCode
                }

                it.onNext(selectIndex)
                it.onComplete()
            }.with(scheduler = scheduler).subscribe {
                PrintLog.d("hairColorSelect", this.petHairTypeData.items[selectIndex].hairFeatureCode, viewModelTag)
                uiData.value = UIModel(hairTypeUpdateIndex = it)
            }
        }
    }
    // 헤어 색상 선택
    fun hairColorSelect(selectIndex: Int) {
        launch {
            Observable.create<Int> {
                val colorData = this.petHairColorData.items[selectIndex]
                colorData.isSelect = !colorData.isSelect
                if (colorData.isSelect)
                    recentPetData.hairColor?.add(colorData.hairFeatureCode)
                else
                    recentPetData.hairColor?.remove(colorData.hairFeatureCode)

                it.onNext(selectIndex)
                it.onComplete()
            }.with(scheduler = scheduler).subscribe {
                PrintLog.d("petColorSelect", this.petHairColorData.items[selectIndex].toString(), viewModelTag)
                uiData.value = UIModel(hairColorUpdateIndex = it)
            }
        }
    }

    fun deleteImageFile() {
        newPetImage.forEach { filePath ->
            SupportData.deleteFile(path = filePath)
        }
    }

    fun savePet(authorization: String) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = networkCheck.networkErrorMsg))
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

            if (editMode == EditMode.ADD) // 추가 모드
                apiPet.requestAddPet(scheduler = scheduler, authorization = authorization, requestBody = reqPetData.getReqBody(),
                        responseData = {
                            PrintLog.d("requestAddPet success", "", viewModelTag)
                            uiData.value = UIModel(isLoading = false, resultCode = Activity.RESULT_OK)
                        },
                        errorData = { errorData ->
                            uiData.value = UIModel(isLoading = false)
                            errorData?.let {
                                PrintLog.e("requestAddPet fail", errorData.message, viewModelTag)
                            }
                        })
            else // 수정 모드
                apiPet.requestUpdatePet(scheduler = scheduler, authorization = authorization, petId = recentPetData.id, requestBody = reqPetData.getReqBody(),
                        responseData = {
                            PrintLog.d("requestUpdatePet success", "", viewModelTag)
                            uiData.value = UIModel(isLoading = false, resultCode = Activity.RESULT_OK)
                        },
                        errorData = { errorData ->
                            uiData.value = UIModel(isLoading = false)
                            errorData?.let {
                                PrintLog.e("requestUpdatePet fail", errorData.message, viewModelTag)
                            }
                        })
        }
    }

    fun deletePet(authorization: String) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = networkCheck.networkErrorMsg))
            return
        }

        uiData.value = UIModel(isLoading = true)
        launch {
            apiPet.requestDeletePet(scheduler = scheduler, authorization = authorization, petId = recentPetData.id,
                    responseData = {
                        PrintLog.d("requestDeletePet success", "", viewModelTag)
                        uiData.value = UIModel(isLoading = false, resultCode = ResultCodeData.TOPIC_DELETE)
                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(isLoading = false)
                        errorData?.let {
                            PrintLog.e("requestDeletePet fail", errorData.message, viewModelTag)
                        }
                    })
        }
    }

    fun backBtnAction() {
        // 변경 된 데이터 있으면 취소 다이얼로그 보이기
        // 없으면 result code => cancel 로 종료
        uiData.postValue(UIModel(
                resultCode = if (existChangeData) null else Activity.RESULT_CANCELED,
                editCancelDialogShow = if (existChangeData) true else null
        ))
    }
}

data class TopicColorData(val topicColor: String, val colorIndex: Int)
data class UIModel(val isLoading: Boolean? = null, val toastMessage: String? = null, val resultCode: Int? = null,
                   val titleStr: String? = null, val deleteBtnVisibility: Int? = null,
                   val topicColorData: TopicColorData? = null, val petImage: String? = null,
                   val petType: Boolean? = null, val petName: String? = null,
                   val petGender: Boolean? = null, val petNeutered: Boolean? = null,
                   val breedName: String? = null, val isBreedSelect: Boolean? = null,
                   val breedDetailVisibility: Int? = null,
                   val hairTypeData: PetHairFeatureData? = null, val hairTypeVisibility: Int? = null,
                   val hairTypeUpdateIndex: Int? = null,
                   val hairColorData: PetHairFeatureData? = null, val hairColorVisibility: Int? = null,
                   val hairColorUpdateIndex: Int? = null,
                   val petBirth: String? = null, val birthRegexVisibility: Int? = null,
                   val petWeight: String? = null,
                   val saveEnable: Boolean? = null,
                   val editCancelDialogShow: Boolean? = null)
