package xlab.world.xlab.data.response

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ResOrderDetailData(@SerializedName("message") val message: String,
                              @SerializedName("total") val total: Int,
                              @SerializedName("orderHistory") val goodsList: ArrayList<Goods>?,
                              @SerializedName("receiver") val receiver: Receiver,
                              @SerializedName("payment") val payment: Payment,
                              @SerializedName("price") val price: Price): Serializable {

    data class Goods(@SerializedName("sno") val sno: String, // 주문 상품 시리얼 번호
                     @SerializedName("orderNo") val orderNo: String, // 주문 번호
                     @SerializedName("orderStatus") val orderState: String, // 주문 상태
                     @SerializedName("invoiceNo") val invoiceNo: String, // 송장번호
                     @SerializedName("orderYear") val orderYear: String, // 주문 날짜 (년)
                     @SerializedName("orderMonth") val orderMonth: String, // 주문 날짜 (월)
                     @SerializedName("orderDay") val orderDay: String, // 주문 날짜 (일)
                     @SerializedName("image") val image: String, // 상품 이미지 (고도 url + 경로 + 이름)
                     @SerializedName("brandName") val brand: String, // 상품 브랜드 이름
                     @SerializedName("goodsCd") val code: String, // 상품 코드
                     @SerializedName("name") val name: String, // 상품 이름
                     @SerializedName("count") val count: Int, // 상품 갯수
                     @SerializedName("price") val price: Int, // 상품 가격 (단일)
                     @SerializedName("userHandleSno") val userHandleSno: String, // 배송 정보
                     @SerializedName("deliverySno") val deliverySno: String): Serializable // 환불, 반품, 교환 처리 시리얼 번호


    data class Receiver(@SerializedName("name") val name: String,
                        @SerializedName("phone") val phone: String,
                        @SerializedName("zoneCode") val zoneCode: String,
                        @SerializedName("address") val address: String,
                        @SerializedName("subAddress") val subAddress: String,
                        @SerializedName("memo") val memo: String): Serializable

    data class Payment(@SerializedName("type") val type: String,
                       @SerializedName("name") val name: String,
                       @SerializedName("bankInfo") val bankInfo: ArrayList<String>?): Serializable

    data class Price(@SerializedName("total") val total: Int,
                     @SerializedName("delivery") val delivery: Int,
                     @SerializedName("settle") val settle: Int): Serializable
}