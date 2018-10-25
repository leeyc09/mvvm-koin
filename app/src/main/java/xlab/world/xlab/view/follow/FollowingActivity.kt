package xlab.world.xlab.view.follow

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SimpleItemAnimator
import android.view.View
import android.widget.TextView
import kotlinx.android.synthetic.main.action_bar_title_with_number.*
import kotlinx.android.synthetic.main.activity_following.*
import org.koin.android.architecture.ext.viewModel
import org.koin.android.ext.android.inject
import xlab.world.xlab.R
import xlab.world.xlab.adapter.recyclerView.UserDefaultAdapter
import xlab.world.xlab.adapter.recyclerView.UserRecommendAdapter
import xlab.world.xlab.utils.listener.DefaultListener
import xlab.world.xlab.utils.listener.UserDefaultListener
import xlab.world.xlab.utils.support.*
import xlab.world.xlab.utils.view.dialog.DefaultProgressDialog
import xlab.world.xlab.utils.view.recyclerView.CustomItemDecoration
import xlab.world.xlab.utils.view.toast.DefaultToast

class FollowingActivity : AppCompatActivity(), View.OnClickListener {
    private val followViewModel: FollowViewModel by viewModel()
    private val spHelper: SPHelper by inject()

    private lateinit var defaultToast: DefaultToast
    private lateinit var progressDialog: DefaultProgressDialog

    private lateinit var userRecommendAdapter: UserRecommendAdapter
    private lateinit var followingAdapter: UserDefaultAdapter

