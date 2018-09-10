package xlab.world.xlab.view.main

import android.arch.lifecycle.MutableLiveData
import xlab.world.xlab.utils.rx.SchedulerProvider
import xlab.world.xlab.utils.support.NetworkCheck
import xlab.world.xlab.view.AbstractViewModel

class MainViewModel(private val networkCheck: NetworkCheck,
                    private val scheduler: SchedulerProvider): AbstractViewModel() {
    val tag = "Main"

    val uiData = MutableLiveData<UIModel>()
}

data class UIModel(val isLoading: Boolean? = null, val toastMessage: String? = null)