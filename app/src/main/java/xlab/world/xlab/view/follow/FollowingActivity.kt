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

    private var resultCode = Activity.RESULT_CANCELED
    private var userId = ""

    private lateinit var userRecommendAdapter: UserRecommendAdapter
    private lateinit var followingAdapter: UserDefaultAdapter

    private lateinit var defaultToast: DefaultToast
    private lateinit var progressDialog: DefaultProgressDialog

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

        when (resultCode) {
            Activity.RESULT_OK -> {
                if (this.resultCode == Activity.RESULT_CANCELED)
                    this.resultCode = Activity.RESULT_OK
                when (requestCode) {
                    RequestCodeData.PROFILE -> { // 프로필
                        followViewModel.loadRecommendUser(authorization = spHelper.authorization, page = 1, loadingBar = null)
                        followViewModel.loadFollowing(authorization = spHelper.authorization, userId = userId, page = 1, loadingBar = null)
                    }
                }
            }
            ResultCodeData.LOGOUT_SUCCESS -> {
                setResult(ResultCodeData.LOGOUT_SUCCESS)
                finish()
            }
            ResultCodeData.LOGIN_SUCCESS -> {
                this.resultCode = ResultCodeData.LOGIN_SUCCESS
                followViewModel.loadRecommendUser(authorization = spHelper.authorization, page = 1, loadingBar = null)
                followViewModel.loadFollowing(authorization = spHelper.authorization, userId = userId, page = 1, loadingBar = null)
            }
        }
    }

    private fun onSetup() {
        userId = intent.getStringExtra(IntentPassName.USER_ID)

        actionBarTitle.setText(getString(R.string.following), TextView.BufferType.SPANNABLE)

        // Toast, Dialog 초기화
        defaultToast = DefaultToast(context = this)
        progressDialog = DefaultProgressDialog(context = this)

        defaultListener = DefaultListener(context = this)
        userDefaultListener = UserDefaultListener(context = this,
                followUserEvent = { position ->
                    followViewModel.userFollow(authorization = spHelper.authorization, position = position,
                            userData = followingAdapter.getItem(position), recommendUserData = null, followCnt = actionBarNumber.tag as Int)
                })

        // recommend recycler view & adapter 초기화
        userRecommendAdapter = UserRecommendAdapter(context = this,
                followListener = View.OnClickListener { view ->
                    followViewModel.userFollow(authorization = spHelper.authorization, position = view.tag as Int,
                            userData = null, recommendUserData = userRecommendAdapter.getItem(view.tag as Int), followCnt = actionBarNumber.tag as Int)
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
        followViewModel.loadFollowing(authorization = spHelper.authorization, userId = userId, page = 1)
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
                followViewModel.loadFollowing(authorization = spHelper.authorization, userId = userId, page = followingAdapter.dataNextPage)
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
                uiData.recommendUserData?.let {
                    if (it.nextPage <= 2) { // 요청한 page => 첫페이지
                        userRecommendAdapter.updateData(userRecommendData = it)

                        recommendLayout.visibility =
                                if (it.items.isEmpty()) View.GONE
                                else View.VISIBLE
                    } else
                        userRecommendAdapter.addData(userRecommendData = it)
                }
                uiData.userData?.let {
                    if (it.nextPage <= 2) { // 요청한 page => 첫페이지
                        followingAdapter.updateData(userDefaultData = it)
                    } else
                        followingAdapter.addData(userDefaultData = it)
                }
                uiData.followCnt?.let {
                    actionBarNumber.tag = it
                    actionBarNumber.setText(SupportData.countFormat(it), TextView.BufferType.SPANNABLE)
                }
                uiData.userUpdatePosition?.let {
                    if (this.resultCode == Activity.RESULT_CANCELED)
                        this.resultCode = Activity.RESULT_OK
                    userRecommendAdapter.notifyItemChanged(it)
                    followingAdapter.notifyItemChanged(it)
                }
            }
        })

        // load loadRecommendUser 이벤트 observe
        followViewModel.loadRecommendUserEvent.observe(owner = this, observer = android.arch.lifecycle.Observer { loadRecommendUserEvent ->
            loadRecommendUserEvent?.let { _ ->
                loadRecommendUserEvent.status?.let { isLoading ->
                    userRecommendAdapter.dataLoading = isLoading
                }
            }
        })

        // load loadFollowingEvent 이벤트 observe
        followViewModel.loadFollowEvent.observe(owner = this, observer = android.arch.lifecycle.Observer { loadFollowerEvent ->
            loadFollowerEvent?.let { _ ->
                loadFollowerEvent.status?.let { isLoading ->
                    followingAdapter.dataLoading = isLoading
                }
            }
        })
    }

    override fun onClick(v: View?) {
        v?.let {
            when (v.id) {
                R.id.actionBackBtn -> { //뒤로가기
                    setResult(resultCode)
                    finish()
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
