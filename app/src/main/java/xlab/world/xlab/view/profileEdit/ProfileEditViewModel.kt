package xlab.world.xlab.view.profileEdit

import android.app.Activity
import android.arch.lifecycle.MutableLiveData
import android.view.View
import io.reactivex.Observable
import okhttp3.MediaType
import okhttp3.RequestBody
import xlab.world.xlab.data.request.ReqProfileUpdateData
import xlab.world.xlab.data.response.ResProfileEditData
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
    private val viewModelTag = "ProfileEdit"
    private var existChangeData: Boolean = false

    private var initProfileData = ResProfileEditData()
    private var recentProfileData = ResProfileEditData()
    private val newProfileImage: ArrayList<String> = ArrayList()

    val uiData = MutableLiveData<UIModel>()

    fun setNewProfileImage(profileImage: String) {
        launch {
            Observable.create<String> {
                // 새로운 프로필 이미지 추가
                newProfileImage.add(profileImage)

                it.onNext(newProfileImage.last())
                it.onComplete()
            }.with(scheduler = scheduler).subscribe {
                PrintLog.d("setNewProfileImage", it, viewModelTag)
                uiData.value = UIModel(profileImage = it)
            }
        }
    }

    fun loadProfileEditData(authorization: String) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = networkCheck.networkErrorMsg))
            return
        }

        uiData.value = UIModel(isLoading = true)
        launch {
            apiUser.requestProfileEdit(scheduler = scheduler, authorization = authorization,
                    responseData = {
                        PrintLog.d("requestProfileEdit success", it.toString(), viewModelTag)
                        initProfileData = it.copy()
                        recentProfileData = it.copy()

                        uiData.value = UIModel(isLoading = false,
                                profileImage = it.profileImg,
                                nickName = it.nickName,
                                introduction = it.introduction,
                                detailInfoVisibility =
                                if (it.gender != UserInfo.NO_GENDER_SELECT || it.locale.isNotEmpty() || it.birthYear.isNotEmpty()) View.GONE
                                else View.VISIBLE,
                                gender = UserInfo.genderMap[it.gender],
                                birth = it.birthYear,
                                saveEnable = false)
                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(isLoading = false)
                        errorData?.let {
                            PrintLog.e("requestProfileEdit fail", errorData.message, viewModelTag)
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
            }.with(scheduler = scheduler).subscribe { resultData ->
                PrintLog.d("birthRegexCheck", resultData.toString(), viewModelTag)
                uiData.value = UIModel(birthConfirmVisibility =
                if (resultData) View.INVISIBLE
                else View.VISIBLE)
            }
        }
    }

    // 변경된 데이터 있는지 확인
    fun existChangedData(nickName: String? = null, introduction: String? = null, gender: Int? = null, birth: String? = null) {
        launch {
            Observable.create<Boolean> {
                // 변경 된 값이 있는경우 -> recent data update
                nickName?.let {_-> recentProfileData.nickName = nickName }
                introduction?.let {_-> recentProfileData.introduction = introduction }
                gender?.let {_-> recentProfileData.gender = gender }
                birth?.let {_-> recentProfileData.birthYear = birth }

                PrintLog.d("initProfileData", initProfileData.toString(), viewModelTag)
                PrintLog.d("recentData", recentProfileData.toString(), viewModelTag)
                val birthRegex =
                        if (recentProfileData.birthYear.isEmpty()) initProfileData?.let{_->initProfileData!!.birthYear.isEmpty()}?: let{_->false}
                        else DataRegex.birthRegex(recentProfileData.birthYear.toInt())
                val regex = birthRegex && DataRegex.nickNameRegex(recentProfileData.nickName)

                this.existChangeData = (initProfileData != recentProfileData) || newProfileImage.isNotEmpty()

                it.onNext(regex && this.existChangeData)
                it.onComplete()
            }.with(scheduler = scheduler).subscribe { resultData ->
                PrintLog.d("existChangedData", resultData.toString(), viewModelTag)
                uiData.value = UIModel(saveEnable = resultData)
            }
        }
    }

    // 프로필 변경
    fun changeProfileData(authorization: String, userId: String) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = networkCheck.networkErrorMsg))
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
            if (initProfileData!!.nickName != recentProfileData.nickName && recentProfileData.nickName.isNotEmpty())
                reqData.addNickName(nickName = recentProfileData.nickName)

            // 소개 변경된 경우
            if (initProfileData!!.introduction != recentProfileData.introduction)
                reqData.addIntroduction(introduction = recentProfileData.introduction)

            // 성별
            reqData.addGender(gender = recentProfileData.gender)

            // 생년 변경된 경우
            if (initProfileData!!.birthYear != recentProfileData.birthYear)
                reqData.addBirthYear(birthYear = recentProfileData.birthYear)

            apiUser.requestProfileUpdate(scheduler = scheduler, authorization = authorization, userId = userId, requestBody = reqData.getReqBody(),
                    responseData = {
                        uiData.value = UIModel(isLoading = false, resultCode = Activity.RESULT_OK)
                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(isLoading = false)
                        errorData?.let {
                            PrintLog.d("requestProfileUpdate fail", errorData.message)
                        }
                    })
        }

    }

    fun deleteProfileImage() {
        newProfileImage.forEach { filePath ->
            SupportData.deleteFile(path = filePath)
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

data class UIModel(val isLoading: Boolean? = null, val toastMessage: String? = null, val resultCode: Int? = null,
                   val profileImage: String? = null, val nickName: String? = null,
                   val introduction: String? = null, val detailInfoVisibility: Int? = null,
                   val gender: String? = null, val birth: String? = null,
                   val birthConfirmVisibility: Int? = null,
                   val saveEnable: Boolean? = null,
                   val editCancelDialogShow: Boolean? = null)