package xlab.world.xlab.data.adapter

import java.io.Serializable

data class PostDetailGoodsListData(val goodsCode: String,
                                   val imageURL: String): Serializable {
    override fun toString(): String {
        return "goodsCode: $goodsCode / imageURL: $imageURL"
    }
}
