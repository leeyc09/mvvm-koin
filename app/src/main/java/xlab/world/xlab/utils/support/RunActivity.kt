package xlab.world.xlab.utils.support

import android.app.Activity
import android.net.Uri
import com.google.android.youtube.player.YouTubeStandalonePlayer
import xlab.world.xlab.R
import xlab.world.xlab.data.adapter.SelectUsedGoodsListData
import xlab.world.xlab.data.response.ResUserLoginData
import xlab.world.xlab.view.cart.CartActivity
import xlab.world.xlab.view.comment.CommentActivity
import xlab.world.xlab.view.completePurchase.CompletePurchaseActivity
import xlab.world.xlab.view.follow.FollowerActivity
import xlab.world.xlab.view.follow.FollowingActivity
import xlab.world.xlab.view.galleryImageSelect.GalleryImageSelectActivity
import xlab.world.xlab.view.goodsDetail.GoodsDetailActivity
import xlab.world.xlab.view.goodsInfo.GoodsDeliveryActivity
import xlab.world.xlab.view.goodsInfo.GoodsInquiryActivity
import xlab.world.xlab.view.goodsInfo.GoodsNecessaryActivity
import xlab.world.xlab.view.login.LoginActivity
import xlab.world.xlab.view.main.MainActivity
import xlab.world.xlab.view.myShopping.MyShoppingActivity
import xlab.world.xlab.view.myShopping.ShopProfileEditActivity
import xlab.world.xlab.view.notice.NoticeActivity
import xlab.world.xlab.view.notification.NotificationActivity
import xlab.world.xlab.view.onBoarding.OnBoardingActivity
import xlab.world.xlab.view.orderDetail.OrderDetailActivity
import xlab.world.xlab.view.petBreed.PetBreedActivity
import xlab.world.xlab.view.postDetail.PostDetailActivity
import xlab.world.xlab.view.posts.GoodsTaggedPostsActivity
import xlab.world.xlab.view.posts.LikedPostsActivity
import xlab.world.xlab.view.posts.SavedPostsActivity
import xlab.world.xlab.view.postsUpload.content.PostUploadContentActivity
import xlab.world.xlab.view.postsUpload.filter.PostUploadFilterActivity
import xlab.world.xlab.view.postsUpload.goods.PostUploadUsedGoodsActivity
import xlab.world.xlab.view.postsUpload.picture.PostUploadPictureActivity
import xlab.world.xlab.view.profile.ProfileActivity
import xlab.world.xlab.view.profileEdit.ProfileEditActivity
import xlab.world.xlab.view.recentView.RecentViewGoodsActivity
import xlab.world.xlab.view.register.LocalRegisterActivity
import xlab.world.xlab.view.register.SocialRegisterActivity
import xlab.world.xlab.view.resetPassword.ResetPasswordActivity
import xlab.world.xlab.view.resetPassword.UpdatePasswordActivity
import xlab.world.xlab.view.search.*
import xlab.world.xlab.view.setting.SettingActivity
import xlab.world.xlab.view.topicDetail.TopicPetDetailActivity
import xlab.world.xlab.view.topicEdit.TopicPetEditActivity
import xlab.world.xlab.view.topicSetting.TopicSettingActivity
import xlab.world.xlab.view.webView.BuyGoodsWebViewActivity
import xlab.world.xlab.view.webView.DefaultWebViewActivity
import xlab.world.xlab.view.webView.PolicyActivity
import xlab.world.xlab.view.withdraw.WithdrawActivity

object RunActivity {
    private const val tag = "RunActivity"

    fun onBoarding(context: Activity) {
        val intent = OnBoardingActivity.newIntent(context = context)
        context.startActivity(intent)
    }

    fun loginActivity(context: Activity, isComePreLoadActivity: Boolean, linkData: Uri?) {
        val intent = LoginActivity.newIntent(context = context, isComePreLoadActivity = isComePreLoadActivity, linkData = linkData)
        context.startActivityForResult(intent, RequestCodeData.LOGIN_USER)
    }

    fun localRegisterActivity(context: Activity) {
        val intent = LocalRegisterActivity.newIntent(context = context)
        context.startActivityForResult(intent, RequestCodeData.REGISTER_USER)
    }

    fun socialRegisterActivity(context: Activity, userData: ResUserLoginData) {
        val intent = SocialRegisterActivity.newIntent(context = context, userData = userData)
        context.startActivityForResult(intent, RequestCodeData.REGISTER_USER)
    }

    fun defaultWebViewActivity(context: Activity, pageTitle: String?, webUrl: String, zoomControl: Boolean) {
        val intent = DefaultWebViewActivity.newIntent(context = context, pageTitle = pageTitle, webUrl = webUrl, zoomControl = zoomControl)
        context.startActivity(intent)
    }

    fun resetPasswordActivity(context: Activity, email: String) {
        val intent = ResetPasswordActivity.newIntent(context = context, email = email)
        context.startActivity(intent)
    }

