package xlab.world.xlab.view.goodsDetail

import android.arch.lifecycle.MutableLiveData
import xlab.world.xlab.server.provider.ApiGodoProvider
import xlab.world.xlab.server.provider.ApiPetProvider
import xlab.world.xlab.server.provider.ApiShopProvider
import xlab.world.xlab.server.provider.ApiUserActivityProvider
import xlab.world.xlab.utils.rx.SchedulerProvider
import xlab.world.xlab.utils.support.NetworkCheck
import xlab.world.xlab.view.AbstractViewModel
import java.util.regex.Pattern

class GoodsDetailViewModel(private val apiGodo: ApiGodoProvider,
                           private val apiShop: ApiShopProvider,
                           private val apiPet: ApiPetProvider,
                           private val apiUserActivity: ApiUserActivityProvider,
                           private val networkCheck: NetworkCheck,
                           private val scheduler: SchedulerProvider): AbstractViewModel() {
    val tag = "GoodsDetail"
    private val imageRegexPattern = Pattern.compile("\\(?\\b(http://|www[.])[-A-Za-z0-9+&amp;@#/%?=~_()|!:,.;]*[-A-Za-z0-9+&amp;@#/%=~_()|]")

    val uiData = MutableLiveData<UIModel>()
}

data class GoodsDetailEvent(val status: Boolean? = null)

data class UIModel(val isLoading: Boolean? = null, val toastMessage: String? = null)
