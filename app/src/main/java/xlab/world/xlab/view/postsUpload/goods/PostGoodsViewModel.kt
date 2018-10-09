package xlab.world.xlab.view.postsUpload.goods

import xlab.world.xlab.server.provider.ApiShopProvider
import xlab.world.xlab.utils.rx.SchedulerProvider
import xlab.world.xlab.utils.support.NetworkCheck
import xlab.world.xlab.view.AbstractViewModel

class PostGoodsViewModel(private val apiShop: ApiShopProvider,
                         private val networkCheck: NetworkCheck,
                         private val scheduler: SchedulerProvider): AbstractViewModel() {
    val tag = "PostGoods"

}

data class UIModel(val isLoading: Boolean? = null, val toastMessage: String? = null)
