package xlab.world.xlab.utils.listener

import android.app.Activity
import android.support.v4.app.FragmentManager
import android.support.v4.content.res.ResourcesCompat
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.view.View
import org.koin.android.ext.android.inject
import xlab.world.xlab.R
import xlab.world.xlab.utils.support.AppConstants
import xlab.world.xlab.utils.support.RunActivity
import xlab.world.xlab.utils.support.SPHelper
import xlab.world.xlab.utils.view.dialog.DefaultDialog
import xlab.world.xlab.utils.view.dialog.DialogCreator
import xlab.world.xlab.utils.view.hashTag.HashTagHelper
import xlab.world.xlab.utils.view.toast.DefaultToast

class DefaultListener(private val context: Activity) {

    private val spHelper: SPHelper by context.inject()

    private val suggestAddTopicDialog = DialogCreator.suggestAddTopicDialog(context = context)

    // 이용 약관 터치
    val clausePolicyListener = object: ClickableSpan() {
        override fun onClick(widget: View?) {
            RunActivity.defaultWebViewActivity(context = context, pageTitle = context.getString(R.string.inquiry_under3),
                    webUrl =  AppConstants.LOCAL_HTML_URL + "clause.html", zoomControl = true)
        }

        override fun updateDrawState(ds: TextPaint) {
            super.updateDrawState(ds)
            ds.color = ResourcesCompat.getColor(context.resources, R.color.color6D6D6D, null)
        }
    }

    // 개인 정보 약관 터치
    val personalInfoPolicyListener = object: ClickableSpan() {
        override fun onClick(widget: View?) {
            RunActivity.defaultWebViewActivity(context = context, pageTitle = context.getString(R.string.inquiry_under2),
                    webUrl =  AppConstants.LOCAL_HTML_URL + "personalInfo.html", zoomControl = true)
        }

        override fun updateDrawState(ds: TextPaint) {
            super.updateDrawState(ds)
            ds.color = ResourcesCompat.getColor(context.resources, R.color.color6D6D6D, null)
        }
    }

    // 매칭률 ? 클릭
    val questionMatchListener = View.OnClickListener {
        val defaultToast = DefaultToast(context = context)
        if (spHelper.accessToken == "") { // guest mode
            defaultToast.showToast(context.resources.getString(R.string.question_match_toast))
        } else {
            suggestAddTopicDialog.show()
        }
    }

    // 포스트 디테일
    val postListener = View.OnClickListener { view ->
        if (view.tag is String) {
            RunActivity.postDetailActivity(context = context, postId = view.tag as String, goComment = false)
        }
    }

    // 상품 상세
    val goodsListener = View.OnClickListener { view ->
        if (view.tag is String) {
            RunActivity.goodsDetailActivity(context = context, goodsCd = view.tag as String)
        }
    }

    // 프로필 화면
    val profileListener = View.OnClickListener { view ->
        if (view.tag is String) {
            RunActivity.profileActivity(context = context, userId = view.tag as String)
        }
    }

    // 해시태그 포스트
    val hashTagListener = object : HashTagHelper.ClickListener {
        override fun onHashTagClicked(hashTag: String) {
            RunActivity.hashTagPostActivity(context = context, hashTag = hashTag)
        }
    }

    // 포스트 댓글
    val commentsListener = View.OnClickListener { view ->
        if (view.tag is String) {
            RunActivity.postCommentActivity(context = context, postId = view.tag as String)
        }
    }

    // 포스트 -> 포스트 댓글 바로가기
    val commentsFromPostListener = View.OnClickListener { view ->
        if (view.tag is String) {
            RunActivity.postDetailActivity(context = context, postId = view.tag as String, goComment = true)
        }
    }
//    private val loginDialog = DefaultDialog(activity,
//            activity.resources.getString(R.string.is_login_service),
//            activity.resources.getString(R.string.is_login_service2),
//            object: DefaultDialog.Listener {
//                override fun onOkayTouch(tag: Any?) {
//                    val intent = LoginActivity.newIntent(activity, false, null)
//                    activity.startActivityForResult(intent, RequestCodeData.LOGIN_USER)
//                }
//            })
//

//
//    // 로그인하기
//    val loginDialogListener = object: DefaultDialog.DefaultDialogListener {
//        override fun onOkayTouch(tag: Any?) {
//            val intent = LoginActivity.newIntent(activity, false, null)
//            activity.startActivityForResult(intent, RequestCodeData.LOGIN_USER)
//        }
//    }
//
//    // 프로필 수정
//    val profileEditListener = View.OnClickListener {
//        // login check
//        if (SPHelper(activity).accessToken == "") { // guest mode
//            loginDialog.show()
//        } else {
//            val intent = ProfileEditActivity.newIntent(activity)
//            activity.startActivityForResult(intent, RequestCodeData.PROFILE_EDIT)
//        }
//    }
//
//    // 토픽 설정
//    val topicSettingListener = View.OnClickListener {
//        // login check
//        if (SPHelper(activity).accessToken == "") { // guest mode
//            loginDialog.show()
//        } else {
//            val intent = TopicSettingActivity.newIntent(activity)
//            activity.startActivityForResult(intent, RequestCodeData.TOPIC_SETTING)
//        }
//    }
//
//
//
//    // 장바구니
//    val myCartListener = View.OnClickListener {
//        // login check
//        if (SPHelper(activity).accessToken == "") { // guest mode
//            loginDialog.show()
//        } else {
//            val intent = MyCartActivity.newIntent(activity)
//            activity.startActivityForResult(intent, RequestCodeData.MY_CART)
//        }
//    }
//
//    // 상품 문의
//    val goodsInquiryListener = View.OnClickListener { view ->
//        if (view.tag is String) {
//            val intent = GoodsInquiryInfoActivity.newIntent(activity, view.tag as String)
//            activity.startActivity(intent)
//        }
//    }
//
//    // 쇼핑 정보 수정
//    val myShopInfoEditListener = View.OnClickListener {
//        // login check
//        if (SPHelper(activity).accessToken == "") { // guest mode
//            loginDialog.show()
//        } else {
//            val intent = EditShopInfoActivity.newIntent(activity)
//            activity.startActivityForResult(intent, RequestCodeData.MY_SHOP_PROFILE_EDIT)
//        }
//    }
//
//    // 주문 상세 화면
//    val orderDetailListener = View.OnClickListener { view ->
//        // login check
//        if (SPHelper(activity).accessToken == "") { // guest mode
//            loginDialog.show()
//        } else {
//            if (view.tag is String) {
//                val orderNo = view.tag as String
//                val intent = OrderDetailActivity.newIntent(activity, orderNo)
//                activity.startActivityForResult(intent, RequestCodeData.ORDER_DETAIL)
//            }
//        }
//    }
//
//    // 최근 본 상품
//    val recentViewGoodsListener = View.OnClickListener {
//        val intent = RecentViewGoodsActivity.newIntent(activity)
//        activity.startActivityForResult(intent, RequestCodeData.RECENT_VIEW_GOODS)
//    }
//
//    // 배송 추적
//    val deliverTrackingListener = View.OnClickListener { view ->
//        if (view.tag is String) {
//            val deliverNo = view.tag as String
//            val intent = DefaultWebViewActivity.newIntent(activity,
//                    pageTitle = null, webUrl = SupportData.TRACKING_DELIVER_URL + deliverNo, zoomControl = true)
//            activity.startActivity(intent)
//        }
//    }
}