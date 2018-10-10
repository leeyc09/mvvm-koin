package xlab.world.xlab.data.adapter

import xlab.world.xlab.utils.support.AppConstants
import java.io.Serializable

data class ShopNotificationListData(val type: Int,
                                    val goodsCode: String,
                                    val goodsImage: String,
                                    val year: Int,
                                    val month: Int,
                                    val day: Int,
                                    val hour: Int,
                                    val minute: Int): Serializable
