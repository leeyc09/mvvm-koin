package xlab.world.xlab.view.onBoarding

import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.view.View
import io.reactivex.Observable
import xlab.world.xlab.R
import xlab.world.xlab.utils.rx.SchedulerProvider
import xlab.world.xlab.utils.rx.with
import xlab.world.xlab.utils.span.FontForegroundColorSpan
import xlab.world.xlab.utils.support.PrintLog
import xlab.world.xlab.view.AbstractViewModel

class OnBoardingViewModel(private val scheduler: SchedulerProvider): AbstractViewModel() {
    val tag = "OnBoarding"

    val uiData = MutableLiveData<UIModel>()

    // view pager 위치에 따른 버튼 변경
    fun buttonVisibility(index: Int, pagerSize: Int) {
        launch {
            Observable.create<ArrayList<Int>> {
                // 첫페이지 ~ 마지막 이전 페이지 -> 건너뛰기 활성화 & 시작하기 비활성화
                // 마지막 페이지 -> 시작하기 버튼 활성화
                val btnVisibility = // [0] -> 건너뛰기, [1] -> 시작하기
                        if (index < pagerSize - 1) arrayListOf(View.VISIBLE, View.INVISIBLE)
                        else arrayListOf(View.INVISIBLE, View.VISIBLE)

                it.onNext(btnVisibility)
                it.onComplete()
            }.with(scheduler = scheduler).subscribe {
                PrintLog.d(title = "buttonVisibility", log = it.toString(), tag = tag)
                uiData.value = UIModel(skipBtnVisibility = it[0], startBtnVisibility = it[1])
            }
        }
    }

    // 내용 업데이트 이벤트
    fun contentTextSet(context: Context, index: Int,
                       boldFont: FontForegroundColorSpan, regularFont: FontForegroundColorSpan) {
        launch {
            Observable.create<SpannableString> {
                // 페이지에 따른 내용 폰트 적용
                val spannableString = when (index){
                    0 -> {
                        val textStr = SpannableString(context.getString(R.string.onBoarding1))
                        textStr.setSpan(boldFont, 0, 8, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                        textStr.setSpan(regularFont, 8, textStr.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                        textStr
                    }
                    1 -> {
                        val textStr = SpannableString(context.getString(R.string.onBoarding2))
                        textStr.setSpan(regularFont, 0, 12, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                        textStr.setSpan(boldFont, 12, 30, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                        textStr.setSpan(regularFont, 30, textStr.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                        textStr
                    }
                    2 -> {
                        val textStr = SpannableString(context.getString(R.string.onBoarding3))
                        textStr.setSpan(boldFont, 0, 10, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                        textStr.setSpan(regularFont, 10, textStr.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                        textStr
                    }
                    else -> {
                        SpannableString("")
                    }
                }

                it.onNext(spannableString)
                it.onComplete()
            }.with(scheduler).subscribe { contentStr ->
                uiData.value = UIModel(contentStr = contentStr)
            }
        }
    }
}

data class UIModel(val skipBtnVisibility: Int? = null, val startBtnVisibility: Int? = null,
                   val contentStr: SpannableString? = null)
