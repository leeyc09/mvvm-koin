package xlab.world.xlab.data.response

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ResGoodsDetailData(@SerializedName("message") val message: String,
                              @SerializedName("goods") val goods: GoodsData): Serializable {

    data class GoodsData(@SerializedName("goodsNo") val no: String, // 번호 (고도)
                         @SerializedName("goodsNm") val name: String, // 이름
                         @SerializedName("goodsCd") val code: String, // 고유 코드
                         @SerializedName("originNm") val originName: String, // 원산지
                         @SerializedName("makerNm") val makerName: String, // 제조사 이름
                         @SerializedName("brandCd") val brandCode: String, // 브랜드 코드
                         @SerializedName("brandNm") val brandName: String, // 브랜드 이름
                         @SerializedName("goodsPriceString") val priceStr: String, // 가격 대체 문구
                         @SerializedName("goodsPrice") val price: Int, // 가격
                         @SerializedName("deliverySno") val deliverySno: Int, // 배송 정보
                         @SerializedName("mainImage") val mainImage: String, // 제품 대표 이미지
                         @SerializedName("goodsDescription") val description: String, // 제품 상세 이미지 (html 형식 -> 분리해야됨)
                         @SerializedName("soldOut") val isSoldOut: Boolean): Serializable // 제품 품절 여부
}
