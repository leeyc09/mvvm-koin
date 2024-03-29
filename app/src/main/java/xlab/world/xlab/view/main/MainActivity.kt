package xlab.world.xlab.view.main

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.action_bar_main.*
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.android.architecture.ext.viewModel
import org.koin.android.ext.android.inject
import xlab.world.xlab.R
import xlab.world.xlab.adapter.viewPager.ViewStatePagerAdapter
import xlab.world.xlab.utils.font.FontColorSpan
import xlab.world.xlab.utils.support.*
import xlab.world.xlab.utils.view.button.MatchButtonHelper
import xlab.world.xlab.utils.view.dialog.DefaultDialog
import xlab.world.xlab.utils.view.dialog.DialogCreator
import xlab.world.xlab.utils.view.dialog.TwoSelectBottomDialog
import xlab.world.xlab.utils.view.tabLayout.TabLayoutHelper
import xlab.world.xlab.view.main.fragment.FeedAllFragment
import xlab.world.xlab.view.main.fragment.FeedExploreFragment
import xlab.world.xlab.view.main.fragment.FeedFollowingFragment
import xlab.world.xlab.view.main.fragment.FeedShopFragment
import xlab.world.xlab.view.notice.NoticeViewModel
import xlab.world.xlab.view.notification.NotificationViewModel
import xlab.world.xlab.viewModel.ShareViewModel

class MainActivity : AppCompatActivity(), View.OnClickListener {
    private val mainViewModel: MainViewModel by viewModel()
    private val notificationViewModel: NotificationViewModel by viewModel()
    private val noticeViewModel: NoticeViewModel by viewModel()
    private val shareViewModel: ShareViewModel by viewModel()
    private val fontColorSpan: FontColorSpan by inject()
    private val spHelper: SPHelper by inject()
    private val permissionHelper: PermissionHelper by inject()

    private lateinit var loginDialog: DefaultDialog
    private lateinit var postUploadTypeSelectDialog: TwoSelectBottomDialog

    private lateinit var matchButtonHelper: MatchButtonHelper
    private lateinit var tabLayoutHelper: TabLayoutHelper

    private lateinit var viewPagerAdapter: ViewStatePagerAdapter
    private lateinit var feedAllFragment: FeedAllFragment
    private lateinit var feedFollowingFragment: FeedFollowingFragment
    private lateinit var feedExploreFragment: FeedExploreFragment
    private lateinit var feedShopFragment: FeedShopFragment

    private val tabLayoutListener = object: TabLayoutHelper.Listener {
        override fun onDoubleClick(index: Int) {
            when (index) {
                0 -> (viewPagerAdapter.getItem(index) as FeedAllFragment).scrollToTop()
                1 -> (viewPagerAdapter.getItem(index) as FeedFollowingFragment).scrollToTop()
                2 -> (viewPagerAdapter.getItem(index) as FeedExploreFragment).scrollToTop()
                3 -> (viewPagerAdapter.getItem(index) as FeedShopFragment).scrollToTop()
            }
        }
    }
    private val matchButtonListener = object: MatchButtonHelper.Listener {
        override fun matchVisibility(visibility: Int) {
            feedAllFragment.matchVisibleChange(visibility)
            feedShopFragment.matchVisibleChange(visibility)
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        onSetup()

        onBindEvent()

        observeViewModel()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            AppConstants.PERMISSION_REQUEST_CAMERA_CODE -> {
                if (permissionHelper.resultRequestPermissions(results = grantResults))
                    actionUploadBtn.performClick()
            }
        }
    }

    override fun onPause() {
        ViewFunction.hideKeyboard(context = this, view = mainLayout)
        super.onPause()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        PrintLog.d("resultCode", resultCode.toString(), this::class.java.name)
        PrintLog.d("requestCode", requestCode.toString(), this::class.java.name)

        when (resultCode) {
            Activity.RESULT_OK -> {
                when (requestCode) {
                    RequestCodeData.POST_COMMENT, // 댓글
                    RequestCodeData.POST_DETAIL -> {  // 포스트 상세
                        feedFollowingFragment.reloadFeedData(loadingBar = null)
                    }
                    RequestCodeData.TOPIC_ADD, // 토픽 추가
                    RequestCodeData.COMBINED_SEARCH , // 통합 검색
                    RequestCodeData.TAG_POST, // 포스트 태그 검색
                    RequestCodeData.GOODS_DETAIL, // 상품 상세
                    RequestCodeData.PROFILE, // 프로필
                    RequestCodeData.NOTIFICATION, // 푸시
                    RequestCodeData.GOODS_SEARCH -> { // 상품 검색
                        feedAllFragment.reloadFeedData(loadingBar = null)
                        feedFollowingFragment.reloadFeedData(loadingBar = null)
                        feedExploreFragment.reloadFeedData(loadingBar = null)
                        feedShopFragment.reloadFeedData(loadingBar = null)
                    }
                    RequestCodeData.TOPIC_SETTING -> { // 토픽 설정
                        // 전체, 제품 피드 갱신
                        feedAllFragment.reloadFeedData(loadingBar = null)
                        feedShopFragment.reloadFeedData(loadingBar = null)
                    }
                    RequestCodeData.POST_UPLOAD -> { // 포스트 업로드
                    }
                }

                // 공지, 푸시알림 새로운거 있는지 불러오기
                notificationViewModel.loadExistNewNotification(authorization = spHelper.authorization)
                noticeViewModel.loadExistNewNotification(authorization = spHelper.authorization)
            }
            ResultCodeData.LOAD_OLD_DATA -> {
                // 공지, 푸시알림 새로운거 있는지 불러오기
                notificationViewModel.loadExistNewNotification(authorization = spHelper.authorization)
                noticeViewModel.loadExistNewNotification(authorization = spHelper.authorization)
            }
            ResultCodeData.LOGIN_SUCCESS -> { // login -> reload all data
                feedAllFragment.reloadFeedData(loadingBar = null)
                feedFollowingFragment.reloadFeedData(loadingBar = null)
                feedExploreFragment.reloadFeedData(loadingBar = null)
                feedShopFragment.reloadFeedData(loadingBar = null)

                notificationViewModel.loadExistNewNotification(authorization = spHelper.authorization)
                noticeViewModel.loadExistNewNotification(authorization = spHelper.authorization)
            }
            ResultCodeData.LOGOUT_SUCCESS -> { // logout -> finish activity
                finish()
            }
        }
    }

