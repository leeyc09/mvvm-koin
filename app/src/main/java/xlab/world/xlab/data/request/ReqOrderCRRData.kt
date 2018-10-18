package xlab.world.xlab.data.request

import com.google.gson.annotations.SerializedName
import okhttp3.MultipartBody
import okhttp3.RequestBody
import xlab.world.xlab.utils.support.PrintLog
import java.io.Serializable

data class ReqOrderCRRData(private val builder: MultipartBody.Builder =
                                  MultipartBody.Builder().setType(MultipartBody.FORM)): Serializable {
    private val tag = "ReqProfileUpdateData"

    fun addMode(mode: String) {
        builder.addFormDataPart("mode", mode)
    }
    fun addOrderNo(orderNo: String) {
        builder.addFormDataPart("orderNo", orderNo)
    }
    fun addClaimGoods(goodsNo: String, goodsCnt: Int) {
        builder.addFormDataPart("orderGoodsNo", goodsNo)
        builder.addFormDataPart("claimGoodsCnt", goodsCnt.toString())
    }
    fun addReason(reason: String) {
        builder.addFormDataPart("userHandleReason", reason)
    }
    fun addDetailReason(detailReason: String) {
        builder.addFormDataPart("userHandleDetailReason", detailReason)
    }
    fun addBankName(bankName: String) {
        builder.addFormDataPart("userRefundBankName", bankName)
    }
    fun addBankAccount(bankAccount: String) {
        builder.addFormDataPart("userRefundAccountNumber", bankAccount)
    }
    fun addAccountHolder(accountHolder: String) {
        builder.addFormDataPart("userRefundDepositor", accountHolder)
    }

    fun getReqBody(): RequestBody = builder.build()
}