package xlab.world.xlab.view.profileEdit

import android.arch.lifecycle.MutableLiveData
import android.view.View
import io.reactivex.Observable
import okhttp3.MediaType
import okhttp3.RequestBody
import xlab.world.xlab.data.request.ReqProfileUpdateData
import xlab.world.xlab.server.provider.ApiUserProvider
import xlab.world.xlab.utils.rx.SchedulerProvider
import xlab.world.xlab.utils.rx.with
import xlab.world.xlab.utils.support.*
import xlab.world.xlab.view.AbstractViewModel
import xlab.world.xlab.view.SingleLiveEvent
import java.io.File

class ProfileEditViewModel(private val apiUser: ApiUserProvider,
                           private val networkCheck: NetworkCheck,
                           private val scheduler: SchedulerProvider): AbstractViewModel() {
    val tag = "ProfileEdit"

    private var initProfileData: InitProfile? = null
    private var newProfileImage: ArrayList<String> = ArrayList()

    val profileUpdateEvent = SingleLiveEvent<ProfileEditEvent>()
    val uiData = MutableLiveData<UIModel>()

    fun setNewProfileImage(profileImage: String) {
        newProfileImage.add(profileImage)
        uiData.value = UIModel(profileImage = newProfileImage.last())
    }

    fun loadProfileEditData(authorization: String) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = TextConstants.CHECK_NETWORK_CONNECT))
            return
        }

        uiData.value = UIModel(isLoading = true)
        launch {
            apiUser.requestProfileEdit(scheduler = scheduler, authorization = authorization,
                    responseData = {
                        initProfileData = InitProfile(nickName = it.nickName,
                                introduction = it.introduction,
                                gender = UserInfo.genderMap[it.gender],
                                birth = it.birthYear)

                        uiData.value = UIModel(isLoading = false,
                                profileImage = it.profileImg,
                                nickName = it.nickName,
                                introduction = it.introduction,
                                detailInfoVisibility =
                                if (it.gender != UserInfo.NO_GENDER_SELECT || it.locale.isNotEmpty() || it.birthYear.isNotEmpty()) View.GONE
                                else View.VISIBLE,
                                gender = UserInfo.genderMap[it.gender],
                                birth = it.birthYear)
                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(isLoading = false)
                        errorData?.let {
                            PrintLog.d("requestProfileEdit fail", errorData.message, tag)
                        }
                    })
        }
    }

    // 생년월일 체크
    fun birthRegexCheck(birth: String) {
        launch {
            Observable.create<Boolean> {
                val birthRegex =
                        if (birth.isNotEmpty()) DataRegex.birthRegex(birth.toInt())
                        else false

                it.onNext(birthRegex)
                it.onComplete()
            }.with(scheduler).subscribe { resultData ->
                PrintLog.d("birthRegexCheck", resultData.toString(), tag)
                uiData.value = UIModel(birthConfirmVisibility =
                if (resultData) View.INVISIBLE
                else View.VISIBLE)
            }
        }
    }

    // 변경된 데이터 있는지 확인
    fun existChangedData(nickName: String, introduction: String, gender: String?, birth: String) {
        launch {
            Observable.create<Boolean> {
                val recentData = InitProfile(nickName = nickName,
                        introduction = introduction,
                        gender = gender,
                        birth = birth)

                PrintLog.d("initProfileData", initProfileData.toString(), tag)
                PrintLog.d("recentData", recentData.toString(), tag)
                val birthRegex =
                        if (birth.isEmpty()) initProfileData?.let{_->initProfileData!!.birth.isEmpty()}?: let{_->false}
                        else DataRegex.birthRegex(recentData.birth.toInt())
                val regex = birthRegex && DataRegex.nickNameRegex(recentData.nickName)
                it.onNext(initProfileData?.let { _ ->
                    regex && ((initProfileData != recentData) || newProfileImage.isNotEmpty()) }
                        ?:let { _ ->false})
                it.onComplete()
            }.with(scheduler).subscribe { resultData ->
                PrintLog.d("existChangedData", resultData.toString(), tag)
                uiData.value = UIModel(saveEnable = resultData)
            }
        }
    }

    // 프로필 변경
    fun changeProfileData(authorization: String, userId: String,
                          nickName: String, introduction: String, gender: Int, birth: String) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = TextConstants.CHECK_NETWORK_CONNECT))
            return
        }

        uiData.value = UIModel(isLoading = true)
        launch {
            val reqData = ReqProfileUpdateData()
            // 프로필 이미지 변경된 경우
            if (newProfileImage.isNotEmpty()) {
                val imageFile = File(newProfileImage.last())
                val requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), imageFile)
                reqData.addProfileImage(fileName = imageFile.name, requestBody = requestBody)
            }
            // 닉네임 변경된 경우
            if (initProfileData!!.nickName != nickName && nickName.isNotEmpty())
                reqData.addNickName(nickName = nickName)

            // 소개 변경된 경우
            if (initProfileData!!.introduction != introduction)
                reqData.addIntroduction(introduction = introduction)

            // 성별
            reqData.addGender(gender = gender.toString())

            // 생년 변경된 경우
            if (initProfileData!!.birth != birth)
                reqData.addBirthYear(birthYear = birth)

            apiUser.requestProfileUpdate(scheduler = scheduler, authorization = authorization, userId = userId, requestBody = reqData.getReqBody(),
                    responseData = {
                        profileUpdateEvent.value = ProfileEditEvent(status = true)
                        uiData.value = UIModel(isLoading = false)
                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(isLoading = false)
                        errorData?.let {
                            PrintLog.d("requestProfileUpdate fail", errorData.message, tag)
                        }
                    })
        }

    }

    fun deleteProfileImage() {
        newProfileImage.forEach { filePath ->
            SupportData.deleteFile(path = filePath)
        }
    }
}

data class InitProfile(val nickName: String, val introduction: String, val gender: String?, val birth: String)
data class ProfileEditEvent(val status: Boolean? = null)
data class UIModel(val isLoading: Boolean? = null, val toastMessage: String? = null,
                   val profileImage: String? = null, val nickName: String? = null,
                   val introduction: String? = null, val detailInfoVisibility: Int? = null,
                   val gender: String? = null, val birth: String? = null,
                   val birthConfirmVisibility: Int? = null,
                   val saveEnable: Boolean? = null)