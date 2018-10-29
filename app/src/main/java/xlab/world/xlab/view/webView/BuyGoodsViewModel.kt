package xlab.world.xlab.view.webView

import android.app.Activity
import android.arch.lifecycle.MutableLiveData
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import android.provider.Browser
import android.view.View
import io.reactivex.Observable
import xlab.world.xlab.server.ApiURL
import xlab.world.xlab.server.provider.ApiGodoProvider
import xlab.world.xlab.utils.asyncTask.DownloadFileTask
import xlab.world.xlab.utils.rx.SchedulerProvider
import xlab.world.xlab.utils.rx.with
import xlab.world.xlab.utils.support.AppConstants
import xlab.world.xlab.utils.support.NetworkCheck
import xlab.world.xlab.utils.support.PrintLog
import xlab.world.xlab.view.AbstractViewModel
import xlab.world.xlab.view.SingleLiveEvent
import java.io.File
import java.net.URISyntaxException
import java.net.URLEncoder

class BuyGoodsViewModel(private val apiGodo: ApiGodoProvider,
                        private val networkCheck: NetworkCheck,
                        private val scheduler: SchedulerProvider): AbstractViewModel() {
    private val viewModelTag = "BuyGoods"

    private var intentFrom: Int = AppConstants.FROM_GOODS_DETAIL

    private var isLogin = true
    private var snoText = ""
    private var snoList = ArrayList<Int>()


    val pageLoadingEventData = SingleLiveEvent<PageLoadingModel>()
    val uiData = MutableLiveData<UIModel>()

    fun setBuyGoodsData(snoList: ArrayList<Int>, intentFrom: Int) {
        launch {
            Observable.create<String> {
                this.snoList = snoList
                this.intentFrom = intentFrom
                snoText = "["
                for ((index, sno) in snoList.withIndex()) {
                    snoText += sno.toString()
                    snoText +=
                            if (index < snoList.size - 1) ","
                            else  "]"
                }
                it.onNext(ApiURL.GODO_ORDER_PAGE + snoText)
                it.onComplete()
            }.with(scheduler).subscribe {
                // 프로그래스바 시작
                PrintLog.d("webViewLoadUrl", it, viewModelTag)
                uiData.value = UIModel(isLoading = true, webViewLoadUrl = it, webViewVisibility = View.GONE)
            }
        }
    }

    fun webViewPageStarted(authorization: String, url: String) {
        launch {
            Observable.create<Int> {
                if (url.contains(ApiURL.GODO_CART_PAGE) || url.contains(ApiURL.GODO_MAIN_PAGE)) { // 장바구니, 메인 (웹)페이지 준비 -> 구매화면 종료
                    backBtnAction(authorization = authorization)
                } else if (url.contains(ApiURL.GODO_BUY_GOODS_FINISH_WITH_ORDER_NO_PAGE)) { // 구매 완료 페이지 준비 -> 웹뷰 안보이게
                    uiData.postValue(UIModel(webViewVisibility = View.GONE))
                } else {}

                it.onComplete()
            }.with(scheduler).subscribe {}
        }
    }

    fun webViewPageLoading(authorization: String, userId: String,
                           context: Context, url: String): Boolean {
        if ((url.startsWith("http://") || url.startsWith("https://")) && url.endsWith(".apk")) { // 설치파일 url 링크
            downloadFile(url)
            return false
        } else if ((url.startsWith("http://") || url.startsWith("https://")) && (url.contains("market.android.com") || url.contains("m.ahnlab.com/kr/site/download"))) {
            pageLoadingEventData.postValue(PageLoadingModel(intentData = IntentData(uri = Uri.parse(url))))
            return true
        } else if (url.startsWith("http:") || url.startsWith("https:")) { // http / https 요청
            when (true) {
                url.contains(ApiURL.GODO_LOGIN_PAGE) -> { // 로그인 페이지 로딩 -> 로그인 시도
                    isLogin = false
                    uiData.postValue(UIModel(webViewVisibility = View.GONE))
                    val postData: String = "loginId=" + URLEncoder.encode(userId, "UTF-8")
                    pageLoadingEventData.postValue(PageLoadingModel(
                            webViewPostData = WebViewPostData(url = ApiURL.SHOP_LOGIN, postData = postData)))
                }
                url.contains(ApiURL.GODO_BUY_GOODS_FINISH_WITH_ORDER_NO_PAGE) -> { // 구매 완료 페이지 로딩 -> 구매 화면 종료
                    val orderNo = url.replace(ApiURL.GODO_BUY_GOODS_FINISH_WITH_ORDER_NO_PAGE, "")
                    PrintLog.d("original url", url, viewModelTag)
                    PrintLog.d("replace url", ApiURL.GODO_BUY_GOODS_FINISH_WITH_ORDER_NO_PAGE, viewModelTag)
                    PrintLog.d("orderNo", orderNo, viewModelTag)
                    if (intentFrom == AppConstants.FROM_GOODS_DETAIL) { // 상품 상세에서 넘어온 경우 -> 장바구니에서 해당 상품 삭제 필요
                        deleteCartData(authorization = authorization, sno = snoList.first().toString()) {
                            pageLoadingEventData.postValue(PageLoadingModel(finishOrderNo = orderNo))
                            uiData.postValue(UIModel(resultCode = Activity.RESULT_OK))
                        }
                    } else {
                        pageLoadingEventData.postValue(PageLoadingModel(finishOrderNo = orderNo))
                        uiData.postValue(UIModel(resultCode = Activity.RESULT_OK))
                    }
                }
                else -> { // 이외 -> 웹뷰 활성화 & 해당 주소로 웹뷰 이동
                    uiData.postValue(UIModel(webViewVisibility = View.VISIBLE, webViewLoadUrl = url))
                }
            }
        } else if ((url.contains("vguard") || url.contains("droidxantivirus") || url.contains("smhyundaiansimclick://")
                        || url.contains("smshinhanansimclick://") || url.contains("smshinhancardusim://") || url.contains("smartwall://") || url.contains("appfree://")
                        || url.contains("v3mobile") || url.endsWith(".apk") || url.contains("market://") || url.contains("ansimclick")
                        || url.contains("market://details?id=com.shcard.smartpay") || url.contains("shinhan-sr-ansimclick://"))) {
            return callApp(context = context, url = url)
        } else if (url.startsWith("smartxpay-transfer://")) {
            val isInstallFlag = isPackageInstalled(context = context, packageName = "kr.co.uplus.ecredit")
            if (isInstallFlag) {
                pageLoadingEventData.postValue(PageLoadingModel(
                        intentData = IntentData(uri = Uri.parse(url),
                                category = Intent.CATEGORY_BROWSABLE,
                                browser = Browser.EXTRA_APPLICATION_ID)))
                return true
            } else {
                pageLoadingEventData.postValue(PageLoadingModel(
                        intentData = IntentData(uri = Uri.parse("market://details?id=kr.co.uplus.ecredit"),
                                category = Intent.CATEGORY_BROWSABLE,
                                browser = Browser.EXTRA_APPLICATION_ID),
                        needOverridePendingTransition = true))
            }
        } else if (url.startsWith("ispmobile://")) {
            val isInstallFlag = isPackageInstalled(context = context, packageName = "kvp.jjy.MispAndroid320")
            if (isInstallFlag) run {
                pageLoadingEventData.postValue(PageLoadingModel(
                        intentData = IntentData(uri = Uri.parse(url),
                                category = Intent.CATEGORY_BROWSABLE,
                                browser = Browser.EXTRA_APPLICATION_ID)))
                return true
            } else {
                uiData.postValue(UIModel(webViewLoadUrl = "http://mobile.vpay.co.kr/jsp/MISP/andown.jsp"))
            }
        } else if (url.startsWith("paypin://")) {
            val isInstallFlag = isPackageInstalled(context = context, packageName = "com.skp.android.paypin")
            if (isInstallFlag) run {
                pageLoadingEventData.postValue(PageLoadingModel(
                        intentData = IntentData(uri = Uri.parse(url),
                                category = Intent.CATEGORY_BROWSABLE,
                                browser = Browser.EXTRA_APPLICATION_ID)))
                return true
            } else {
                pageLoadingEventData.postValue(PageLoadingModel(
                        intentData = IntentData(uri = Uri.parse("market://details?id=com.skp.android.paypin&feature=search_result#?t=W251bGwsMSwxLDEsImNvbS5za3AuYW5kcm9pZC5wYXlwaW4iXQ.."),
                                category = Intent.CATEGORY_BROWSABLE,
                                browser = Browser.EXTRA_APPLICATION_ID),
                        needOverridePendingTransition = true))
                return true
            }
        } else if (url.startsWith("lguthepay://")) run {
            val isInstallFlag = isPackageInstalled(context = context, packageName = "com.lguplus.paynow")
            if (isInstallFlag) run {
                pageLoadingEventData.postValue(PageLoadingModel(
                        intentData = IntentData(uri = Uri.parse(url),
                                category = Intent.CATEGORY_BROWSABLE,
                                browser = Browser.EXTRA_APPLICATION_ID)))
                return true
            } else {
                pageLoadingEventData.postValue(PageLoadingModel(
                        intentData = IntentData(uri = Uri.parse("market://details?id=com.lguplus.paynow"),
                                category = Intent.CATEGORY_BROWSABLE,
                                browser = Browser.EXTRA_APPLICATION_ID),
                        needOverridePendingTransition = true))
                return true
            }
        } else {
            return callApp(context = context, url = url)
        }
        return false
    }

    fun webViewPageFinished(url: String, tIsLogin: Boolean) {
        launch {
            Observable.create<Int> {
                if (url == ApiURL.SHOP_LOGIN) { // 로그인 페이지 완료 -> 웹뷰 안보이게, 주문 페이지 다시 요청
                    uiData.postValue(UIModel(webViewLoadUrl = ApiURL.GODO_ORDER_PAGE + snoText))
                    it.onNext(View.GONE)
                } else if (url == ApiURL.GODO_ORDER_PAGE + snoText && tIsLogin) { //isLogin) { // 주문 페이지 -> 웹뷰 활성화, 프로그래스바 종료
                    uiData.postValue(UIModel(isLoading = false))
                    it.onNext(View.VISIBLE)
                }
                it.onComplete()
            }.with(scheduler).subscribe {
                uiData.value = UIModel(webViewVisibility = it)
            }
        }
    }

    private fun downloadFile(url: String) {
        DownloadFileTask{ apkFileName ->
            pageLoadingEventData.postValue(PageLoadingModel(fileName = apkFileName))
        }.execute(url)
    }

    private fun callApp(context: Context, url: String): Boolean {
        val intent: Intent
        try { // 인텐트 정합성 체크
            intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
        } catch (e: URISyntaxException) {
            return false
        }

        try {
            var retval = true
            if (url.startsWith("intent")) {
                // 앱설치 체크를 합니다.

                // 국민 앱카드가 설치 되지 않은 경우
                // 국민카드(+앱카드)로 변경 하여 호출 하고
                // 국민카드(+앱카드)도 없는 경우에 국민카드(+앱카드)마켓으로 이동
                if (context.packageManager.resolveActivity(intent, 0) == null && "com.kbcard.cxh.appcard" == intent?.`package`) {
                    intent.setPackage("com.kbcard.kbkookmincard") // 국민카드(+앱카드)로 변경
                    if (context.packageManager.resolveActivity(intent, 0) == null) {
                        val packageName = intent.`package`
                        if (packageName != null) {
                            val uri = Uri.parse("market://search?q=pname:$packageName")
                            pageLoadingEventData.postValue(PageLoadingModel(intentData = IntentData(uri = uri)))
                            retval = true
                        }
                    } else {
                        intent.addCategory(Intent.CATEGORY_BROWSABLE)
                        intent.component = null
                        pageLoadingEventData.postValue(PageLoadingModel(startActivityIfNeeded = intent))
                        retval = true
//                        try {
//                            if (startActivityIfNeeded(intent, -1)) {
//                                retval = true
//                            }
//                        }catch (e: ActivityNotFoundException) {
//                            retval = false
//                        }
                    }
                } else if (context.packageManager.resolveActivity(intent, 0) == null) { // 앱 미설치 시 마켓 이동
                    val packageName = intent.`package`
                    if (packageName != null) {
                        val uri = Uri.parse("market://search?q=pname:$packageName")
                        pageLoadingEventData.postValue(PageLoadingModel(intentData = IntentData(uri = uri)))
                        retval = true
                    }
                } else {
                    intent.addCategory(Intent.CATEGORY_BROWSABLE)
                    intent.component = null
                    pageLoadingEventData.postValue(PageLoadingModel(startActivityIfNeeded = intent))
                    retval = true
//                    try {
//                        if (startActivityIfNeeded(intent, -1)) {
//                            retval = true
//                        }
//                    }catch (e: ActivityNotFoundException) {
//                        retval = false
//                    }
                }
            } else { // 구 방식
                val uri = Uri.parse(url)
                pageLoadingEventData.postValue(PageLoadingModel(intentData = IntentData(uri = uri)))
                retval = true
            }
            return retval
        } catch (e: ActivityNotFoundException) {
            return false
        }
    }

    private fun isPackageInstalled(context: Context, packageName: String): Boolean {
        try {
            context.packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
        } catch (e: PackageManager.NameNotFoundException) {
            return false
        }
        return true
    }

    fun backBtnAction(authorization: String) {
        if (intentFrom == AppConstants.FROM_GOODS_DETAIL) { // 상품 상세에서 넘어온 경우 -> 장바구니에서 해당 상품 삭제 필요
            deleteCartData(authorization = authorization, sno = snoList.first().toString()) {
                uiData.postValue(UIModel(resultCode = Activity.RESULT_CANCELED))
            }
        } else {
            uiData.postValue(UIModel(resultCode = Activity.RESULT_CANCELED))
        }
    }

    private fun deleteCartData(authorization: String, sno: String, success: (Boolean) -> Unit) {
        // 네트워크 연결 확인
        if (!networkCheck.isNetworkConnected()) {
            uiData.postValue(UIModel(toastMessage = networkCheck.networkErrorMsg))
            return
        }

        uiData.value = UIModel(isLoading = true)
        launch {
            apiGodo.requestDeleteCart(scheduler = scheduler, authorization = authorization, sno = sno,
                    responseData = {
                        uiData.value = UIModel(isLoading = false)
                        success(true)
                    },
                    errorData = { errorData ->
                        uiData.value = UIModel(isLoading = false)
                        errorData?.let {
                            PrintLog.e("requestDeleteCart fail", errorData.message, viewModelTag)
                        }
                    })
        }
    }
}

data class WebViewPostData(val url: String, val postData: String)
data class IntentData(val uri: Uri, val category: String? = null, val browser: String? = null)
data class PageLoadingModel(val fileName: String? = null, val webViewPostData: WebViewPostData? = null,
                            val intentData: IntentData? = null, val startActivityIfNeeded: Intent? = null,
                            val needOverridePendingTransition: Boolean? = null,
                            val finishOrderNo: String? = null)
data class UIModel(val isLoading: Boolean? = null, val toastMessage: String? = null, val resultCode: Int? = null,
                   val webViewLoadUrl: String? = null, val webViewVisibility: Int? = null)
