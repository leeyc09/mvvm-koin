package xlab.world.xlab.view.topicDetail

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.widget.NestedScrollView
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
import xlab.world.xlab.data.response.ResUserPetData
import xlab.world.xlab.utils.listener.DefaultListener
import xlab.world.xlab.utils.support.*
import xlab.world.xlab.utils.view.dialog.DefaultProgressDialog
import xlab.world.xlab.utils.view.recyclerView.CustomItemDecoration
import xlab.world.xlab.utils.view.toast.DefaultToast
import xlab.world.xlab.view.topicDetail.fragment.TopicPetDetailFragment

class TopicPetDetailActivity : AppCompatActivity(), View.OnClickListener {
    private val topicPetDetailViewModel: TopicPetDetailViewModel by viewModel()
    private val spHelper: SPHelper by inject()

    private var userId: String = ""

    private var resultCode = Activity.RESULT_CANCELED

    private lateinit var viewPagerAdapter: ViewStatePagerAdapter

    private lateinit var topicUsedGoodsAdapter: TopicUsedGoodsAdapter

    private lateinit var defaultToast: DefaultToast
    private lateinit var progressDialog: DefaultProgressDialog

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
                if (this.resultCode == Activity.RESULT_CANCELED ||
                        this.resultCode == ResultCodeData.TOPIC_DELETE)
                    this.resultCode = Activity.RESULT_OK
                when (requestCode) {
                    RequestCodeData.TOPIC_ADD -> { // 펫 추가
                        (0 until viewPagerAdapter.count).forEach { index ->
                            val frag = viewPagerAdapter.getItem(index) as TopicPetDetailFragment
                            frag.resetPetData(petNo = null)
                        }
                        viewPagerAdapter.addFragment(fragment = TopicPetDetailFragment.newFragment(userId = userId, petNo = viewPagerAdapter.count + 1), title = "")
                        imageViewPager.setCurrentItem(0, false)

                        topicPetDetailViewModel.changePetTotal(total = viewPagerAdapter.count)
                        topicPetDetailViewModel.loadPetDetailData(userId = userId, petNo = 1, reLoad = true)
                    }
                    RequestCodeData.TOPIC_EDIT -> { // 펫 수정
                        val frag = viewPagerAdapter.getItem(imageViewPager.currentItem) as TopicPetDetailFragment
                        frag.resetPetData(petNo = null)
                        topicPetDetailViewModel.loadPetDetailData(userId = userId, petNo = imageViewPager.currentItem + 1, reLoad = true)
                    }
                    RequestCodeData.GOODS_DETAIL -> { // update pet used item list
//                        loadPetUsedItemData(1, currentPetId, { petUsedItems ->
//                            petItemAdapter.updateData(petUsedItems)
//                        }, { isEnd ->
//                            isLoadingPetUsedItem = !isEnd
//                        })
                    }
                }
            }
            ResultCodeData.TOPIC_DELETE -> {
                if (this.resultCode == Activity.RESULT_CANCELED)
                    this.resultCode = ResultCodeData.TOPIC_DELETE
                when (requestCode) {
                    RequestCodeData.TOPIC_EDIT -> {
                        viewPagerAdapter.removeFragment(viewPagerAdapter.count - 1)
                        if (viewPagerAdapter.count > 0) {
                            (0 until viewPagerAdapter.count).forEach { index ->
                                val frag = viewPagerAdapter.getItem(index) as TopicPetDetailFragment
                                frag.resetPetData(petNo = null)
                            }

                            topicPetDetailViewModel.changePetTotal(total = viewPagerAdapter.count)
                            topicPetDetailViewModel.loadPetDetailData(userId = userId, petNo = imageViewPager.currentItem + 1, reLoad = true)
                        } else {
                            setResult(ResultCodeData.TOPIC_DELETE)
                            finish()
                        }
                    }
                }
            }
            ResultCodeData.LOGOUT_SUCCESS -> {
                setResult(ResultCodeData.LOGOUT_SUCCESS)
                finish()
            }
            ResultCodeData.LOGIN_SUCCESS -> {
                this.resultCode = ResultCodeData.LOGIN_SUCCESS
//                if (userId == SPHelper(this).userId) {
//                    actionAddBtn.visibility = View.VISIBLE
//                    petEditBtn.visibility = View.VISIBLE
//                } else {
//                    actionAddBtn.visibility = View.GONE
//                    petEditBtn.visibility = View.GONE
//                }
//
//                loadPetData(topicNum) { petData ->
//                    setPetInfoData(petData)
//                }
            }
        }
    }

    private fun onSetup() {
        userId = intent.getStringExtra(IntentPassName.USER_ID)

        textViewPetNo.setText(intent.getIntExtra(IntentPassName.PET_NO, 1).toString(), TextView.BufferType.SPANNABLE)

        defaultToast = DefaultToast(context = this)
        progressDialog = DefaultProgressDialog(context = this)

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
            viewPagerAdapter.addFragment(fragment = TopicPetDetailFragment.newFragment(userId = userId, petNo = petNo), title = "")
        }
        imageViewPager.adapter = viewPagerAdapter
        imageViewPager.setCurrentItem(intent.getIntExtra(IntentPassName.PET_NO, 1) - 1, false)

        topicPetDetailViewModel.setButtonVisible(userId = userId, loginUserId = spHelper.userId)
        topicPetDetailViewModel.changePetTotal(total = intent.getIntExtra(IntentPassName.PET_TOTAL, 1))
        topicPetDetailViewModel.loadPetDetailData(userId = userId, petNo = intent.getIntExtra(IntentPassName.PET_NO, 1))
    }

    private fun onBindEvent() {
        actionBackBtn.setOnClickListener(this) // 뒤로가기
        actionAddBtn.setOnClickListener(this) // 펫 추가하기
        petEditBtn.setOnClickListener(this) // 펫 수정하기

        ViewFunction.onViewPagerChangePosition(viewPager = imageViewPager) { position ->
            textViewPetNo.setText((position + 1).toString(), TextView.BufferType.SPANNABLE)

            val frag = viewPagerAdapter.getItem(position) as TopicPetDetailFragment
            frag.petData?.let {
                topicPetDetailViewModel.changePetData(petData = it, isMine = userId == spHelper.userId)
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
                uiData.btnVisibility?.let {
                    actionAddBtn.visibility = it
                    petEditBtn.visibility = it
                }
                uiData.petTotal?.let {
                    textViewPetTotal.tag = it
                    textViewPetTotal.setText(it.toString(), TextView.BufferType.SPANNABLE)
                }
                uiData.petCountVisibility?.let {
                    textViewPetNo.visibility = it
                    imageViewSlash.visibility = it
                    textViewPetTotal.visibility = it
                }
                uiData.petDetailData?.let {
                    textViewPetBreed.setText(it.breed, TextView.BufferType.SPANNABLE)
                    textViewPetBreed.setTextColor(it.topicColor)
                    textViewPetName.setText(it.name, TextView.BufferType.SPANNABLE)
                    textViewPetType.setText(it.type, TextView.BufferType.SPANNABLE)
                    imageViewPetGender.isSelected = it.gender
                    textViewPetAge.setText(it.age, TextView.BufferType.SPANNABLE)
                    textViewPetWeight.setText(it.weight, TextView.BufferType.SPANNABLE)
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
        topicPetDetailViewModel.loadPetDataEvent.observe(owner = this, observer = android.arch.lifecycle.Observer { loadPetDataEvent ->
            loadPetDataEvent?.let { _ ->
                loadPetDataEvent.petData?.let {
                    topicPetDetailViewModel.changePetData(petData = it, isMine = userId == spHelper.userId)
                }
                loadPetDataEvent.petUsedGoods?.let {
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
                    setResult(resultCode)
                    finish()
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
