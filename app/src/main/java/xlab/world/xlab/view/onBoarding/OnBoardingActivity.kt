package xlab.world.xlab.view.onBoarding

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.view.ViewPager
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.activity_on_boarding.*
import org.koin.android.architecture.ext.viewModel
import org.koin.android.ext.android.inject
import xlab.world.xlab.R
import xlab.world.xlab.adapter.viewPager.ViewStatePagerAdapter
import xlab.world.xlab.utils.support.RunActivity
import xlab.world.xlab.utils.support.SPHelper
import xlab.world.xlab.utils.support.ViewFunction
import xlab.world.xlab.view.login.LoginActivity
import xlab.world.xlab.view.onBoarding.fragment.OnBoardingFragment

class OnBoardingActivity : AppCompatActivity(), View.OnClickListener {

    private val viewFunction: ViewFunction by inject()
    private val spHelper: SPHelper by inject()
    private val runActivity: RunActivity by inject()

    private lateinit var viewPagerAdapter: ViewStatePagerAdapter
    private lateinit var firstFragment: OnBoardingFragment
    private lateinit var secondFragment: OnBoardingFragment
    private lateinit var thirdFragment: OnBoardingFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_on_boarding)

        onSetup()

        onBindEvent()

        observeViewModel()
    }

    private fun onSetup() {
        skipBtn.visibility = View.VISIBLE
        startBtn.visibility = View.INVISIBLE

        // 프래그먼트 초기화
        firstFragment = OnBoardingFragment.newFragment(0)
        secondFragment = OnBoardingFragment.newFragment(1)
        thirdFragment = OnBoardingFragment.newFragment(2)
        viewPagerAdapter = ViewStatePagerAdapter(supportFragmentManager)
        viewPagerAdapter.addFragment(firstFragment, "firstFragment")
        viewPagerAdapter.addFragment(secondFragment, "secondFragment")
        viewPagerAdapter.addFragment(thirdFragment, "thirdFragment")
        viewPager.adapter = viewPagerAdapter

        // link tab layout with view pager
        tabLayoutDot.setupWithViewPager(viewPager, true)
        val marginDIP = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3f, resources.displayMetrics).toInt()
        viewFunction.setDotIndicator(tabLayoutDot, marginDIP)
    }

    private fun onBindEvent() {
        skipBtn.setOnClickListener(this) // 건너뛰기
        startBtn.setOnClickListener(this) // 시작하기

        viewFunction.onViewPagerChangePosition(viewPager) { position ->
            // 마지막 페이지일 경우 시작하기 버튼 보여주기
            if (position == viewPagerAdapter.count - 1) {
                skipBtn.visibility = View.INVISIBLE
                startBtn.visibility = View.VISIBLE
            } else {
                skipBtn.visibility = View.VISIBLE
                startBtn.visibility = View.INVISIBLE
            }
        }
    }

    private fun observeViewModel() {

    }

    override fun onClick(v: View?) {
        v?.let {
            when (v.id) {
                R.id.skipBtn -> { // 건너뛰기
                    viewPager.currentItem = viewPagerAdapter.count - 1
                }
                R.id.startBtn -> { // 시작하기 -> 로그인 화면
                    spHelper.onBoard = true
                    runActivity.loginActivity(context = this, isComePreLoadActivity = true, linkData = null)
                    finish()
                }
            }
        }
    }

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, OnBoardingActivity::class.java)
        }
    }
}
