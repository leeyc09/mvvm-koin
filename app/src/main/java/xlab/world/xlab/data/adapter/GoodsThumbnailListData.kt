package xlab.world.xlab.data.adapter

import xlab.world.xlab.utils.support.AppConstants
import java.io.Serializable

data class GoodsThumbnailListData(val dataType: Int,
                                  val headerTitle: String = "",
                                  val goodsImage: String = "",
                                  val goodsCd: String = ""): Serializable
