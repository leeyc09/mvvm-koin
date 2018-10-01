package xlab.world.xlab.view.webView

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import kotlinx.android.synthetic.main.action_bar_default.*
import kotlinx.android.synthetic.main.activity_policy.*
import org.koin.android.ext.android.inject
import xlab.world.xlab.R
import xlab.world.xlab.adapter.viewPager.ViewStatePagerAdapter
import xlab.world.xlab.utils.font.FontColorSpan
import xlab.world.xlab.utils.support.AppConstants
import xlab.world.xlab.utils.view.tabLayout.TabLayoutHelper
import xlab.world.xlab.view.webView.fragment.PolicyFragment

class PolicyActivity : AppCompatActivity() {
    private val fontColorSpan: FontColorSpan by inject()

    private lateinit var tabLayoutHelper: TabLayoutHelper

    private lateinit var viewPagerAdapter: ViewStatePagerAdapter
    private lateinit var policyFragment1: PolicyFragment
    private lateinit var policyFragment2: PolicyFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_policy)

        onSetup()

        onBindEvent()

        observeViewModel()
    }

    override fun onBackPressed() {
        actionBackBtn.performClick()
    }

    private fun onSetup() {
        actionBarTitle.setText(resources.getText(R.string.policy), TextView.BufferType.SPANNABLE)
        actionBtn.visibility = View.GONE

        // 프래그먼트 초기화
        policyFragment1 = PolicyFragment.newFragment(url = AppConstants.LOCAL_HTML_URL + "clause.html")
        policyFragment2 = PolicyFragment.newFragment(url = AppConstants.LOCAL_HTML_URL + "personalInfo.html")

        viewPagerAdapter = ViewStatePagerAdapter(manager = supportFragmentManager)
        viewPagerAdapter.addFragment(fragment = policyFragment1, title = getString(R.string.inquiry_under3))
        viewPagerAdapter.addFragment(fragment = policyFragment2, title = getString(R.string.inquiry_under2))
        viewPager.adapter = viewPagerAdapter

        // tab layout 초기화
        tabLayoutHelper = TabLayoutHelper(this,
                defaultSelectFont = fontColorSpan.notoBold000000,
                defaultUnSelectFont = fontColorSpan.notoMediumBFBFBF,
                useDefaultEvent = true,
                listener = null)
        tabLayoutHelper.handle(layout = tabLayout, viewPager = viewPager)
        tabLayoutHelper.addTab(tabName = getString(R.string.inquiry_under3), tabLayout = null, fontSize = null, selectFont = null, unSelectFont = null, extraData = null)
        tabLayoutHelper.addTab(tabName = getString(R.string.inquiry_under2), tabLayout = null, fontSize = null, selectFont = null, unSelectFont = null, extraData = null)
        tabLayoutHelper.changeSelectedTab(selectIndex = 0)
    }

    private fun onBindEvent() {
        actionBackBtn.setOnClickListener {
            finish()
        }
    }

    private fun observeViewModel() {

    }

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, PolicyActivity::class.java)
        }
    }
}
