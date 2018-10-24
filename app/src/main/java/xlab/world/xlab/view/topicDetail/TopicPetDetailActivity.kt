package xlab.world.xlab.view.topicDetail

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.view.View
import android.widget.TextView
import kotlinx.android.synthetic.main.action_bar_topic.*
import kotlinx.android.synthetic.main.activity_topic_pet_detail.*
import org.koin.android.architecture.ext.viewModel
import org.koin.android.ext.android.inject
import xlab.world.xlab.R
import xlab.world.xlab.adapter.recyclerView.TopicUsedGoodsAdapter
import xlab.world.xlab.adapter.viewPager.ViewStatePagerAdapter
import xlab.world.xlab.utils.listener.DefaultListener
import xlab.world.xlab.utils.support.*
import xlab.world.xlab.utils.view.dialog.DefaultProgressDialog
import xlab.world.xlab.utils.view.recyclerView.CustomItemDecoration
import xlab.world.xlab.utils.view.toast.DefaultToast
import xlab.world.xlab.view.topicDetail.fragment.TopicPetDetailFragment

class TopicPetDetailActivity : AppCompatActivity(), View.OnClickListener {
    private val topicPetDetailViewModel: TopicPetDetailViewModel by viewModel()
    private val spHelper: SPHelper by inject()

    private lateinit var defaultToast: DefaultToast
    private lateinit var progressDialog: DefaultProgressDialog

    private lateinit var viewPagerAdapter: ViewStatePagerAdapter

    private lateinit var topicUsedGoodsAdapter: TopicUsedGoodsAdapter

