package xlab.world.xlab.view.register

import android.arch.lifecycle.MutableLiveData
import xlab.world.xlab.server.provider.ApiUserProvider
import xlab.world.xlab.utils.rx.SchedulerProvider
import xlab.world.xlab.utils.support.NetworkCheck
import xlab.world.xlab.view.AbstractViewModel

class RegisterViewModel(private val apiUser: ApiUserProvider,
                        private val networkCheck: NetworkCheck,
                        private val scheduler: SchedulerProvider): AbstractViewModel() {
    val uiData = MutableLiveData<UIModel>()
}

data class UIModel(val isLoading: Boolean? = null, val toastMessage: String? = null)