    fun mainActivity(context: Activity, linkData: Uri?) {
        val intent = MainActivity.newIntent(context = context, linkData = linkData)
        context.startActivity(intent)
    }

    fun topicSettingActivity(context: Activity) {
        val intent = TopicSettingActivity.newIntent(context = context)
        context.startActivityForResult(intent, RequestCodeData.TOPIC_SETTING)
    }

    fun postDetailActivity(context: Activity, postId: String, goComment: Boolean) {
        val intent = PostDetailActivity.newIntent(context= context, postId = postId, goComment = goComment)
        context.startActivityForResult(intent, RequestCodeData.POST_DETAIL)
    }

    fun postCommentActivity(context: Activity, postId: String) {
        val intent = CommentActivity.newIntent(context = context, postId = postId)
        context.startActivityForResult(intent, RequestCodeData.POST_COMMENT)
    }

    fun profileActivity(context: Activity, userId: String) {
        val intent = ProfileActivity.newIntent(context = context, userId = userId)
        context.startActivityForResult(intent, RequestCodeData.PROFILE)
    }

    fun profileEditActivity(context: Activity) {
        val intent = ProfileEditActivity.newIntent(context = context)
        context.startActivityForResult(intent, RequestCodeData.PROFILE_EDIT)
    }

    fun galleryImageSelectActivity(context: Activity, withOverlay: Boolean) {
        val intent = GalleryImageSelectActivity.newIntent(context = context, withOverlay = withOverlay)
        context.startActivityForResult(intent, RequestCodeData.GALLARY_IMAGE_SELECT)
    }

    fun petEditActivity(context: Activity, petNo: Int?) {
        val intent = TopicPetEditActivity.newIntent(context = context, petNo = petNo)
        val requestCode = petNo?.let{RequestCodeData.TOPIC_EDIT}?:let{RequestCodeData.TOPIC_ADD}
        context.startActivityForResult(intent, requestCode)
    }

    fun petBreedActivity(context: Activity, petType: String) {
        val intent = PetBreedActivity.newIntent(context = context, petType = petType)
        context.startActivityForResult(intent, RequestCodeData.TOPIC_BREED)
    }

    fun petDetailActivity(context: Activity, userId: String, petNo: Int, petTotal: Int) {
        val intent = TopicPetDetailActivity.newIntent(context = context, userId = userId, petNo = petNo, petTotal = petTotal)
        context.startActivityForResult(intent, RequestCodeData.TOPIC_DETAIL)
    }

    fun goodsDetailActivity(context: Activity, goodsCode: String) {
        val intent = GoodsDetailActivity.newIntent(context = context, goodsCode = goodsCode)
        context.startActivityForResult(intent, RequestCodeData.GOODS_DETAIL)
    }

    fun goodsSearchActivity(context: Activity, searchText: String, searchCode: String) {
        val intent = GoodsSearchActivity.newIntent(context = context, searchText = searchText, searchCode = searchCode)
        context.startActivityForResult(intent, RequestCodeData.GOODS_SEARCH)
    }

    fun combinedSearchActivity(context: Activity) {
        val intent = CombinedSearchActivity.newIntent(context = context)
        context.startActivityForResult(intent, RequestCodeData.COMBINED_SEARCH)
    }

    fun searchHashTagPostActivity(context: Activity, searchTag: String) {
        val intent = SearchHashTagPostsActivity.newIntent(context = context, searchTag = searchTag)
        context.startActivityForResult(intent, RequestCodeData.TAG_POST)
    }

    fun notificationActivity(context: Activity) {
        val intent = NotificationActivity.newIntent(context = context)
        context.startActivityForResult(intent, RequestCodeData.NOTIFICATION)
    }

    fun postUploadPictureActivity(context: Activity, postId: String, youTubeVideoId: String) {
        val intent = PostUploadPictureActivity.newIntent(context = context, postId = postId, youTubeVideoId = youTubeVideoId)
        context.startActivityForResult(intent, RequestCodeData.POST_UPLOAD)
    }

    fun postUploadFilterActivity(context: Activity, postId: String, youTubeVideoId: String, imagePathList: ArrayList<String>) {
        val intent = PostUploadFilterActivity.newIntent(context = context, postId = postId, youTubeVideoId = youTubeVideoId, imagePath = imagePathList)
        context.startActivityForResult(intent, RequestCodeData.POST_UPLOAD)
    }

    fun postUploadContentActivity(context: Activity, postId: String, youTubeVideoId: String, imagePathList: ArrayList<String>) {
        val intent = PostUploadContentActivity.newIntent(context = context, postId = postId, youTubeVideoId = youTubeVideoId, imagePath = imagePathList)
        context.startActivityForResult(intent, RequestCodeData.POST_UPLOAD)
    }

    fun postUploadUsedGoodsActivity(context: Activity, selectedItem: ArrayList<SelectUsedGoodsListData>) {
        val intent = PostUploadUsedGoodsActivity.newIntent(context = context, selectedItem = selectedItem)
        context.startActivityForResult(intent, RequestCodeData.POST_UPLOAD)
    }

