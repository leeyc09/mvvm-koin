package xlab.world.xlab.utils.support

import android.graphics.Color

object GodoData {
    const val DELIVERY_DEFAULT = 1
    const val DELIVERY_ALLOW_PRICE = 2
    const val DELIVERY_ALLOW_NUM = 3
    const val DELIVERY_FREE = 4

    const val REQUEST_REFUND_CODE = "refundRegist"
    const val REQUEST_RETURN_CODE = "backRegist"
    const val REQUEST_CHANGE_CODE = "exchangeRegist"

    val paymentTypeMap = hashMapOf(
            "gb" to "무통장 입금",
            "fa" to "무통장 입금",
            "pv" to "가상계좌",
            "fv" to "가상계좌",
            "ev" to "가상계좌",
            "pc" to "카드결제",
            "fc" to "카드결제",
            "ec" to "카드결제",
            "pb" to "계좌이체",
            "fb" to "계좌이체",
            "eb" to "계좌이체"
    )
    private val purpleColor = Color.parseColor("#5300ED")
    private val grayColor = Color.parseColor("#A4A4A4")
    private val redColor = Color.parseColor("#DE5359")
    val orderStateMap = hashMapOf(
            // Normal state
            "o1" to OrderState("입금대기", "", purpleColor, purpleColor, false),
            "p1" to OrderState("결제완료", "", grayColor, grayColor, false),
            "g1" to OrderState("상품준비중", "", grayColor, grayColor, false),
            "d1" to OrderState("배송중", "", purpleColor, purpleColor, true),
            "d2" to OrderState("배송완료", "", purpleColor, purpleColor, true),
            "s1" to OrderState("구매확정", "", grayColor, grayColor, false),
            // Cancel state
            "c1" to OrderState("자동취소", "", redColor, redColor, false),
            "c2" to OrderState("품절취소", "", redColor, redColor, false),
            "c3" to OrderState("관리자취소", "", redColor, redColor, false),
            "c4" to OrderState("고객요청취소", "", redColor, redColor, false),
            // Return state
            "br" to OrderState("반품신청", "", redColor, redColor, false),
            "d1bn" to OrderState("반품거절 • ", "배송중", grayColor, purpleColor, true),
            "d2bn" to OrderState("반품거절 • ", "배송완료", grayColor, purpleColor, true),
            "s1bn" to OrderState("반품거절 • ", "구매확정", grayColor, grayColor, false),
            "b1" to OrderState("반품접수", "", redColor, redColor, false),
            "b2" to OrderState("반송중", "", purpleColor, purpleColor, true),
            "b3" to OrderState("반품보류", "", redColor, redColor, false),
            "b4" to OrderState("반품회수완료", "", redColor, redColor, false),
            // Change state
            "er" to OrderState("교환신청", "", redColor, redColor, false),
            "p1en" to OrderState("교환거절 • ", "결제완료", grayColor, grayColor, false),
            "g1en" to OrderState("교환거절 • ", "상품준비중", grayColor, grayColor, false),
            "d1en" to OrderState("교환거절 • ", "배송중", grayColor, purpleColor, true),
            "d2en" to OrderState("교환거절 • ", "배송완료", grayColor, purpleColor, true),
            "s1en" to OrderState("교환거절 • ", "구매확정", grayColor, grayColor, false),
            "e1" to OrderState("교환접수", "", redColor, redColor, false),
            "e2" to OrderState("반송중", "", redColor, redColor, false),
            "e3" to OrderState("재배송중", "", purpleColor, purpleColor, true),
            "e4" to OrderState("교환보류", "", redColor, redColor, false),
            "e5" to OrderState("교환완료", "", redColor, redColor, false),
            "z1" to OrderState("추가입금대기", "", redColor, redColor, false),
            "z2" to OrderState("추가결제완료", "", redColor, redColor, false),
            "z3" to OrderState("추가배송중", "", purpleColor, purpleColor, true),
            "z4" to OrderState("추가배송완료", "", purpleColor, purpleColor, true),
            "z5" to OrderState("교환추가완료", "", redColor, redColor, false),
            // Refund state
            "rr" to OrderState("환불신청", "", redColor, redColor, false),
            "p1rn" to OrderState("환불거절 • ", "결제완료", grayColor, grayColor, false),
            "g1rn" to OrderState("환불거절 • ", "상품준비중", grayColor, grayColor, false),
            "d1rn" to OrderState("환불거절 • ", "배송중", grayColor, purpleColor, true),
            "d2rn" to OrderState("환불거절 • ", "배송완료", grayColor, purpleColor, true),
            "s1rn" to OrderState("환불거절 • ", "구매확정", grayColor, grayColor, false),

            "r1" to OrderState("환불접수", "", redColor, redColor, false),
            "r2" to OrderState("환불보류", "", redColor, redColor, false),
            "r3" to OrderState("환불완료", "", redColor, redColor, false),
            // Etc...
            "f2" to OrderState("고객결제중단", "", redColor, redColor, false),
            "f3" to OrderState("결제실패", "", redColor, redColor, false)
    )
    data class OrderState(val str: String,
                          val str2: String,
                          val fontColor: Int,
                          val fontColor2: Int,
                          val needArrow: Boolean)
}