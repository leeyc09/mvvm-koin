package xlab.world.xlab.di

import xlab.world.xlab.utils.rx.ApplicationSchedulerProvider
import xlab.world.xlab.utils.rx.SchedulerProvider
import xlab.world.xlab.view.login.LoginViewModel
import org.koin.android.architecture.ext.viewModel
import org.koin.dsl.module.Module
import org.koin.dsl.module.applicationContext
import xlab.world.xlab.server.*
import xlab.world.xlab.server.`interface`.*
import xlab.world.xlab.server.provider.*
import xlab.world.xlab.utils.font.FontColorSpan
import xlab.world.xlab.utils.support.*
import xlab.world.xlab.view.cart.CartViewModel
import xlab.world.xlab.view.comment.CommentViewModel
import xlab.world.xlab.view.completePurchase.CompletePurchaseViewModel
import xlab.world.xlab.view.crrDetail.CRRDetailViewModel
import xlab.world.xlab.view.follow.UserViewModel
import xlab.world.xlab.view.galleryImageSelect.GalleryImageSelectViewModel
import xlab.world.xlab.view.goodsDetail.GoodsDetailViewModel
import xlab.world.xlab.view.goodsInfo.GoodsInfoVewModel
import xlab.world.xlab.view.goodsRating.GoodsRatingViewModel
import xlab.world.xlab.view.main.MainViewModel
import xlab.world.xlab.view.myShopping.MyShoppingViewModel
import xlab.world.xlab.view.notice.NoticeViewModel
import xlab.world.xlab.view.notification.NotificationViewModel
import xlab.world.xlab.view.onBoarding.OnBoardingViewModel
import xlab.world.xlab.view.orderCRR.OrderCRRViewModel
import xlab.world.xlab.view.orderDetail.OrderDetailViewModel
import xlab.world.xlab.view.orderStateDetail.OrderStateViewModel
import xlab.world.xlab.view.postDetail.PostDetailViewModel
import xlab.world.xlab.view.posts.PostsViewModel
import xlab.world.xlab.view.postsUpload.content.PostContentViewModel
import xlab.world.xlab.view.postsUpload.filter.ImageFilterViewModel
import xlab.world.xlab.view.postsUpload.goods.PostUsedGoodsViewModel
import xlab.world.xlab.view.preload.PreloadViewModel
import xlab.world.xlab.view.profile.ProfileViewModel
import xlab.world.xlab.view.profileEdit.ProfileEditViewModel
import xlab.world.xlab.view.recentView.RecentViewViewModel
import xlab.world.xlab.view.register.RegisterViewModel
import xlab.world.xlab.view.resetPassword.ResetPasswordViewModel
import xlab.world.xlab.view.search.SearchViewModel
import xlab.world.xlab.view.setting.SettingViewModel
import xlab.world.xlab.view.topicDetail.TopicPetDetailViewModel
import xlab.world.xlab.view.topicEdit.TopicPetEditViewModel
import xlab.world.xlab.view.topicSetting.TopicSettingViewModel
import xlab.world.xlab.view.webView.BuyGoodsViewModel
import xlab.world.xlab.view.withdraw.WithDrawViewModel

/**
 *  Koin main module
 */
