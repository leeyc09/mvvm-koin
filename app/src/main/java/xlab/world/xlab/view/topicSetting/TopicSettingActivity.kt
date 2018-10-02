package xlab.world.xlab.view.topicSetting

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SimpleItemAnimator
import android.view.View
import android.widget.TextView
import kotlinx.android.synthetic.main.action_bar_default.*
import kotlinx.android.synthetic.main.activity_topic_setting.*
import org.koin.android.architecture.ext.viewModel
import org.koin.android.ext.android.inject
import xlab.world.xlab.R
import xlab.world.xlab.adapter.recyclerView.TopicSettingAdapter
import xlab.world.xlab.utils.support.PrintLog
import xlab.world.xlab.utils.support.RunActivity
import xlab.world.xlab.utils.support.SPHelper
import xlab.world.xlab.utils.support.ViewFunction
import xlab.world.xlab.utils.view.dialog.DefaultProgressDialog
import xlab.world.xlab.utils.view.recyclerView.CustomItemDecoration
import xlab.world.xlab.utils.view.toast.DefaultToast

class TopicSettingActivity : AppCompatActivity(), View.OnClickListener {
    private val topicSettingViewModel: TopicSettingViewModel by viewModel()
    private val spHelper: SPHelper by inject()

    private var resultCode = Activity.RESULT_CANCELED

    private lateinit var defaultToast: DefaultToast
    private lateinit var progressDialog: DefaultProgressDialog

    private lateinit var topicSettingAdapter: TopicSettingAdapter

    private val topicToggleListener = View.OnClickListener { view ->
        if (view.tag is Int) {
            val position = view.tag as Int
            topicSettingViewModel.changeTopicToggle(authorization = spHelper.authorization,
                    position = position,
                    topicData = topicSettingAdapter.getItem(position))
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_topic_setting)

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

                topicSettingViewModel.loadUserPetsData(userId = spHelper.userId, page = 1)
            }
        }
    }

    private fun onSetup() {
        actionBarTitle.setText(getString(R.string.topic_setting), TextView.BufferType.SPANNABLE)
        actionBtn.visibility = View.GONE

        // Toast, Dialog 초기화
        defaultToast = DefaultToast(context = this)
        progressDialog = DefaultProgressDialog(context = this)

        // topic recycler view & adapter 초기화
        topicSettingAdapter = TopicSettingAdapter(context = this, topicToggleListener = topicToggleListener)
        recyclerView.adapter = topicSettingAdapter
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recyclerView.addItemDecoration(CustomItemDecoration(context = this, bottom = 10f))
        (recyclerView.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false

        topicSettingViewModel.loadUserPetsData(userId = spHelper.userId, page = 1)
    }

    private fun onBindEvent() {
        actionBackBtn.setOnClickListener(this) // 뒤로가기
        addTopicBtn.setOnClickListener(this) // 토픽 추가하기

        ViewFunction.onRecyclerViewScrolledDown(recyclerView = recyclerView) {
            ViewFunction.isScrolledRecyclerView(layoutManager = it as LinearLayoutManager, isLoading = topicSettingAdapter.dataLoading, total = topicSettingAdapter.dataTotal) { _ ->
                topicSettingViewModel.loadUserPetsData(userId = spHelper.userId, page = topicSettingAdapter.dataNextPage)
            }
        }
    }

    private fun observeViewModel() {
        // UI 이벤트 observe
        topicSettingViewModel.uiData.observe(this, android.arch.lifecycle.Observer { uiData ->
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
                uiData.petData?.let {
                    if (it.nextPage <= 2 ) { // 요청한 page => 첫페이지
                        if (it.items.isEmpty()) { // topic 없음
                            addTopicBtn.visibility = View.VISIBLE
                            textLayout.visibility = View.GONE
                            recyclerView.visibility = View.GONE
                        } else { // topic 있음
                            addTopicBtn.visibility = View.GONE
                            textLayout.visibility = View.VISIBLE
                            recyclerView.visibility = View.VISIBLE
                        }
                        topicSettingAdapter.updateData(topicSettingData = it)
                    }
                    else {
                        topicSettingAdapter.addData(topicSettingData = it)
                    }
                }
                uiData.topicPosition?.let {
                    topicSettingAdapter.notifyItemChanged(it)
                    if (resultCode == Activity.RESULT_CANCELED)
                        resultCode = Activity.RESULT_OK
                }
            }
        })

        // load user pet list 이벤트 observe
        topicSettingViewModel.loadUserPetsListDataEvent.observe(owner = this, observer = android.arch.lifecycle.Observer { loadUserPetListEvent ->
            loadUserPetListEvent?.let { _ ->
                loadUserPetListEvent.isLoading?.let {
                    topicSettingAdapter.dataLoading = it
                }
            }
        })
    }

    override fun onClick(v: View?) {
        v?.let {
            when (v.id) {
                R.id.actionBackBtn -> { // 뒤로가기
                    setResult(resultCode)
                    finish()
                }
                R.id.addTopicBtn -> { // 토픽 추가하기
                    RunActivity.petEditActivity(context = this, petNo = null)
                }
            }
        }
    }

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, TopicSettingActivity::class.java)
        }
    }
}