    private lateinit var defaultListener: DefaultListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_topic_pet_detail)

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
                topicPetDetailViewModel.setResultCode(resultCode = Activity.RESULT_OK)
                when (requestCode) {
                    RequestCodeData.TOPIC_ADD -> { // 펫 추가
                        (0 until viewPagerAdapter.count).forEach { index ->
                            val frag = viewPagerAdapter.getItem(index) as TopicPetDetailFragment
                            frag.resetPetData(petNo = null)
                        }
                        viewPagerAdapter.addFragment(fragment = TopicPetDetailFragment.newFragment(
                                userId = intent.getStringExtra(IntentPassName.USER_ID),
                                petNo = viewPagerAdapter.count + 1),
                                title = "")
                        imageViewPager.setCurrentItem(0, false)

                        topicPetDetailViewModel.changePetTotal(total = viewPagerAdapter.count)
                        topicPetDetailViewModel.loadPetDetailData(userId = intent.getStringExtra(IntentPassName.USER_ID), petNo = 1, reLoad = true)
                    }
                    RequestCodeData.TOPIC_EDIT -> { // 펫 수정
                        val frag = viewPagerAdapter.getItem(imageViewPager.currentItem) as TopicPetDetailFragment
                        frag.resetPetData(petNo = null)
                        topicPetDetailViewModel.loadPetDetailData(userId = intent.getStringExtra(IntentPassName.USER_ID), petNo = imageViewPager.currentItem + 1, reLoad = true)
                    }
                    RequestCodeData.GOODS_DETAIL -> { // update pet used item list
                        topicPetDetailViewModel.loadPetUsedGoodsData(page = 1)
                    }
                }
            }
            ResultCodeData.TOPIC_DELETE -> {
                topicPetDetailViewModel.setResultCode(resultCode = ResultCodeData.TOPIC_DELETE)
                when (requestCode) {
                    RequestCodeData.TOPIC_EDIT -> {
                        viewPagerAdapter.removeFragment(viewPagerAdapter.count - 1)
                        if (viewPagerAdapter.count > 0) {
                            (0 until viewPagerAdapter.count).forEach { index ->
                                val frag = viewPagerAdapter.getItem(index) as TopicPetDetailFragment
                                frag.resetPetData(petNo = null)
                            }

                            topicPetDetailViewModel.changePetTotal(total = viewPagerAdapter.count)
                            topicPetDetailViewModel.loadPetDetailData(userId = intent.getStringExtra(IntentPassName.USER_ID), petNo = imageViewPager.currentItem + 1, reLoad = true)
                        } else {
                            setResult(ResultCodeData.TOPIC_DELETE)
                            finish()
                        }
                    }
                }
            }
            ResultCodeData.LOGIN_SUCCESS -> {
                topicPetDetailViewModel.setResultCode(resultCode = ResultCodeData.LOGIN_SUCCESS)
                topicPetDetailViewModel.setButtonVisible(userId = intent.getStringExtra(IntentPassName.USER_ID), loginUserId = spHelper.userId)
                topicPetDetailViewModel.loadPetDetailData(userId = intent.getStringExtra(IntentPassName.USER_ID), petNo = intent.getIntExtra(IntentPassName.PET_NO, 1))
            }
            ResultCodeData.LOGOUT_SUCCESS -> {
                topicPetDetailViewModel.setResultCode(resultCode = ResultCodeData.LOGOUT_SUCCESS)
                actionBackBtn.performClick()
            }
        }
    }

    private fun onSetup() {
        textViewPetNo.setText(intent.getIntExtra(IntentPassName.PET_NO, 1).toString(), TextView.BufferType.SPANNABLE)

        // Toast, Dialog 초기화
        defaultToast = DefaultToast(context = this)
        progressDialog = DefaultProgressDialog(context = this)

        // Listener 초기화
        defaultListener = DefaultListener(context = this)

        // topicUsedGoods adapter & recycler view 초기화
        topicUsedGoodsAdapter = TopicUsedGoodsAdapter(context = this,
                goodsListener = defaultListener.goodsListener)
        recyclerView.adapter = topicUsedGoodsAdapter
        recyclerView.layoutManager = GridLayoutManager(this, 3, GridLayoutManager.VERTICAL, false)
        recyclerView.addItemDecoration(CustomItemDecoration(context = this, offset = 0.5f))
        recyclerView.isNestedScrollingEnabled = false

        // 프래그먼트 & 뷰 페이저 초기화
        viewPagerAdapter = ViewStatePagerAdapter(manager = supportFragmentManager)
        (1..intent.getIntExtra(IntentPassName.PET_TOTAL, 1)).forEach { petNo ->
            viewPagerAdapter.addFragment(fragment = TopicPetDetailFragment.newFragment(userId = intent.getStringExtra(IntentPassName.USER_ID), petNo = petNo), title = "")
        }
        imageViewPager.adapter = viewPagerAdapter
        imageViewPager.setCurrentItem(intent.getIntExtra(IntentPassName.PET_NO, 1) - 1, false)

        topicPetDetailViewModel.setButtonVisible(userId = intent.getStringExtra(IntentPassName.USER_ID), loginUserId = spHelper.userId)
        topicPetDetailViewModel.changePetTotal(total = intent.getIntExtra(IntentPassName.PET_TOTAL, 1))
        topicPetDetailViewModel.loadPetDetailData(userId = intent.getStringExtra(IntentPassName.USER_ID), petNo = intent.getIntExtra(IntentPassName.PET_NO, 1))
    }

    private fun onBindEvent() {
        actionBackBtn.setOnClickListener(this) // 뒤로가기
        actionAddBtn.setOnClickListener(this) // 펫 추가하기
        petEditBtn.setOnClickListener(this) // 펫 수정하기

        ViewFunction.onViewPagerChangePosition(viewPager = imageViewPager) { position ->
            textViewPetNo.setText((position + 1).toString(), TextView.BufferType.SPANNABLE)

            val frag = viewPagerAdapter.getItem(position) as TopicPetDetailFragment
            frag.petData?.let {
                topicPetDetailViewModel.changePetData(petData = it,
                        userId = intent.getStringExtra(IntentPassName.USER_ID),
                        loginUserId = spHelper.userId)
            }
            frag.petUsedGoods?.let {
                topicPetDetailViewModel.changePetUsedGoodsData(petUsedGoods = it)
            }
        }

        ViewFunction.isNestedScrollViewScrolledDown(nestedScrollView = scrollView) { isScrolled ->
            if (isScrolled && !topicUsedGoodsAdapter.dataLoading) {
                topicPetDetailViewModel.loadPetUsedGoodsData(page = topicUsedGoodsAdapter.dataNextPage)
            }
        }
    }

    private fun observeViewModel() {
        // UI 이벤트 observe
        topicPetDetailViewModel.uiData.observe(this, android.arch.lifecycle.Observer { uiData ->
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
                uiData.editBtnVisibility?.let {
                    petEditBtn.visibility = it
                }
                uiData.addBtnVisibility?.let {
                    actionAddBtn.visibility = it
                }
                uiData.petTotal?.let {
                    textViewPetTotal.tag = it
                    textViewPetTotal.setText(it.toString(), TextView.BufferType.SPANNABLE)
                }
                uiData.petCountVisibility?.let {
                    cntLayout.visibility = it
                }
                uiData.petBreed?.let {
                    textViewPetBreed.setText(it, TextView.BufferType.SPANNABLE)
                }
                uiData.topicColor?.let {
                    textViewPetBreed.setTextColor(it)
                }
                uiData.petType?.let {
                    textViewPetType.setText(it, TextView.BufferType.SPANNABLE)
                }
                uiData.petName?.let {
                    textViewPetName.setText(it, TextView.BufferType.SPANNABLE)
                }
                uiData.petGender?.let {
                    imageViewPetGender.isSelected = it
                }
                uiData.petAge?.let {
                    textViewPetAge.setText(it, TextView.BufferType.SPANNABLE)
                }
                uiData.petWeight?.let {
                    textViewPetWeight.setText(it, TextView.BufferType.SPANNABLE)
                }
                uiData.petUsedGoods?.let {
                    if (it.nextPage <= 2 ) // 요청한 page => 첫페이지
                        topicUsedGoodsAdapter.updateData(topicUsedGoodsData = it)
                    else
                        topicUsedGoodsAdapter.addData(topicUsedGoodsData = it)

                    topicUsedGoodsAdapter.dataLoading = it.items.isEmpty()
                }
            }
        })

        // load pet data 이벤트 observe
        topicPetDetailViewModel.loadPetData.observe(owner = this, observer = android.arch.lifecycle.Observer { eventData ->
            eventData?.let { _ ->
                eventData.petData?.let {
                    topicPetDetailViewModel.loadPetUsedGoodsData(page = 1)
                    topicPetDetailViewModel.changePetData(petData = it,
                            userId = intent.getStringExtra(IntentPassName.USER_ID),
                            loginUserId = spHelper.userId)
                }
                eventData.petUsedGoods?.let {
                    topicPetDetailViewModel.changePetUsedGoodsData(petUsedGoods = it)
                }
            }
        })

        // load pet used goods 이벤트 observe
        topicPetDetailViewModel.loadPetUsedGoodsEvent.observe(owner = this, observer = android.arch.lifecycle.Observer { loadPetUsedGoodsEvent ->
            loadPetUsedGoodsEvent?.let { _ ->
                loadPetUsedGoodsEvent.status?.let {  isLoading ->
                    topicUsedGoodsAdapter.dataLoading = isLoading
                }
            }
        })
    }

    override fun onClick(v: View?) {
        v?.let {
            when (v.id) {
                R.id.actionBackBtn -> { // 뒤로가기
                    topicPetDetailViewModel.backBtnAction()
                }
                R.id.actionAddBtn -> { // 펫 추가하기
                    RunActivity.petEditActivity(context = this, petNo = null)
                }
                R.id.petEditBtn -> { // 펫 수정하기
                    RunActivity.petEditActivity(context = this, petNo = textViewPetNo.text.toString())
                }
            }
        }
    }

    companion object {
        fun newIntent(context: Context, userId: String, petNo: Int, petTotal: Int): Intent {
            val intent = Intent(context, TopicPetDetailActivity::class.java)
            intent.putExtra(IntentPassName.USER_ID, userId)
            intent.putExtra(IntentPassName.PET_NO, petNo)
            intent.putExtra(IntentPassName.PET_TOTAL, petTotal)

            return intent
        }
    }
}
