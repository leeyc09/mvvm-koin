package xlab.world.xlab.data.adapter

import java.io.Serializable

data class CartListData(val sno: String,
                        val goodsCode: String,
                        val goodsImage: String,
                        val goodsBrand: String,
                        val goodsName: String,
                        val goodsOption: ArrayList<String>?,
                        var goodsCnt: Int,
                        val goodsInitCnt: Int,
                        val goodsPrice: Int,
                        val deliverySno: String,
                        var isSelect: Boolean): Serializable
