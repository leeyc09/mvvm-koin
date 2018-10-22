package xlab.world.xlab.view.preload

import io.reactivex.Observable
import xlab.world.xlab.utils.rx.SchedulerProvider
import xlab.world.xlab.utils.rx.with
import xlab.world.xlab.utils.support.PrintLog
import xlab.world.xlab.view.AbstractViewModel
import xlab.world.xlab.view.SingleLiveEvent

class PreloadViewModel(private val scheduler: SchedulerProvider): AbstractViewModel() {
    private val viewModelTag = "PreLoad"

    val onBoardingData = SingleLiveEvent<OnBoardingModel>()
    val loginRecordModelData = SingleLiveEvent<LoginRecordModel>()

    // onBoarding 화면 본적 없는 경우 -> onBoarding 화면으로
    // 본적 있는 경우 -> Login check
    fun checkOnBoarding(onBoarding: Boolean) {
        launch {
            Observable.create<Boolean> {
                PrintLog.d("checkOnBoarding", onBoarding.toString(), viewModelTag)
                it.onNext(!onBoarding)
                it.onComplete()
            }.with(scheduler = scheduler).subscribe {
                PrintLog.d("needOnBoardingPage", it.toString(), viewModelTag)
                onBoardingData.postValue(OnBoardingModel(needOnBoardingPage = it))
            }
        }
    }

    // login 기록 있는 경우 -> login 요청
    // 기록 없는 경우 -> login 화면으로
    fun loginRecordCheck(accessToken: String) {
        launch {
            Observable.create<Boolean> {
                PrintLog.d("loginRecordCheck", accessToken, viewModelTag)
                it.onNext(accessToken.isNotEmpty())
                it.onComplete()
            }.with(scheduler = scheduler).subscribe {
                PrintLog.d("hasLoginRecord", it.toString(), viewModelTag)
                loginRecordModelData.postValue(LoginRecordModel(hasLoginRecord = it))
            }
        }
    }
}

data class OnBoardingModel(val needOnBoardingPage: Boolean? = null)
data class LoginRecordModel(val hasLoginRecord: Boolean? = null)
