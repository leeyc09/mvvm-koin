package xlab.world.xlab.utils.support

import android.app.Activity
import android.net.Uri
import xlab.world.xlab.data.response.ResUserLoginData
import xlab.world.xlab.view.comment.CommentActivity
import xlab.world.xlab.view.follow.FollowerActivity
import xlab.world.xlab.view.follow.FollowingActivity
import xlab.world.xlab.view.galleryImageSelect.GalleryImageSelectActivity
import xlab.world.xlab.view.login.LoginActivity
import xlab.world.xlab.view.main.MainActivity
import xlab.world.xlab.view.onBoarding.OnBoardingActivity
import xlab.world.xlab.view.petBreed.PetBreedActivity
import xlab.world.xlab.view.postDetail.PostDetailActivity
import xlab.world.xlab.view.profile.ProfileActivity
import xlab.world.xlab.view.profileEdit.ProfileEditActivity
import xlab.world.xlab.view.register.LocalRegisterActivity
import xlab.world.xlab.view.register.SocialRegisterActivity
import xlab.world.xlab.view.resetPassword.ResetPasswordActivity
import xlab.world.xlab.view.topicDetail.TopicPetDetailActivity
import xlab.world.xlab.view.topicEdit.TopicPetEditActivity
import xlab.world.xlab.view.topicSetting.TopicSettingActivity
import xlab.world.xlab.view.webView.DefaultWebViewActivity

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
        PrintLog.d("Run", "PostComment", tag)
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

    fun hashTagPostActivity(context: Activity, hashTag: String) {
        PrintLog.d("Run", "HashTagPost", tag)
    }

    fun goodsDetailActivity(context: Activity, goodsCd: String) {
        PrintLog.d("Run", "GoodsDetail", tag)
    }

    fun searchGoodsActivity(context: Activity, searchText: String, searchCode: String) {
        PrintLog.d("Run", "SearchGoods", tag)
    }

    fun searchActivity(context: Activity) {
        PrintLog.d("Run", "Search", tag)
    }

    fun notificationActivity(context: Activity) {
        PrintLog.d("Run", "Notification", tag)
    }

    fun postUploadActivity(context: Activity) {
        PrintLog.d("Run", "PostUpload", tag)
    }

    fun savedPostActivity(context: Activity) {
        PrintLog.d("Run", "SavedPost", tag)
    }

    fun myShoppingActivity(context: Activity) {
        PrintLog.d("Run", "MyShopping", tag)
    }

    fun settingActivity(context: Activity) {
        PrintLog.d("Run", "Setting", tag)
    }

    fun followerActivity(context: Activity, userId: String) {
        val intent = FollowerActivity.newIntent(context = context, userId = userId)
        context.startActivityForResult(intent, RequestCodeData.FOLLOW)
    }

    fun followingActivity(context: Activity, userId: String) {
        val intent = FollowingActivity.newIntent(context = context, userId = userId)
        context.startActivityForResult(intent, RequestCodeData.FOLLOW)
    }
}