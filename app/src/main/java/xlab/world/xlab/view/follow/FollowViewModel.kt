package xlab.world.xlab.view.follow

import android.arch.lifecycle.MutableLiveData
import xlab.world.xlab.server.provider.ApiFollowProvider
import xlab.world.xlab.utils.rx.SchedulerProvider
import xlab.world.xlab.utils.support.NetworkCheck
import xlab.world.xlab.view.AbstractViewModel

class FollowViewModel(private val apiFollow: ApiFollowProvider,
                      private val networkCheck: NetworkCheck,
                      private val scheduler: SchedulerProvider): AbstractViewModel() {
    val tag = "Follow"

    val uiData = MutableLiveData<UIModel>()
}

data class FollowEvent(val status: Boolean? = null)
data class UIModel(val isLoading: Boolean? = null, val toastMessage: String? = null)
