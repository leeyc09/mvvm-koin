package xlab.world.xlab.data.response

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ResCRRDetailData(@SerializedName("message") val message: String,
                            @SerializedName("userHandleMode") val userHandleMode: String, // 처리 모드 (r 환불, b 반품, e 교환)
                            @SerializedName("userHandleFl") val userHandleFl: String, // 처리 상태 (y 승인, n 거절, r 신청)
                            @SerializedName("reason") val reason: String,
                            @SerializedName("memo") val memo: String,
                            @SerializedName("refundInfo") val refundInfo: String, // 환불 정보
                            @SerializedName("adminReason") val adminReason: String): Serializable