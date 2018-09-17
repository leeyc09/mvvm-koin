package xlab.world.xlab.view.profileEdit

import android.arch.lifecycle.MutableLiveData
import android.view.View
import io.reactivex.Observable
import xlab.world.xlab.server.provider.ApiUserProvider
import xlab.world.xlab.utils.rx.SchedulerProvider
import xlab.world.xlab.utils.rx.with
import xlab.world.xlab.utils.support.*
import xlab.world.xlab.view.AbstractViewModel

class ProfileEditViewModel(private val apiUser: ApiUserProvider,
                           private val networkCheck: NetworkCheck,
                           private val scheduler: SchedulerProvider): AbstractViewModel() {
    val tag = "ProfileEdit"

    private var initProfileData: InitProfile? = null

    val uiData = MutableLiveData<UIModel>()

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
                val birthRegex = DataRegex.birthRegex(birth.toInt())

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
                it.onNext(initProfileData?.let { _ -> initProfileData != recentData } ?:let { _ ->false})
                it.onComplete()
            }.with(scheduler).subscribe { resultData ->
                PrintLog.d("existChangedData", resultData.toString(), tag)
                uiData.value = UIModel(saveEnable = resultData)
            }
        }
    }
}

data class InitProfile(val nickName: String, val introduction: String, val gender: String?, val birth: String)
data class UIModel(val isLoading: Boolean? = null, val toastMessage: String? = null,
                   val profileImage: String? = null, val nickName: String? = null,
                   val introduction: String? = null, val detailInfoVisibility: Int? = null,
                   val gender: String? = null, val birth: String? = null,
                   val birthConfirmVisibility: Int? = null,
                   val saveEnable: Boolean? = null)