    private fun onSetup() {
        // appBarLayout 애니메이션 없애기
        appBarLayout.stateListAnimator = null

        // Dialog 초기화
        loginDialog = DialogCreator.loginDialog(context = this)
        postUploadTypeSelectDialog = DialogCreator.postUploadTypeSelectDialog(context = this)

        // 프래그먼트 초기화
        feedAllFragment = FeedAllFragment.newFragment()
        feedFollowingFragment = FeedFollowingFragment.newFragment()
        feedExploreFragment = FeedExploreFragment.newFragment()
        feedShopFragment = FeedShopFragment.newFragment()

        viewPagerAdapter = ViewStatePagerAdapter(manager = supportFragmentManager)
        viewPagerAdapter.addFragment(fragment = feedAllFragment, title = resources.getString(R.string.all))
        viewPagerAdapter.addFragment(fragment = feedFollowingFragment, title = resources.getString(R.string.following))
        viewPagerAdapter.addFragment(fragment = feedExploreFragment, title = resources.getString(R.string.explore))
        viewPagerAdapter.addFragment(fragment = feedShopFragment, title = resources.getString(R.string.goods))
        viewPager.adapter = viewPagerAdapter

        // tab layout 초기화
        tabLayoutHelper = TabLayoutHelper(context = this,
                defaultSelectFont = fontColorSpan.notoBold000000,
                defaultUnSelectFont = fontColorSpan.notoMediumBFBFBF,
                useDefaultEvent = false,
                listener = tabLayoutListener)
        tabLayoutHelper.handle(layout = tabLayout, viewPager = viewPager)
        tabLayoutHelper.addTab(tabName = resources.getString(R.string.all),
                fontSize = null,
                tabLayout = R.layout.tab_layout_feed_all,
                selectFont = fontColorSpan.notoBold000000,
                unSelectFont = fontColorSpan.notoBoldBFBFBF,
                extraData = null)
        tabLayoutHelper.addTab(tabName = getString(R.string.following), tabLayout = null, fontSize = null, selectFont = null, unSelectFont = null, extraData = null)
        tabLayoutHelper.addTab(tabName = getString(R.string.explore), tabLayout = null, fontSize = null, selectFont = null, unSelectFont = null, extraData = null)
        tabLayoutHelper.addTab(tabName = getString(R.string.goods), tabLayout = null, fontSize = null, selectFont = null, unSelectFont = null, extraData = null)
        tabLayoutHelper.changeSelectedTab(selectIndex = 0)

        // match button 초기화
        matchButtonHelper = MatchButtonHelper(
                context = this,
                rootView = matchBtnLayout,
                isMatchShow = true,
                listener = matchButtonListener)

        notificationViewModel.loadExistNewNotification(authorization = spHelper.authorization)
        noticeViewModel.loadExistNewNotification(authorization = spHelper.authorization)

        shareViewModel.linkShareActivity(data = intent.data)
    }

    private fun onBindEvent() {
        actionSearchBtn.setOnClickListener(this) // 통합 검색 버튼
        actionUploadBtn.setOnClickListener(this) // 포스트 업로드 버튼
        actionNotificationBtn.setOnClickListener(this) // 알림 버튼
        actionProfileBtn.setOnClickListener(this) // 프로필 버튼

        // 키보드 보일때 매칭 버튼 안보이게
        ViewFunction.showUpKeyboardLayout(view = mainLayout) { visibility ->
            matchBtnLayout.visibility =
                    if (visibility == View.VISIBLE || viewPager.currentItem == 1 || viewPager.currentItem == 2) View.GONE
                    else View.VISIBLE
        }

        ViewFunction.onViewPagerChangePosition(viewPager = viewPager) { position ->
            tabLayoutHelper.changeSelectedTab(selectIndex = position)
            ViewFunction.hideKeyboard(context = this, view = mainLayout)
            matchBtnLayout.visibility =
                    if (position == 1 || position == 2) View.GONE  // 팔로잉, 탐색 tab
                    else View.VISIBLE
        }
    }

