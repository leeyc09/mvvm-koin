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
    private val onBoardingViewModel: OnBoardingViewModel by viewModel()
    private val spHelper: SPHelper by inject()

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
        // fragment 초기화
        firstFragment = OnBoardingFragment.newFragment(0)
        secondFragment = OnBoardingFragment.newFragment(1)
        thirdFragment = OnBoardingFragment.newFragment(2)

        // view pager adapter & view pager 초기화
        viewPagerAdapter = ViewStatePagerAdapter(manager = supportFragmentManager)
        viewPagerAdapter.addFragment(fragment = firstFragment, title = "firstFragment")
        viewPagerAdapter.addFragment(fragment = secondFragment, title = "secondFragment")
        viewPagerAdapter.addFragment(fragment = thirdFragment, title = "thirdFragment")
        viewPager.adapter = viewPagerAdapter

        // dot indicator 초기화
        tabLayoutDot.setupWithViewPager(viewPager, true)
        val marginDIP = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3f, resources.displayMetrics).toInt()
        ViewFunction.setDotIndicator(tabLayoutDot = tabLayoutDot, marginDIP = marginDIP)

        // 건너뛰기, 시작하기 버튼 활성화 변경
        onBoardingViewModel.buttonVisibility(index = 0, pagerSize = viewPagerAdapter.count)
    }

    private fun onBindEvent() {
        skipBtn.setOnClickListener(this) // 건너뛰기
        startBtn.setOnClickListener(this) // 시작하기

        ViewFunction.onViewPagerChangePosition(viewPager = viewPager) { index ->
            // 건너뛰기, 시작하기 버튼 활성화 변경
            onBoardingViewModel.buttonVisibility(index = index, pagerSize = viewPagerAdapter.count)
        }
    }

    private fun observeViewModel() {
        // UI 이벤트 observe
        onBoardingViewModel.uiData.observe(this, android.arch.lifecycle.Observer { uiData ->
            uiData?.let { _ ->
                uiData.skipBtnVisibility?.let {
                    skipBtn.visibility = it
                }
                uiData.startBtnVisibility?.let {
                    startBtn.visibility = it
                }
            }
        })
    }

    override fun onClick(v: View?) {
        v?.let {
            when (v.id) {
                R.id.skipBtn -> { // 건너뛰기 -> view pager 마지막 페이지로
                    viewPager.currentItem = viewPagerAdapter.count - 1
                }
                R.id.startBtn -> { // 시작하기 -> 로그인 화면
                    spHelper.onBoard = true
                    RunActivity.loginActivity(context = this,
                            isComePreLoadActivity = true,
                            linkData = null)
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
