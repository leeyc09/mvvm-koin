package xlab.world.xlab.view.crrDetail

import android.arch.lifecycle.MutableLiveData
import xlab.world.xlab.server.provider.ApiGodoProvider
import xlab.world.xlab.utils.rx.SchedulerProvider
import xlab.world.xlab.utils.support.NetworkCheck
import xlab.world.xlab.view.AbstractViewModel

class CRRDetailViewModel(private val apiGodo: ApiGodoProvider,
                         private val networkCheck: NetworkCheck,
                         private val scheduler: SchedulerProvider): AbstractViewModel() {

    val uiData = MutableLiveData<UIModel>()
}

data class UIModel(val isLoading: Boolean? = null)
