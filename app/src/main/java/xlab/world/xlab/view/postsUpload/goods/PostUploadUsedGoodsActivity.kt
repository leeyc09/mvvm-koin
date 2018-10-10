package xlab.world.xlab.view.postsUpload.goods

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SimpleItemAnimator
import android.view.View
import kotlinx.android.synthetic.main.action_bar_default.*
import kotlinx.android.synthetic.main.activity_post_upload_used_goods.*
import org.koin.android.architecture.ext.viewModel
import org.koin.android.ext.android.inject
import xlab.world.xlab.R
import xlab.world.xlab.adapter.recyclerView.SelectUsedGoodsAdapter
import xlab.world.xlab.adapter.recyclerView.SelectedUsedGoodsAdapter
import xlab.world.xlab.data.adapter.SelectUsedGoodsListData
import xlab.world.xlab.utils.support.AppConstants
import xlab.world.xlab.utils.support.IntentPassName
import xlab.world.xlab.utils.support.SPHelper
import xlab.world.xlab.utils.support.ViewFunction
import xlab.world.xlab.utils.view.dialog.DefaultProgressDialog
import xlab.world.xlab.utils.view.recyclerView.CustomItemDecoration
import xlab.world.xlab.utils.view.toast.DefaultToast

class PostUploadUsedGoodsActivity : AppCompatActivity(), View.OnClickListener {
    private val postUsedGoodsViewModel: PostUsedGoodsViewModel by viewModel()
    private val spHelper: SPHelper by inject()

    private lateinit var defaultToast: DefaultToast
    private lateinit var progressDialog: DefaultProgressDialog

    private lateinit var selectedUsedGoodsAdapter: SelectedUsedGoodsAdapter
    private lateinit var selectUsedGoodsAdapter: SelectUsedGoodsAdapter

    private val selectedDeleteListener = View.OnClickListener { view ->
        postUsedGoodsViewModel.deleteSelectedUsedGoods(selectedGoodsPosition = view.tag as Int, selectedUsedGoods = selectUsedGoodsAdapter.getSelectedGoods())
    }
    private val selectUsedGoodsListener = View.OnClickListener { view ->
        val position = view.tag as Int
        postUsedGoodsViewModel.selectUsedGoods(position = position, usedGoodsData = selectUsedGoodsAdapter.getItem(position = position))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_upload_used_goods)

        onSetup()

        onBindEvent()

        observeViewModel()
    }

    override fun onBackPressed() {
        actionBackBtn.performClick()
    }
    
    private fun onSetup() {
        actionBarTitle.visibility = View.GONE
        actionBtn.isEnabled = false

        defaultToast = DefaultToast(context = this)
        progressDialog = DefaultProgressDialog(context = this)

        // selected used goods adapter & recycler 초기화
        selectedUsedGoodsAdapter = SelectedUsedGoodsAdapter(context = this, deleteListener = selectedDeleteListener)
        selectedGoodsRecyclerView.adapter = selectedUsedGoodsAdapter
        selectedGoodsRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        selectedGoodsRecyclerView.addItemDecoration(CustomItemDecoration(context= this, right = 20f))

        // select used goods adapter & recycler 초기화
        selectUsedGoodsAdapter = SelectUsedGoodsAdapter(context = this, selectListener = selectUsedGoodsListener)
        usedGoodsRecyclerView.adapter = selectUsedGoodsAdapter
        usedGoodsRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        usedGoodsRecyclerView.addItemDecoration(CustomItemDecoration(context= this, right = 20f, left = 20f))
        (usedGoodsRecyclerView.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false

        val selectedData = intent.getParcelableArrayListExtra<SelectUsedGoodsListData>(IntentPassName.SELECTED_USED_GOODS)
        postUsedGoodsViewModel.setSelectedUsedGoodsData(selectedData = selectedData, dataType = AppConstants.SELECTED_GOODS_ONLY_THUMB)
    }

    private fun onBindEvent() {
        actionBackBtn.setOnClickListener(this) // 뒤로가기
        actionBtn.setOnClickListener(this) // 완료

        ViewFunction.onRecyclerViewScrolledDown(recyclerView = usedGoodsRecyclerView) {
            ViewFunction.isScrolledRecyclerView(layoutManager = it as LinearLayoutManager, isLoading = selectUsedGoodsAdapter.dataLoading, total = selectUsedGoodsAdapter.dataTotal) {_->
                postUsedGoodsViewModel.loadUsedGoodsData(userId = spHelper.userId, goodsType = AppConstants.USED_GOODS_PET, page = selectUsedGoodsAdapter.dataNextPage)
            }
        }
    }

    private fun observeViewModel() {
        // UI 이벤트 observe
        postUsedGoodsViewModel.uiData.observe(this, android.arch.lifecycle.Observer { uiData ->
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
                uiData.selectedUsedGoodsData?.let {
                    selectedLayout.visibility =
                            if (it.isEmpty()) View.GONE
                            else View.VISIBLE
                    selectedUsedGoodsAdapter.updateData(selectUsedGoodsData = it)
                    postUsedGoodsViewModel.loadUsedGoodsData(userId = spHelper.userId, goodsType = AppConstants.USED_GOODS_PET, page = 1)
                }
                uiData.updateSelectedUsedGoodsData?.let {
                    selectedLayout.visibility =
                            if (it.isEmpty()) View.GONE
                            else View.VISIBLE
                    selectedUsedGoodsAdapter.updateData(selectUsedGoodsData = it)
                    actionBtn.isEnabled = true
                }
                uiData.selectedUsedGoodsScrollIndex?.let {
                    selectedGoodsRecyclerView.scrollToPosition(it)
                }
                uiData.usedGoodsData?.let {
                    if (it.nextPage <= 2 ) { // 요청한 page => 첫페이지
                        selectUsedGoodsAdapter.updateData(selectUsedGoodsData = it)
                        textViewEmptyUsedGoods.visibility =
                                if (it.items.isEmpty()) View.VISIBLE
                                else View.GONE
                    }
                    else
                        selectUsedGoodsAdapter.addData(selectUsedGoodsData = it)
                }
                uiData.usedGoodsUpdateIndex?.let {
                    selectUsedGoodsAdapter.notifyItemChanged(it)
                }
            }
        })

        // load used goods 이벤트 observe
        postUsedGoodsViewModel.loadUsedGoodsEventData.observe(owner = this, observer = android.arch.lifecycle.Observer { eventData ->
            eventData?.let { _->
                eventData.status?.let { isLoading ->
                    selectUsedGoodsAdapter.dataLoading = isLoading
                }
            }
        })
    }

    override fun onClick(v: View?) {
        v?.let {
            when (v.id) {
                R.id.actionBackBtn -> { // 뒤로가기
                    setResult(Activity.RESULT_CANCELED)
                    finish()
                }
                R.id.actionBtn -> { // 완료
                    intent.putParcelableArrayListExtra(IntentPassName.SELECTED_USED_GOODS, postUsedGoodsViewModel.getSelectedUsedGoodsData())
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                }
            }
        }
    }

    companion object {
        fun newIntent(context: Context, selectedItem: ArrayList<SelectUsedGoodsListData>): Intent {
            val intent = Intent(context, PostUploadUsedGoodsActivity::class.java)
            intent.putParcelableArrayListExtra(IntentPassName.SELECTED_USED_GOODS, selectedItem)

            return intent
        }
    }
}