    private fun observeViewModel() {
        // TODO: Main View Model
        // UI 이벤트 observe
        mainViewModel.uiData.observe(this, android.arch.lifecycle.Observer { uiData ->
            uiData?.let {_->
                uiData.guestMode?.let {
                    if (spHelper.accessToken.isEmpty()) { // 게스트
                        loginDialog.showDialog(tag = null)
                    }
                }
            }
        })

        // button Action 이벤트 observe
        mainViewModel.btnActionData.observe(owner = this, observer = android.arch.lifecycle.Observer { eventData ->
            eventData?.let {_->
                eventData.notification?.let {
                    RunActivity.notificationActivity(context = this)
                }
                eventData.profile?.let {
                    RunActivity.profileActivity(context = this, userId = spHelper.userId)
                }
                eventData.post?.let {
                    RunActivity.postUploadPictureActivity(context = this, postId = "", youTubeVideoId = "")
                }
                eventData.postTypeDialog?.let {
                    postUploadTypeSelectDialog.showDialog(manager = supportFragmentManager, dialogTag = "postUploadTypeSelectDialog",
                            tagData = null)
                }
            }
        })

        // TODO: Notification View Model
        // UI 이벤트 observe
        notificationViewModel.uiData.observe(this, android.arch.lifecycle.Observer { uiData ->
            uiData?.let {_->
                uiData.newNotificationDotVisibility?.let {
                    notiDotView.visibility = it
                }
            }
        })

        // notification activity 이동 이벤트 observe
        notificationViewModel.notificationRunData.observe(owner = this, observer = android.arch.lifecycle.Observer { eventData ->
            eventData?.let { _ ->
                eventData.userId?.let {
                    RunActivity.profileActivity(context = this, userId = it)
                }
                eventData.postId?.let {
                    RunActivity.postDetailActivity(context = this, postId = it, goComment = false)
                }
                eventData.commentPostId?.let {
                    RunActivity.postDetailActivity(context = this, postId = it, goComment = true)
                }
            }
        })

        // TODO: Notice View Model
        // UI 이벤트 observe
        noticeViewModel.uiData.observe(this, android.arch.lifecycle.Observer { uiData ->
            uiData?.let {_->
                uiData.newNoticeDotVisibility?.let {
                    profileDotView.visibility = it
                }
            }
        })

        // TODO: Share View Model
        // link data activity 이동 이벤트 observe
        shareViewModel.linkData.observe(owner = this, observer = android.arch.lifecycle.Observer { eventData ->
            eventData?.let { _ ->
                eventData.userId?.let {
                    RunActivity.profileActivity(context = this, userId = it)
                }
                eventData.postId?.let {
                    RunActivity.postDetailActivity(context = this, postId = it, goComment = false)
                }
                eventData.goodsCode?.let {
                    RunActivity.goodsDetailActivity(context = this, goodsCode = it)
                }
                eventData.noData?.let {
                    notificationViewModel.notificationRun(notificationType = intent.getStringExtra(IntentPassName.NOTIFICATION_TYPE),
                            notificationData = intent.getStringExtra(IntentPassName.NOTIFICATION_DATA))
                }
            }
        })
    }

    override fun onClick(v: View?) {
        v?.let {
            when (v.id) {
                R.id.actionSearchBtn -> { // 통합 검색 버튼
                    RunActivity.combinedSearchActivity(context = this)
                }
                R.id.actionNotificationBtn -> { // 노티 버튼
                    mainViewModel.notificationBtnAction(authorization = spHelper.authorization)
                }
                R.id.actionUploadBtn -> { // 포스트 업로드 버튼
                    // 권한 체크
                    if (!permissionHelper.hasPermission(context = this, permissions = permissionHelper.cameraPermissions)) {
                        permissionHelper.requestCameraPermissions(context = this)
                        return
                    }

                    mainViewModel.uploadPostBtnAction(authorization = spHelper.authorization,
                            userLevel = spHelper.userLevel)
                }
                R.id.actionProfileBtn -> { // 프로필 버튼
                    mainViewModel.profileBtnAction(authorization = spHelper.authorization)
                }
            }
        }
    }

    companion object {
        fun newIntent(context: Context, linkData: Uri?,
                      notificationType: String?, notificationData: String?): Intent {
            val intent = Intent(context, MainActivity::class.java)
            intent.data = linkData
            intent.putExtra(IntentPassName.NOTIFICATION_TYPE, notificationType)
            intent.putExtra(IntentPassName.NOTIFICATION_DATA, notificationData)

            return intent
        }
    }
}
