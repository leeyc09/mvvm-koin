package xlab.world.xlab.view.notification

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import kotlinx.android.synthetic.main.action_bar_default.*
import kotlinx.android.synthetic.main.activity_notification.*
import org.koin.android.architecture.ext.viewModel
import org.koin.android.ext.android.inject
import xlab.world.xlab.R
import xlab.world.xlab.adapter.viewPager.ViewStatePagerAdapter
import xlab.world.xlab.utils.font.FontColorSpan
import xlab.world.xlab.utils.support.PrintLog
import xlab.world.xlab.utils.support.RequestCodeData
import xlab.world.xlab.utils.support.ResultCodeData
import xlab.world.xlab.utils.view.tabLayout.TabLayoutHelper
import xlab.world.xlab.view.notification.fragment.ShopNotificationFragment
import xlab.world.xlab.view.notification.fragment.SocialNotificationFragment

class NotificationActivity : AppCompatActivity(), View.OnClickListener {
    private val notificationViewModel: NotificationViewModel by viewModel()
    private val fontColorSpan: FontColorSpan by inject()

    private lateinit var tabLayoutHelper: TabLayoutHelper

    private lateinit var viewPagerAdapter: ViewStatePagerAdapter
    private lateinit var socialNotificationFragment: SocialNotificationFragment
    private lateinit var shopNotificationFragment: ShopNotificationFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification)

        onSetup()

        onBindEvent()

        observeViewModel()
    }

    override fun onBackPressed() {
        actionBackBtn.performClick()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        PrintLog.d("resultCode", resultCode.toString(), this::class.java.name)
        PrintLog.d("requestCode", requestCode.toString(), this::class.java.name)

        notificationViewModel.setResultCode(resultCode = resultCode)
        when (resultCode) {
            Activity.RESULT_OK -> {
                when (requestCode) {
                    RequestCodeData.PROFILE, // 프로필
                    RequestCodeData.POST_DETAIL -> { // 포스트 상세
                    }
                }
            }
            ResultCodeData.LOGOUT_SUCCESS -> { // logout -> finish activity
                actionBackBtn.performClick()
            }
        }
    }

    private fun onSetup() {
        // 타이틀 설정, 액션 버튼 비활성화
        actionBarTitle.setText(getText(R.string.notification), TextView.BufferType.SPANNABLE)
        actionBtn.visibility = View.GONE

        // 프래그먼트 초기화
        socialNotificationFragment = SocialNotificationFragment.newFragment()
        shopNotificationFragment = ShopNotificationFragment.newFragment()

        viewPagerAdapter = ViewStatePagerAdapter(manager = supportFragmentManager)
        viewPagerAdapter.addFragment(fragment = socialNotificationFragment, title = resources.getString(R.string.social))
        viewPagerAdapter.addFragment(fragment = shopNotificationFragment, title = resources.getString(R.string.shopping))
        viewPager.adapter = viewPagerAdapter

        // tab layout 초기화
        tabLayoutHelper = TabLayoutHelper(context = this,
                defaultSelectFont = fontColorSpan.notoBold000000,
                defaultUnSelectFont = fontColorSpan.notoMediumBFBFBF,
                useDefaultEvent = true,
                listener = null)
        tabLayoutHelper.handle(layout = tabLayout, viewPager = viewPager)
        tabLayoutHelper.addTab(resources.getString(R.string.social), tabLayout = R.layout.tab_layout_dot, fontSize = null, selectFont = null, unSelectFont = null, extraData = false)
        tabLayoutHelper.addTab(resources.getString(R.string.shopping), tabLayout = R.layout.tab_layout_dot, fontSize = null, selectFont = null, unSelectFont = null, extraData = false)
        tabLayoutHelper.changeSelectedTab(0)
    }

    private fun onBindEvent() {
        actionBackBtn.setOnClickListener(this) // 뒤로가기
    }

    private fun observeViewModel() {
        // UI 이벤트 observe
        notificationViewModel.uiData.observe(this, android.arch.lifecycle.Observer { uiData ->
            uiData?.let { _ ->
                uiData.resultCode?.let {
                    setResult(it)
                    finish()
                }
            }
        })
    }

    override fun onClick(v: View?) {
        v?.let {
            when (v.id) {
                R.id.actionBackBtn -> { // 뒤로가기
                    notificationViewModel.backBtnAction()
                }
            }
        }
    }

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, NotificationActivity::class.java)
        }
    }

}
