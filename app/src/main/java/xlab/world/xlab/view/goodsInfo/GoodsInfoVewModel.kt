package xlab.world.xlab.view.goodsInfo

import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.view.View
import io.reactivex.Observable
import xlab.world.xlab.R
import xlab.world.xlab.utils.rx.SchedulerProvider
import xlab.world.xlab.utils.rx.with
import xlab.world.xlab.utils.support.AppConstants
import xlab.world.xlab.view.AbstractViewModel

class GoodsInfoVewModel(private val scheduler: SchedulerProvider): AbstractViewModel() {
    private val viewModelTag = "GoodsInfo"

    val uiData = MutableLiveData<UIModel>()

    fun necessaryPhoneLayout(deliveryNo: String) {
        launch {
            Observable.create<ArrayList<Int>> {
                val layoutVisibility = ArrayList<Int>()
                if (deliveryNo == AppConstants.CS_HOLAPET) {
                    layoutVisibility.add(View.VISIBLE) // 올라펫 레이아웃
                    layoutVisibility.add(View.GONE) // 슬랩 레이아웃
                } else if (deliveryNo == AppConstants.CS_XLAB) {
                    layoutVisibility.add(View.GONE) // 올라펫 레이아웃
                    layoutVisibility.add(View.VISIBLE) // 슬랩 레이아웃
                }

                it.onNext(layoutVisibility)
                it.onComplete()
            }.with(scheduler).subscribe {
                if (it.size > 1) {
                    uiData.value = UIModel(holapetPhoneVisibility = it[0])
                    uiData.value = UIModel(xlabPhoneVisibility = it[1])
                }
            }
        }
    }

    fun deliveryLayout(context: Context, deliveryNo: String) {
        launch {
            Observable.create<ArrayList<String>> {
                val deliveryText = ArrayList<String>()
                if (deliveryNo == AppConstants.CS_HOLAPET) {
                    deliveryText.add(context.getString(R.string.holapet_shop)) // 반품지명
                    deliveryText.add(context.getString(R.string.holapet_return_address)) // 반품 주소
                    deliveryText.add(context.getString(R.string.esc_company_phone_num)) // 반품 연락처
                    deliveryText.add(context.getString(R.string.delivery_company_cj)) // 택배사
                    deliveryText.add(context.getString(R.string.holapet_return_info1)) // 교환 상세
                } else if (deliveryNo == AppConstants.CS_XLAB) {
                    deliveryText.add(context.getString(R.string.boomyung)) // 반품지명
                    deliveryText.add(context.getString(R.string.xlab_return_address)) // 반품 주소
                    deliveryText.add(context.getString(R.string.xlab_return__phone)) // 반품 연락처
                    deliveryText.add(context.getString(R.string.delivery_company_logen)) // 택배사
                    deliveryText.add(context.getString(R.string.xlab_return_info1)) // 교환 상세
                }

                it.onNext(deliveryText)
                it.onComplete()
            }.with(scheduler).subscribe {
                if (it.size > 4) {
                    uiData.value = UIModel(returnName = it[0])
                    uiData.value = UIModel(returnAddress = it[1])
                    uiData.value = UIModel(returnPhone = it[2])
                    uiData.value = UIModel(deliveryCompany = it[3])
                    uiData.value = UIModel(returnInfo = it[4])
                }
            }
        }
    }

    fun inquiryLayout(context: Context, deliveryNo: String) {
        launch {
            Observable.create<ArrayList<Any>> {
                val inquiryLayout = ArrayList<Any>()
                if (deliveryNo == AppConstants.CS_HOLAPET) {
                    inquiryLayout.add(View.VISIBLE)
                    inquiryLayout.add(context.getString(R.string.holapet_shop)) // 문의 회사
                    inquiryLayout.add(context.getString(R.string.esc_company_phone_num)) // 문의 번호
                    inquiryLayout.add(context.getString(R.string.holapet_shop)) // 문의 회사
                    inquiryLayout.add(context.getString(R.string.holapet_kakao_id))
                } else if (deliveryNo == AppConstants.CS_XLAB) {
                    inquiryLayout.add(View.GONE)
                    inquiryLayout.add(context.getString(R.string.xlab_company)) // 문의 회사
                    inquiryLayout.add(context.getString(R.string.xlab_cs_num)) // 문의 번호
                    inquiryLayout.add(context.getString(R.string.xlab_company))  // 문의 회사
                    inquiryLayout.add(context.getString(R.string.xlab_kakao_id))
                }

                it.onNext(inquiryLayout)
                it.onComplete()
            }.with(scheduler).subscribe {
                if (it.size > 4) {
                    uiData.value = UIModel(holapetRequestVisibility = it[0] as Int)
                    uiData.value = UIModel(companyName = it[1] as String)
                    uiData.value = UIModel(companyPhone = it[2] as String)
                    uiData.value = UIModel(companyKaKao = it[3] as String)
                    uiData.value = UIModel(kakaoId = it[4] as String)
                }
            }
        }
    }
}

data class UIModel(val isLoading: Boolean? = null, val toastMessage: String? = null,
                   val holapetPhoneVisibility: Int? = null, val xlabPhoneVisibility: Int? = null,
                   val returnName: String? = null, val returnAddress: String? = null,
                   val returnPhone: String? = null, val deliveryCompany: String? = null,
                   val returnInfo: String? = null,
                   val holapetRequestVisibility: Int? = null, val companyName: String? = null,
                   val companyPhone: String? = null, val companyKaKao: String? = null,
                   val kakaoId: String? = null)
