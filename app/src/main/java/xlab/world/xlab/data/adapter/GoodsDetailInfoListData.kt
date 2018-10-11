package xlab.world.xlab.data.adapter

import xlab.world.xlab.adapter.recyclerView.GoodsDetailInfoAdapter
import xlab.world.xlab.utils.support.AppConstants
import java.io.Serializable

data class GoodsDetailInfoListData(val dataType: Int = AppConstants.ADAPTER_CONTENT,
                                   var headerTitle: String = "",
                                   var headerSubTitle: String = "",
                                   var detailUrl: ArrayList<String> = ArrayList(),
                                   var detailText: String = "",
                                   var isShowAllDescription: Boolean = false,
                                   var footerTitle: String = "",
                                   var footerIndex: Int = 0,
                                   var necessaryInfo: GoodsDetailInfoAdapter.NecessaryInfo? = null): Serializable