    fun savedPostActivity(context: Activity) {
        val intent = SavedPostsActivity.newIntent(context = context)
        context.startActivityForResult(intent, RequestCodeData.SAVED_POST)
    }

    fun likedPostActivity(context: Activity) {
        val intent = LikedPostsActivity.newIntent(context = context)
        context.startActivityForResult(intent, RequestCodeData.LIKED_POST)
    }

    fun updatePasswordActivity(context: Activity) {
        val intent = UpdatePasswordActivity.newIntent(context = context)
        context.startActivity(intent)
    }

    fun settingActivity(context: Activity) {
        val intent = SettingActivity.newIntent(context = context)
        context.startActivityForResult(intent, RequestCodeData.SETTING)
    }

    fun followerActivity(context: Activity, userId: String) {
        val intent = FollowerActivity.newIntent(context = context, userId = userId)
        context.startActivityForResult(intent, RequestCodeData.FOLLOW)
    }

    fun followingActivity(context: Activity, userId: String) {
        val intent = FollowingActivity.newIntent(context = context, userId = userId)
        context.startActivityForResult(intent, RequestCodeData.FOLLOW)
    }

    fun policyActivity(context: Activity) {
        val intent = PolicyActivity.newIntent(context = context)
        context.startActivity(intent)
    }

    fun noticeActivity(context: Activity) {
        val intent = NoticeActivity.newIntent(context = context)
        context.startActivityForResult(intent, RequestCodeData.NOTICE)
    }

    fun withdrawActivity(context: Activity) {
        val intent = WithdrawActivity.newIntent(context = context)
        context.startActivityForResult(intent, RequestCodeData.WITHDRAW)
    }

    fun youtubePlayerActivity(context: Activity, youTubeVideoId: String) {
        val intent = YouTubeStandalonePlayer.createVideoIntent(context, context.getString(R.string.app_api_key), youTubeVideoId)
        context.startActivity(intent)
    }

    fun searchBrandGoodsActivity(context: Activity, brandName: String, brandCode: String) {
        val intent = SearchBrandGoodsActivity.newIntent(context = context, brandName = brandName, brandCode = brandCode)
        context.startActivityForResult(intent, RequestCodeData.GOODS_BRAND_SEARCH)
    }

    fun goodsTaggedPostsActivity(context: Activity, goodsCode: String) {
        val intent = GoodsTaggedPostsActivity.newIntent(context = context, goodsCode = goodsCode)
        context.startActivityForResult(intent, RequestCodeData.GOODS_TAGGED_POST)
    }

    fun myShoppingActivity(context: Activity) {
        val intent = MyShoppingActivity.newIntent(context = context)
        context.startActivityForResult(intent, RequestCodeData.MY_SHOP)
    }

    fun shopProfileEditActivity(context: Activity) {
        val intent = ShopProfileEditActivity.newIntent(context = context)
        context.startActivityForResult(intent, RequestCodeData.MY_SHOP_PROFILE_EDIT)
    }

    fun goodsNecessaryActivity(context: Activity, deliveryNo: String, goodsName: String, origin: String, maker: String) {
        val intent = GoodsNecessaryActivity.newIntent(context = context, deliveryNo = deliveryNo,
                goodsName = goodsName, origin = origin, maker = maker)
        context.startActivity(intent)
    }

    fun goodsDeliveryActivity(context: Activity, deliveryNo: String) {
        val intent = GoodsDeliveryActivity.newIntent(context = context, deliveryNo = deliveryNo)
        context.startActivity(intent)
    }

    fun goodsInquiryActivity(context: Activity, deliveryNo: String) {
        val intent = GoodsInquiryActivity.newIntent(context = context, deliveryNo = deliveryNo)
        context.startActivity(intent)
    }

    fun recentViewGoodsActivity(context: Activity) {
        val intent = RecentViewGoodsActivity.newIntent(context = context)
        context.startActivityForResult(intent, RequestCodeData.RECENT_VIEW_GOODS)
    }

    fun cartActivity(context: Activity) {
        val intent = CartActivity.newIntent(context = context)
        context.startActivityForResult(intent, RequestCodeData.MY_CART)
    }

    fun buyGoodsWebViewActivity(context: Activity, snoList: ArrayList<Int>, from: Int) {
        val intent = BuyGoodsWebViewActivity.newIntent(context = context, snoList = snoList, from = from)
        context.startActivityForResult(intent, RequestCodeData.GOODS_BUYING)
    }

    fun completePurchaseActivity(context: Activity, orderNo: String) {
        val intent = CompletePurchaseActivity.newIntent(context = context, orderNo = orderNo)
        context.startActivityForResult(intent, RequestCodeData.COMPLETE_BUYING)
    }

    fun orderDetailActivity(context: Activity, orderNo: String) {
        val intent = OrderDetailActivity.newIntent(context = context, orderNo = orderNo)
        context.startActivityForResult(intent, RequestCodeData.ORDER_DETAIL)
    }
}