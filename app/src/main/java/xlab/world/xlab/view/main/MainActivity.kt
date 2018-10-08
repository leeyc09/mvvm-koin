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
import org.koin.android.ext.android.inject
import xlab.world.xlab.R
import xlab.world.xlab.adapter.viewPager.ViewStatePagerAdapter
import xlab.world.xlab.utils.font.FontColorSpan
import xlab.world.xlab.utils.support.*
import xlab.world.xlab.utils.view.button.MatchButtonHelper
import xlab.world.xlab.utils.view.dialog.DefaultProgressDialog
import xlab.world.xlab.utils.view.tabLayout.TabLayoutHelper
import xlab.world.xlab.utils.view.toast.DefaultToast
import xlab.world.xlab.view.main.fragment.FeedAllFragment
import xlab.world.xlab.view.main.fragment.FeedExploreFragment
import xlab.world.xlab.view.main.fragment.FeedFollowingFragment
import xlab.world.xlab.view.main.fragment.FeedShopFragment

class MainActivity : AppCompatActivity(), View.OnClickListener {
    private val fontColorSpan: FontColorSpan by inject()
    private val spHelper: SPHelper by inject()
    private val permissionHelper: PermissionHelper by inject()

    private var linkData: Uri? = null

    private lateinit var defaultToast: DefaultToast
    private lateinit var progressDialog: DefaultProgressDialog

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
        super.onPause()
        ViewFunction.hideKeyboard(context = this, view = mainLayout)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        PrintLog.d("resultCode", resultCode.toString(), this::class.java.name)
        PrintLog.d("requestCode", requestCode.toString(), this::class.java.name)
        when (resultCode) {
            Activity.RESULT_OK -> {
                when (requestCode) {
//                    RequestCodeData.POST_COMMENT, // 댓글
                    RequestCodeData.POST_DETAIL -> {  // 포스트 상세
                        feedFollowingFragment.reloadFeedData(loadingBar = null)
                    }
//                    RequestCodeData.TOPIC_ADD, // 토픽 추가
//                    RequestCodeData.COMBINATION_SEARCH , // 통합 검색
//                    RequestCodeData.TAG_POST, // 포스트 태그 검색
//                    RequestCodeData.GOODS_DETAIL, // 상품 상세
//                    RequestCodeData.PROFILE, // 프로필
//                    RequestCodeData.NOTIFICATION, // 푸시
//                    RequestCodeData.GOODS_SEARCH -> { // 상품 검색
//                        reloadAllData { max, current -> }
//                    }
//                    RequestCodeData.TOPIC_SETTING -> { // 토픽 설정
//                        // 전체
//                        allFragment.initFeedAllData { }
//                        // 제품
//                        shopFragment.initFeedShopData {  }
//                    }
//                    RequestCodeData.POST_UPLOAD -> { // 포스트 업로드
//                    }
                }
            }
            ResultCodeData.LOAD_OLD_DATA -> { // notification dot reload
//                if (SPHelper(this).accessToken != "") {
//                    loadExistNewData ({ profileNew ->
//                        profileDotView.visibility =
//                                if (profileNew) View.VISIBLE
//                                else View.INVISIBLE
//                    }, { notiNew ->
//                        notiDotView.visibility =
//                                if (notiNew) View.VISIBLE
//                                else View.INVISIBLE
//                    })
//                }
            }
            ResultCodeData.LOGIN_SUCCESS -> { // login -> reload all data
                reloadAllData()
            }
            ResultCodeData.LOGOUT_SUCCESS -> { // logout -> finish activity
                finish()
            }
        }
    }

    private fun onSetup() {
        linkData = intent.data

        // appBarLayout 애니메이션 없애기
        appBarLayout.stateListAnimator = null

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
    }

    private fun onBindEvent() {
        actionSearchBtn.setOnClickListener(this) // 통합 검색 버튼
        actionUploadBtn.setOnClickListener(this) // 포스트 업로드 버튼
        actionNotificationBtn.setOnClickListener(this) // 알림 버튼
        actionProfileBtn.setOnClickListener(this) // 프로필 버튼

        // 키보드 보일때 매칭 버튼 안보이게
        ViewFunction.showUpKeyboardLayout(view = mainLayout) { visibility ->
            matchBtnLayout.visibility =
                    if (visibility == View.VISIBLE) View.GONE
                    else View.VISIBLE
        }

        ViewFunction.onViewPagerChangePosition(viewPager = viewPager) { position ->
            tabLayoutHelper.changeSelectedTab(selectIndex = position)
            ViewFunction.hideKeyboard(context = this, view = mainLayout)
        }
    }

    private fun observeViewModel() {
    }

    override fun onClick(v: View?) {
        v?.let {
            when (v.id) {
                R.id.actionSearchBtn -> { // 통합 검색 버튼
                    RunActivity.combinedSearchActivity(context = this)
                }
                R.id.actionNotificationBtn -> { // 노티 버튼
                    if (spHelper.accessToken.isEmpty()) // 게스트
                        return

                    RunActivity.notificationActivity(context = this)
                }
                R.id.actionUploadBtn -> { // 포스트 업로드 버튼
                    if (spHelper.accessToken.isEmpty()) // 게스트
                        return

                    if (!permissionHelper.hasPermission(context = this, permissions = permissionHelper.cameraPermissions)) {
                        permissionHelper.requestCameraPermissions(context = this)
                        return
                    }

                    RunActivity.postUploadPictureActivity(context = this)
                }
                R.id.actionProfileBtn -> { // 프로필 버튼
                    if (spHelper.accessToken.isEmpty()) // 게스트
                        return

                    RunActivity.profileActivity(context = this, userId = spHelper.userId)
                }
            }
        }
    }

    private fun reloadAllData() {
        feedAllFragment.reloadFeedData(loadingBar = null)
        feedFollowingFragment.reloadFeedData(loadingBar = null)
        feedExploreFragment.reloadFeedData(loadingBar = null)
        feedShopFragment.reloadFeedData(loadingBar = null)
    }

    companion object {
        fun newIntent(context: Context, linkData: Uri?): Intent {
            val intent = Intent(context, MainActivity::class.java)
            intent.data = linkData

            return intent
        }
    }
}