val viewModelModule: Module = applicationContext {
    // ViewModel for Preload View
    viewModel { PreloadViewModel(scheduler = get()) }
    // ViewModel for OnBoarding View
    viewModel { OnBoardingViewModel(scheduler = get()) }
    // ViewModel for Login View
    viewModel { LoginViewModel(apiUser = get(), networkCheck = get(), scheduler = get()) }
    // ViewModel for Register View
    viewModel { RegisterViewModel(apiUser = get(), networkCheck = get(), scheduler = get()) }
    // ViewModel for Reset Password View
    viewModel { ResetPasswordViewModel(apiUser = get(), networkCheck = get(), scheduler = get()) }
    // ViewModel for Main(Feed) View
    viewModel { MainViewModel(apiPost = get(), apiShop = get(), networkCheck = get(), scheduler = get()) }
    // ViewModel for Topic Setting View
    viewModel { TopicSettingViewModel(apiPet = get(), networkCheck = get(), scheduler = get()) }
    // ViewModel for Post Detail View
    viewModel { PostDetailViewModel(apiPost = get(), apiUserActivity = get(), apiFollow = get(), networkCheck = get(), scheduler = get()) }
    // ViewModel for Comment View
    viewModel { CommentViewModel(apiPost = get(), apiComment = get(), networkCheck = get(), scheduler = get()) }
    // ViewModel for Profile View
    viewModel { ProfileViewModel(apiUser = get(), apiPet = get(), apiFollow = get(), apiPost = get(), apiUserActivity = get(), networkCheck = get(), scheduler = get()) }
    // ViewModel for Profile Edit View
    viewModel { ProfileEditViewModel(apiUser = get(), networkCheck = get(), scheduler = get()) }
    // ViewModel for follow View
    viewModel { UserViewModel(apiFollow = get(), apiUser = get(), petInfo = get(), networkCheck = get(), scheduler = get()) }
    // ViewModel for Gallery Image Select View
    viewModel { GalleryImageSelectViewModel(scheduler = get()) }
    // ViewModel for Topic Pet Edit View
    viewModel { TopicPetEditViewModel(apiPet = get(), petInfo = get(), networkCheck = get(), scheduler = get()) }
    // ViewModel for Topic Pet Detail View
    viewModel { TopicPetDetailViewModel(apiPet = get(), petInfo = get(), networkCheck = get(), scheduler = get()) }
    // ViewModel for Setting View
    viewModel { SettingViewModel(apiUser = get(), networkCheck = get(), scheduler = get()) }
    // ViewModel for Posts View
    viewModel { PostsViewModel(apiPost = get(), apiShop = get(), apiUserActivity = get(), networkCheck = get(), scheduler = get()) }
    // ViewModel for Withdraw View
    viewModel { WithDrawViewModel(apiUser = get(), networkCheck = get(), scheduler = get()) }
    // ViewModel for Notice View
    viewModel { NoticeViewModel(apiNotice = get(), networkCheck = get(), scheduler = get()) }
    // ViewModel for Search View
    viewModel { SearchViewModel(apiShop = get(), apiPost = get(), apiUser = get(), petInfo = get(), networkCheck = get(), scheduler = get()) }
    // ViewModel for Image Filter View
    viewModel { ImageFilterViewModel(gpuImageFilterData = get(), scheduler = get()) }
    // ViewModel for Post Content View
    viewModel { PostContentViewModel(apiPost = get(), apiHashTag = get(), networkCheck = get(), scheduler = get()) }
    // ViewModel for Post Goods View
    viewModel { PostUsedGoodsViewModel(apiUserActivity = get(), networkCheck = get(), scheduler = get()) }
    // ViewModel for Notification View
    viewModel { NotificationViewModel(apiNotification = get(), networkCheck = get(), scheduler = get()) }
    // ViewModel for Goods Detail View
    viewModel { GoodsDetailViewModel(apiGodo = get(), apiShop = get(), apiPet = get(), apiUser = get(), apiUserActivity = get(), petInfo = get(), networkCheck = get(), scheduler = get()) }
    // ViewModel for Goods Info View
    viewModel { GoodsInfoVewModel(scheduler = get()) }
    // ViewModel for My Shopping View
    viewModel { MyShoppingViewModel(apiGodo = get(), apiUserActivity = get(), networkCheck = get(), scheduler = get()) }
    // ViewModel for Recent View View
    viewModel { RecentViewViewModel(apiShop = get(), networkCheck = get(), scheduler = get()) }
    // ViewModel for Cart View
    viewModel { CartViewModel(apiGodo = get(), networkCheck = get(), scheduler = get()) }
    // ViewModel for Buy Goods View
    viewModel { BuyGoodsViewModel(apiGodo = get(), networkCheck = get(), scheduler = get()) }
    // ViewModel for Complete Purchase View
    viewModel { CompletePurchaseViewModel(apiGodo = get(), networkCheck = get(), scheduler = get()) }
    // ViewModel for Order Detail View
    viewModel { OrderDetailViewModel(apiGodo = get(), networkCheck = get(), scheduler = get()) }
    // ViewModel for Order State View
    viewModel { OrderStateViewModel(apiGodo = get(), networkCheck = get(), scheduler = get()) }
    // ViewModel for Order CRR(Change Refund Return) View
    viewModel { OrderCRRViewModel(apiGodo = get(), networkCheck = get(), scheduler = get()) }
    // ViewModel for CRR(Change Refund Return) Detail View
    viewModel { CRRDetailViewModel(apiGodo = get(), networkCheck = get(), scheduler = get()) }
    // ViewModel for Goods Rating View
    viewModel { GoodsRatingViewModel(apiShop = get(), apiPet = get(), apiUserActivity = get(), networkCheck = get(), scheduler = get()) }
}

