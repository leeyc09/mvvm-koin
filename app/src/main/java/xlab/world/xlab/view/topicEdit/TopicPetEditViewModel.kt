package xlab.world.xlab.view.topicEdit

import android.arch.lifecycle.MutableLiveData
import io.reactivex.Observable
import xlab.world.xlab.server.provider.ApiPetProvider
import xlab.world.xlab.utils.rx.SchedulerProvider
import xlab.world.xlab.utils.rx.with
import xlab.world.xlab.utils.support.NetworkCheck
import xlab.world.xlab.utils.support.PrintLog
import xlab.world.xlab.view.AbstractViewModel

class TopicPetEditViewModel(private val apiPet: ApiPetProvider,
                            private val networkCheck: NetworkCheck,
                            private val scheduler: SchedulerProvider): AbstractViewModel() {
    val tag = "TopicPetEdit"

    private var initPetData: InitPet? = null

    val uiData = MutableLiveData<UIModel>()

    // 변경된 데이터 있는지 확인
    fun existChangedData(topicColor: String?) {
        launch {
            Observable.create<Boolean> {
                val recentData = InitPet(topicColor = topicColor)

                PrintLog.d("initPetData", initPetData.toString(), tag)
                PrintLog.d("recentData", recentData.toString(), tag)

                it.onNext(initPetData?.let { _ ->
                    (initPetData != recentData) }
                        ?:let { _ ->
                            recentData.isFillData()
                        })
                it.onComplete()
            }.with(scheduler).subscribe { resultData ->
                PrintLog.d("existChangedData", resultData.toString(), tag)
                uiData.value = UIModel(saveEnable = resultData)
            }
        }
    }
}

data class InitPet(val topicColor: String?) {
    fun isFillData(): Boolean {
        return true
    }
}
data class UIModel(val isLoading: Boolean? = null, val toastMessage: String? = null,
                   val saveEnable: Boolean? = null)
