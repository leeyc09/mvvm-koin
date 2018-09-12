package xlab.world.xlab.data.adapter

import xlab.world.xlab.utils.support.AppConstants
import java.io.Serializable

data class SearchGoodsListData(val dataType: Int,
                               val sortType: Int,
                               val goodsCd: String,
                               val imageURL: String,
                               val price: Int = 0,
                               val title: String,
                               val brand: String,
                               var showQuestionMark: Boolean,
                               val matchingPercent: Int,
                               val matchColor: Int): Serializable {
    override fun toString(): String {
        return "dataType: $dataType / sortType: $sortType / goodsCd: $goodsCd / " +
                "imageURL: $imageURL / price: $price / title: $title / brand: $brand / " +
                "showQuestionMark: $showQuestionMark / matchingPercent: $matchingPercent / matchColor: $matchColor"
    }
}