val utilModule: Module = applicationContext {
    // provided rx scheduler
    bean { ApplicationSchedulerProvider() as SchedulerProvider }
    // provided network check
    bean { NetworkCheck(context = get()) }
    // provided shared preference
    bean { SPHelper(context = get()) }
    // provided pet information
    bean { PetInfo(context = get()) }
    // provided font color span
    bean { FontColorSpan(context = get()) }
    // provided letter or digit input filter
    bean { LetterOrDigitInputFilter() }
    // provided permission checker
    bean { PermissionHelper() }
    // GPU Image filter
    bean { GPUImageFilterData() }
}

//const val xlabRemoteBaseUrl = ApiURL.XLAB_API_URL_SSL
const val xlabRemoteBaseUrl = "http://192.168.1.11:8080"
val remoteModule: Module = applicationContext {
    // provided web components
    bean { createOkHttpClient() }

    // user api interface
    bean { createRetrofit<IUserRequest>(client = get(), baseUrl = xlabRemoteBaseUrl) }
    // user api implement
    bean { ApiUser(iUserRequest = get()) as ApiUserProvider }

    // pet api interface
    bean { createRetrofit<IPetRequest>(client = get(), baseUrl = xlabRemoteBaseUrl) }
    // pet api implement
    bean { ApiPet(iPetRequest = get()) as ApiPetProvider }

    // post api interface
    bean { createRetrofit<IPostRequest>(client = get(), baseUrl = xlabRemoteBaseUrl) }
    // post api implement
    bean { ApiPost(iPostRequest = get()) as ApiPostProvider }

    // shop api interface
    bean { createRetrofit<IShopRequest>(client = get(), baseUrl = xlabRemoteBaseUrl) }
    // shop api implement
    bean { ApiShop(iShopRequest = get()) as ApiShopProvider }

    // user activity api interface
    bean { createRetrofit<IUserActivityRequest>(client = get(), baseUrl = xlabRemoteBaseUrl) }
    // user activity api implement
    bean { ApiUserActivity(iUserActivityRequest = get()) as ApiUserActivityProvider }

    // follow api interface
    bean { createRetrofit<IFollowRequest>(client = get(), baseUrl = xlabRemoteBaseUrl) }
    // follow api implement
    bean { ApiFollow(iFollowRequest = get()) as ApiFollowProvider }

    // comment api interface
    bean { createRetrofit<ICommentRequest>(client = get(), baseUrl = xlabRemoteBaseUrl) }
    // comment api implement
    bean { ApiComment(iCommentRequest = get()) as ApiCommentProvider }

    // notice api interface
    bean { createRetrofit<INoticeRequest>(client = get(), baseUrl = xlabRemoteBaseUrl) }
    // notice api implement
    bean { ApiNotice(iNoticeRequest = get()) as ApiNoticeProvider }

    // hash tag api interface
    bean { createRetrofit<IHashTagRequest>(client = get(), baseUrl = xlabRemoteBaseUrl) }
    // hash tag api implement
    bean { ApiHashTag(iHashTagRequest = get()) as ApiHashTagProvider }

    // notification api interface
    bean { createRetrofit<INotificationRequest>(client = get(), baseUrl = xlabRemoteBaseUrl) }
    // notification api implement
    bean { ApiNotification(iNotificationRequest = get()) as ApiNotificationProvider }

    // godo api interface
    bean { createRetrofit<IGodoRequest>(client = get(), baseUrl = xlabRemoteBaseUrl) }
    // godo api implement
    bean { ApiGodo(iGodoRequest = get()) as ApiGodoProvider }
}

/**
 * Module list
 */
val appModule = listOf(viewModelModule, utilModule, remoteModule)