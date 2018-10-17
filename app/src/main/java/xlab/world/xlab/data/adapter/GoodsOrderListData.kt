package xlab.world.xlab.data.adapter

import java.io.Serializable

data class GoodsOrderListData(val sno: String,  // 주문 상품 시리얼 번호
                              val orderNo: String, // 주문 번호
                              val orderStatus: String, // 주문 상태
                              val invoiceNo: String, // 송장번호
                              val orderYear: String, // 주문 날짜 (년)
                              val orderMonth: String, // 주문 날짜 (월)
                              val orderDay: String, // 주문 날짜 (일)
                              val code: String, // 상품 코드
                              val image: String, // 상품 이미지 (고도 url + 경로 + 이름)
                              val brand: String, // 상품 브랜드 이름
                              val name: String, // 상품 이름
                              val option: ArrayList<String>?, // 상품 옵션
                              val count: Int, // 상품 갯수
                              val price: Int, // 상품 가격 (단일)
                              val userHandleSno: String, // 배송 정보
                              val deliveryNo: String): Serializable // 환불, 반품, 교환 처리 시리얼 번호