    private lateinit var defaultListener: DefaultListener
    private lateinit var userDefaultListener: UserDefaultListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_following)

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

        followViewModel.setResultCode(resultCode = resultCode)
        when (resultCode) {
            Activity.RESULT_OK -> {
                when (requestCode) {
                    RequestCodeData.PROFILE -> { // 프로필
                        followViewModel.loadRecommendUser(authorization = spHelper.authorization, page = 1, loadingBar = null)
                        followViewModel.loadFollowing(authorization = spHelper.authorization, userId = intent.getStringExtra(IntentPassName.USER_ID), page = 1, loadingBar = null)
                    }
                }
            }
            ResultCodeData.LOGIN_SUCCESS -> {
                followViewModel.loadRecommendUser(authorization = spHelper.authorization, page = 1, loadingBar = null)
                followViewModel.loadFollowing(authorization = spHelper.authorization, userId = intent.getStringExtra(IntentPassName.USER_ID), page = 1, loadingBar = null)
            }
            ResultCodeData.LOGOUT_SUCCESS -> {
                actionBackBtn.performClick()
            }
        }
    }

    private fun onSetup() {
        // 타이틀 설정
        actionBarTitle.setText(getString(R.string.following), TextView.BufferType.SPANNABLE)

        // Toast, Dialog 초기화
        defaultToast = DefaultToast(context = this)
        progressDialog = DefaultProgressDialog(context = this)

        // listener 초기화
        defaultListener = DefaultListener(context = this)
        userDefaultListener = UserDefaultListener(context = this,
                followUserEvent = { position ->
                    followViewModel.userFollow(authorization = spHelper.authorization, selectIndex = position, userType = FollowViewModel.UserType.DEFAULT)
                })

        // recommend recycler view & adapter 초기화
        userRecommendAdapter = UserRecommendAdapter(context = this,
                followListener = View.OnClickListener { view ->
                    followViewModel.userFollow(authorization = spHelper.authorization, selectIndex = view.tag as Int, userType = FollowViewModel.UserType.RECOMMEND)
                },
                profileListener = defaultListener.profileListener)
        recommendRecyclerView.adapter = userRecommendAdapter
        recommendRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        (recommendRecyclerView.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false

        // following recycler view & adapter 초기화
        followingAdapter = UserDefaultAdapter(context = this,
                followListener = userDefaultListener.followListener,
                profileListener = defaultListener.profileListener)
        recyclerView.adapter = followingAdapter
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recyclerView.addItemDecoration(CustomItemDecoration(context = this, bottom = 10f))
        (recyclerView.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        recyclerView.isNestedScrollingEnabled = false

        followViewModel.loadRecommendUser(authorization = spHelper.authorization, page = 1)
        followViewModel.loadFollowing(authorization = spHelper.authorization, userId = intent.getStringExtra(IntentPassName.USER_ID), page = 1)
    }

    private fun onBindEvent() {
        actionBackBtn.setOnClickListener(this) // 뒤로가기

        ViewFunction.onRecyclerViewScrolledDown(recyclerView = recommendRecyclerView) {
            ViewFunction.isScrolledRecyclerView(layoutManager = it as LinearLayoutManager, isLoading = userRecommendAdapter.dataLoading, total = userRecommendAdapter.dataTotal) { _ ->
                followViewModel.loadRecommendUser(authorization = spHelper.authorization, page = userRecommendAdapter.dataNextPage)
            }
        }

        ViewFunction.onRecyclerViewScrolledDown(recyclerView = recyclerView) {
            ViewFunction.isScrolledRecyclerView(layoutManager = it as LinearLayoutManager, isLoading = followingAdapter.dataLoading, total = followingAdapter.dataTotal) { _ ->
                followViewModel.loadFollowing(authorization = spHelper.authorization, userId = intent.getStringExtra(IntentPassName.USER_ID), page = followingAdapter.dataNextPage)
            }
        }
    }

    private fun observeViewModel() {
        // UI 이벤트 observe
        followViewModel.uiData.observe(this, android.arch.lifecycle.Observer { uiData ->
            uiData?.let { _ ->
                uiData.isLoading?.let {
                    if (it && !progressDialog.isShowing)
                        progressDialog.show()
                    else if (!it && progressDialog.isShowing)
                        progressDialog.dismiss()
                }
                uiData.toastMessage?.let {
                    defaultToast.showToast(message = it)
                }
                uiData.resultCode?.let {
                    setResult(it)
                    finish()
                }
                uiData.recommendLayoutVisibility?.let {
                    recommendLayout.visibility = it
                }
                uiData.recommendUserData?.let {
                    userRecommendAdapter.linkData(userRecommendData = it)
                }
                uiData.recommendUserUpdate?.let {
                    userRecommendAdapter.notifyDataSetChanged()
                }
                uiData.recommendUserUpdateIndex?.let {
                    followViewModel.setResultCode(resultCode = Activity.RESULT_OK)
                    userRecommendAdapter.notifyItemChanged(it)
                }
                uiData.emptyDefaultUserVisibility?.let {
                    textViewNoFollowing.visibility = it
                }
                uiData.defaultUserData?.let {
                    followingAdapter.linkData(userDefaultData = it)
                }
                uiData.defaultUserUpdate?.let {
                    followingAdapter.notifyDataSetChanged()
                }
                uiData.defaultUserUpdateIndex?.let {
                    followViewModel.setResultCode(resultCode = Activity.RESULT_OK)
                    followingAdapter.notifyItemChanged(it)
                }
                uiData.followingCnt?.let {
                    actionBarNumber.setText(it, TextView.BufferType.SPANNABLE)
                }
            }
        })

        // load loadRecommendUser 이벤트 observe
        followViewModel.loadRecommendUserData.observe(owner = this, observer = android.arch.lifecycle.Observer { evneteData ->
            evneteData?.let { isLoading ->
                userRecommendAdapter.dataLoading = isLoading
            }
        })

        // load loadFollowingEvent 이벤트 observe
        followViewModel.loadDefaultUserData.observe(owner = this, observer = android.arch.lifecycle.Observer { evneteData ->
            evneteData?.let { isLoading ->
                followingAdapter.dataLoading = isLoading
            }
        })
    }

    override fun onClick(v: View?) {
        v?.let {
            when (v.id) {
                R.id.actionBackBtn -> { //뒤로가기
                    followViewModel.backBtnAction()
                }
            }
        }
    }

    companion object {
        fun newIntent(context: Context, userId: String): Intent {
            val intent = Intent(context, FollowingActivity::class.java)
            intent.putExtra(IntentPassName.USER_ID, userId)

            return intent
        }
    }
}
