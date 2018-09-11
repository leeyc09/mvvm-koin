package xlab.world.xlab.view.main

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.android.architecture.ext.viewModel
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
import xlab.world.xlab.view.topicSetting.TopicSettingViewModel

class MainActivity : AppCompatActivity(), View.OnClickListener {
    private val fontColorSpan: FontColorSpan by inject()
    private val spHelper: SPHelper by inject()

    private var linkData: Uri? = null

    private lateinit var matchButtonHelper: MatchButtonHelper
    private lateinit var tabLayoutHelper: TabLayoutHelper

    private lateinit var defaultToast: DefaultToast
    private lateinit var progressDialog: DefaultProgressDialog

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
        override fun onMatchSetting() {
            RunActivity.topicSettingActivity(context = this@MainActivity, isGuest = spHelper.accessToken.isEmpty())

        }
        override fun isSelectedMatchBtn(isSelected: Boolean) {
            PrintLog.d("matchButtonListener", "isSelectedMatchBtn: " + isSelected.toString(), "Main")
            val matchVisibility =
                    if (isSelected) View.VISIBLE // show match percent
                    else View.GONE // hide match percent

            feedAllFragment.matchVisibleChange(matchVisibility)
//            shopFragment.matchVisibleChange(matchVisibility)
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        onSetup()

        onBindEvent()

        observeViewModel()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        PrintLog.d("resultCode", resultCode.toString(), this::class.java.name)
        PrintLog.d("requestCode", requestCode.toString(), this::class.java.name)
        when (resultCode) {
            Activity.RESULT_OK -> {
//                when (requestCode) {
//                    RequestCodeData.POST_COMMENT, // 댓글
//                    RequestCodeData.POST_DETAIL -> {  // 포스트 상세
//                        followingFragment.initFeedFollowingData {  }
//                    }
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
//                }
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

        viewPagerAdapter = ViewStatePagerAdapter(supportFragmentManager)
        viewPagerAdapter.addFragment(feedAllFragment, resources.getString(R.string.all))
        viewPagerAdapter.addFragment(feedFollowingFragment, resources.getString(R.string.following))
        viewPagerAdapter.addFragment(feedExploreFragment, resources.getString(R.string.explore))
        viewPagerAdapter.addFragment(feedShopFragment, resources.getString(R.string.shop))
        viewPager.adapter = viewPagerAdapter

        // tab layout 초기화
        tabLayoutHelper = TabLayoutHelper(context = this,
                defaultSelectFont = fontColorSpan.notoBold000000,
                defaultUnSelectFont = fontColorSpan.notoMediumBFBFBF,
                useDefaultEvent = false,
                listener = tabLayoutListener)
        tabLayoutHelper.handle(layout = tabLayout, viewPager = viewPager)
        tabLayoutHelper.addTab(tabName = resources.getString(R.string.all),
                tabLayout = R.layout.tab_layout_feed_all,
                selectFont = fontColorSpan.notoBold000000,
                unSelectFont = fontColorSpan.notoBoldBFBFBF,
                extraData = null)
        tabLayoutHelper.addTab(tabName = resources.getString(R.string.following), tabLayout = null, selectFont = null, unSelectFont = null, extraData = null)
        tabLayoutHelper.addTab(tabName = resources.getString(R.string.explore), tabLayout = null, selectFont = null, unSelectFont = null, extraData = null)
        tabLayoutHelper.addTab(tabName = resources.getString(R.string.shop), tabLayout = null, selectFont = null, unSelectFont = null, extraData = null)
        tabLayoutHelper.changeSelectedTab(selectIndex = 0)

        // match button 초기화
        matchButtonHelper = MatchButtonHelper(rootView = matchBtnLayout,
                isMatchShow = true,
                listener = matchButtonListener)
    }

    private fun onBindEvent() {
        // 키보드 보일때 매칭 버튼 안보이게
        ViewFunction.showUpKeyboardLayout(view = mainLayout) { visibility ->
        }

        ViewFunction.onViewPagerChangePosition(viewPager = viewPager) { position ->
            tabLayoutHelper.changeSelectedTab(selectIndex = position)
        }
    }

    private fun observeViewModel() {
    }

    override fun onClick(v: View?) {
        v?.let {
            when (v.id) {

            }
        }
    }

    private fun reloadAllData() {
        feedAllFragment.reloadFeedData()
        feedFollowingFragment.reloadFeedData()
    }

    companion object {
        fun newIntent(context: Context, linkData: Uri?): Intent {
            val intent = Intent(context, MainActivity::class.java)
            intent.data = linkData

            return intent
        }
    }
}
