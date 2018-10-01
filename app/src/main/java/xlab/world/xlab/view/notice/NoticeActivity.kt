package xlab.world.xlab.view.notice

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.TextView
import kotlinx.android.synthetic.main.action_bar_default.*
import kotlinx.android.synthetic.main.activity_notice.*
import org.koin.android.architecture.ext.viewModel
import org.koin.android.ext.android.inject
import xlab.world.xlab.R
import xlab.world.xlab.adapter.recyclerView.NoticeAdapter
import xlab.world.xlab.utils.support.SPHelper
import xlab.world.xlab.utils.support.ViewFunction
import xlab.world.xlab.utils.view.dialog.DefaultProgressDialog
import xlab.world.xlab.utils.view.recyclerView.CustomItemDecoration
import xlab.world.xlab.utils.view.toast.DefaultToast

class NoticeActivity : AppCompatActivity(), View.OnClickListener {
    private val noticeViewModel: NoticeViewModel by viewModel()
    private val spHelper: SPHelper by inject()

    private lateinit var defaultToast: DefaultToast
    private lateinit var progressDialog: DefaultProgressDialog

    private lateinit var noticeAdapter: NoticeAdapter

    private val noticeSelectListener = View.OnClickListener { view ->
        val position = view.tag as Int
        noticeViewModel.readNotice(authorization = spHelper.authorization, noticeListData = noticeAdapter.getItem(position = position), position = position)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notice)

        onSetup()

        onBindEvent()

        observeViewModel()
    }

    override fun onBackPressed() {
        actionBackBtn.performClick()
    }

    private fun onSetup() {
        actionBarTitle.setText(getText(R.string.notice), TextView.BufferType.SPANNABLE)
        actionBtn.visibility = View.GONE

        defaultToast = DefaultToast(context = this)
        progressDialog = DefaultProgressDialog(context = this)

        // adapter & recycler view 초기화
        noticeAdapter = NoticeAdapter(context = this, selectListener = noticeSelectListener)
        recyclerView.adapter = noticeAdapter
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recyclerView.addItemDecoration(CustomItemDecoration(context = this, bottom = 8f))

        noticeViewModel.loadNoticeData(authorization = spHelper.authorization, page = 1)
    }

    private fun onBindEvent() {
        actionBackBtn.setOnClickListener(this) // 뒤로가기

        ViewFunction.onRecyclerViewScrolledDown(recyclerView = recyclerView) {
            ViewFunction.isScrolledRecyclerView(layoutManager = it as LinearLayoutManager, isLoading = noticeAdapter.dataLoading, total = noticeAdapter.dataTotal) { _ ->
                noticeViewModel.loadNoticeData(authorization = spHelper.authorization, page = noticeAdapter.dataNextPage)
            }
        }
    }

    private fun observeViewModel() {
        // UI 이벤트 observe
        noticeViewModel.uiData.observe(this, android.arch.lifecycle.Observer { uiData ->
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
                uiData.noticeData?.let {
                    if (it.nextPage <= 2 ) { // 요청한 page => 첫페이지
                        // post 없으면 no post 띄우기
                        textViewEmptyNotice.visibility =
                                if (it.items.isEmpty()) View.VISIBLE
                                else View.GONE
                        noticeAdapter.updateData(noticeData = it)
                    }
                    else
                        noticeAdapter.addData(noticeData = it)
                }
                uiData.noticeUpdatePosition?.let {
                    noticeAdapter.notifyItemChanged(it)
                    recyclerView.smoothScrollToPosition(it)
                }
            }
        })

        // load notice 이벤트 observe
        noticeViewModel.loadNoticeventData.observe(owner = this, observer = android.arch.lifecycle.Observer { eventData ->
            eventData?.let { _->
                eventData.status?.let { isLoading ->
                    noticeAdapter.dataLoading = isLoading
                }
            }
        })
    }

    override fun onClick(v: View?) {
        v?.let {
            when (v.id) {
                R.id.actionBackBtn -> { // 뒤로가기
                    setResult(Activity.RESULT_OK)
                    finish()
                }
            }
        }
    }

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, NoticeActivity::class.java)
        }
    }
}
