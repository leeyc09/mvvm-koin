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
import kotlinx.android.synthetic.main.activity_follower.*
import org.koin.android.architecture.ext.viewModel
import org.koin.android.ext.android.inject
import xlab.world.xlab.R
import xlab.world.xlab.adapter.recyclerView.UserDefaultAdapter
import xlab.world.xlab.utils.listener.DefaultListener
import xlab.world.xlab.utils.listener.UserDefaultListener
import xlab.world.xlab.utils.support.*
import xlab.world.xlab.utils.view.dialog.DefaultProgressDialog
import xlab.world.xlab.utils.view.recyclerView.CustomItemDecoration
import xlab.world.xlab.utils.view.toast.DefaultToast

class FollowerActivity : AppCompatActivity(), View.OnClickListener {
    private val followViewModel: FollowViewModel by viewModel()
    private val spHelper: SPHelper by inject()

    private var resultCode = Activity.RESULT_CANCELED
    private var userId = ""

    private lateinit var followerAdapter: UserDefaultAdapter

    private lateinit var defaultToast: DefaultToast
    private lateinit var progressDialog: DefaultProgressDialog

    private lateinit var defaultListener: DefaultListener
    private lateinit var userDefaultListener: UserDefaultListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_follower)

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
                        followViewModel.loadFollower(authorization = spHelper.authorization, userId = userId, page = 1, loadingBar = null)
                    }
                }
            }
            ResultCodeData.LOGIN_SUCCESS -> { // login -> reload all data
                this.resultCode = ResultCodeData.LOGIN_SUCCESS
                followViewModel.loadFollower(authorization = spHelper.authorization, userId = userId, page = 1, loadingBar = null)
            }
            ResultCodeData.LOGOUT_SUCCESS -> { // logout -> finish activity
                setResult(ResultCodeData.LOGOUT_SUCCESS)
                finish()
            }
        }
    }

    private fun onSetup() {
        userId = intent.getStringExtra(IntentPassName.USER_ID)

        actionBarTitle.setText(getString(R.string.follower), TextView.BufferType.SPANNABLE)

        // Toast, Dialog 초기화
        defaultToast = DefaultToast(context = this)
        progressDialog = DefaultProgressDialog(context = this)

        defaultListener = DefaultListener(context = this)
        userDefaultListener = UserDefaultListener(context = this,
                followUserEvent = { position ->
                    followViewModel.userFollow(authorization = spHelper.authorization, position = position,
                            userData = followerAdapter.getItem(position), recommendUserData = null)
                })

        // follower recycler view & adapter 초기화
        followerAdapter = UserDefaultAdapter(context = this,
                followListener = userDefaultListener.followListener,
                profileListener = defaultListener.profileListener)
        recyclerView.adapter = followerAdapter
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recyclerView.addItemDecoration(CustomItemDecoration(context = this, bottom = 10f))
        (recyclerView.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false

        followViewModel.loadFollower(authorization = spHelper.authorization, userId = userId, page = 1)
    }

    private fun onBindEvent() {
        actionBackBtn.setOnClickListener(this) // 뒤로가기

        ViewFunction.onRecyclerViewScrolledDown(recyclerView = recyclerView) {
            ViewFunction.isScrolledRecyclerView(layoutManager = it as LinearLayoutManager, isLoading = followerAdapter.dataLoading, total = followerAdapter.dataTotal) { _ ->
                followViewModel.loadFollower(authorization = spHelper.authorization, userId = userId, page = followerAdapter.dataNextPage)
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
                uiData.userData?.let {
                    if (it.nextPage <= 2) { // 요청한 page => 첫페이지
                        followerAdapter.updateData(userDefaultData = it)
                        actionBarNumber.setText(SupportData.countFormat(it.total), TextView.BufferType.SPANNABLE)

                        textViewNoFollower.visibility =
                                if (it.items.isEmpty()) View.VISIBLE
                                else View.INVISIBLE
                    } else
                        followerAdapter.addData(userDefaultData = it)
                }
                uiData.followCnt?.let {
                    actionBarNumber.tag = it
                    actionBarNumber.setText(SupportData.countFormat(it), TextView.BufferType.SPANNABLE)
                }
                uiData.userUpdatePosition?.let {
                    if (this.resultCode == Activity.RESULT_CANCELED)
                        this.resultCode = Activity.RESULT_OK
                    followerAdapter.notifyItemChanged(it)
                }
            }
        })

        // load follower 이벤트 observe
        followViewModel.loadFollowEvent.observe(owner = this, observer = android.arch.lifecycle.Observer { loadFollowerEvent ->
            loadFollowerEvent?.let { _ ->
                loadFollowerEvent.status?.let { isLoading ->
                    followerAdapter.dataLoading = isLoading
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
            val intent = Intent(context, FollowerActivity::class.java)
            intent.putExtra(IntentPassName.USER_ID, userId)

            return intent
        }
    }
}
