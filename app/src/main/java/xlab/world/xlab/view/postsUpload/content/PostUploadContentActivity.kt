package xlab.world.xlab.view.postsUpload.content

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.TextView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.action_bar_default.*
import kotlinx.android.synthetic.main.activity_post_upload_content.*
import org.koin.android.architecture.ext.viewModel
import org.koin.android.ext.android.inject
import xlab.world.xlab.R
import xlab.world.xlab.adapter.recyclerView.*
import xlab.world.xlab.data.adapter.SelectUsedGoodsListData
import xlab.world.xlab.utils.font.CustomFont
import xlab.world.xlab.utils.support.*
import xlab.world.xlab.utils.view.dialog.DefaultProgressDialog
import xlab.world.xlab.utils.view.hashTag.HashTagHelper
import xlab.world.xlab.utils.view.recyclerView.CustomItemDecoration
import xlab.world.xlab.utils.view.toast.DefaultToast
import xlab.world.xlab.view.postsUpload.goods.PostUsedGoodsViewModel

class PostUploadContentActivity : AppCompatActivity(), View.OnClickListener {
    private val postContentViewModel: PostContentViewModel by viewModel()
    private val postUsedGoodsViewModel: PostUsedGoodsViewModel by viewModel()
    private val spHelper: SPHelper by inject()

    private lateinit var defaultToast: DefaultToast
    private lateinit var progressDialog: DefaultProgressDialog

    private lateinit var hashTagHelper: HashTagHelper

    private lateinit var postUploadPictureAdapter: PostUploadPictureAdapter // 업로드 이미지 adapter
    private lateinit var recentHashTagAdapter: RecentHashTagAdapter // 최근 검색 기록 adapter
    private lateinit var searchHashTagAdapter: SearchHashTagAdapter // 해시 태그 adapter
    private lateinit var selectedUsedGoodsAdapter: SelectedUsedGoodsAdapter // 선택한 사용 제품 adapter

    private val hashTagWritingListener = object: HashTagHelper.WritingListener {
        override fun onWritingHashTag(hashTagSign: Char, hashTag: String, start: Int, end: Int) {
            if (hashTagSign == AppConstants.HASH_TAG_SIGN) {
                postContentViewModel.setHashTagStartEndPoint(start = start, end = end)
                postContentViewModel.loadSearchHashTagData(authorization = spHelper.authorization, searchText = hashTag, page = 1)
            }
        }
        override fun onLastWritingHashTag(hashTagSign: Char) {
            if (hashTagSign == AppConstants.HASH_TAG_SIGN) { // hash tag (#)
                // show recent used hash tag recycler view
                hashTagRecyclerView.adapter = recentHashTagAdapter
                hashTagRecyclerView.scrollToPosition(0)
            }
        }
        override fun onEditTextState(state: Int) {
            postContentViewModel.changeHashTagState(hashTagNowState = state)
        }
    }
    private val recentHashTagSelectListener = View.OnClickListener { view ->
        editTextContent.text?.insert(editTextContent.selectionStart, view.tag as String)

        normalPopupLayout.visibility = View.VISIBLE
        hashTagPopupLayout.visibility = View.GONE
    }
    private val searchHashTagSelectListener = View.OnClickListener { view ->
        editTextContent.text = editTextContent.text?.replace(postContentViewModel.hashTagStartEndPoint.x, postContentViewModel.hashTagStartEndPoint.y, view.tag as String)
        editTextContent.setSelection(postContentViewModel.hashTagStartEndPoint.x + (view.tag as String).length)

        normalPopupLayout.visibility = View.VISIBLE
        hashTagPopupLayout.visibility = View.GONE
    }
    private val selectedDeleteListener = View.OnClickListener { view ->
        postUsedGoodsViewModel.deleteSelectedUsedGoods(selectIndex = view.tag as Int)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_upload_content)

        onSetup()

        onBindEvent()

        observeViewModel()
    }

    override fun onDestroy() {
        super.onDestroy()
        postContentViewModel.deleteProfileImage()
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
                when (requestCode) {
                    RequestCodeData.POST_UPLOAD -> { // 제품 대크 변경 완료
                        data?.let {
                            val selectedData = data.getParcelableArrayListExtra<SelectUsedGoodsListData>(IntentPassName.SELECTED_USED_GOODS)
                            postUsedGoodsViewModel.setSelectedUsedGoodsData(selectedData = selectedData, dataType = AppConstants.SELECTED_GOODS_WITH_INFO)
                        }
                    }
                }
            }
        }
    }

    private fun onSetup() {
        // 제목 숨기기, 다음 버튼 활성화
        actionBarTitle.visibility = View.GONE
        actionBtn.isEnabled = true
        // popup layout 숨기기 (normal popup 활성화, hash tag popup 비활성화)
        popupLayout.visibility = View.GONE
        normalPopupLayout.visibility = View.VISIBLE
        hashTagPopupLayout.visibility = View.GONE

        // Toast, Dialog 초기화
        defaultToast = DefaultToast(context = this)
        progressDialog = DefaultProgressDialog(context = this)

        // hash tag helper 초기화
        hashTagHelper = HashTagHelper(
                hashTagCharsColor = hashMapOf(AppConstants.HASH_TAG_SIGN to ResourcesCompat.getColor(resources, R.color.color000000, null)),
                hashTagCharsFont = hashMapOf(AppConstants.HASH_TAG_SIGN to CustomFont.getTypeface(CustomFont.notoSansCJKkrBold, this)!!),
                onHashTagWritingListener = hashTagWritingListener,
                onHashTagClickListener = null,
                additionalHashTagChar = ArrayList())
        hashTagHelper.handle(editTextContent)

        // post upload image adapter & recycler 초기화
        postUploadPictureAdapter = PostUploadPictureAdapter(context = this)
        imageRecyclerView.adapter = postUploadPictureAdapter
        imageRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        imageRecyclerView.addItemDecoration(CustomItemDecoration(context = this, right = 0.5f, left = 0.5f))

        // recent, search hash tag adapter & recycler 초기화
        recentHashTagAdapter = RecentHashTagAdapter(context = this, selectListener = recentHashTagSelectListener)
        searchHashTagAdapter = SearchHashTagAdapter(context = this, selectListener = searchHashTagSelectListener)
        hashTagRecyclerView.adapter = recentHashTagAdapter
        hashTagRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        // selected used goods adapter & recycler 초기화
        selectedUsedGoodsAdapter = SelectedUsedGoodsAdapter(context = this, deleteListener = selectedDeleteListener)
        goodsRecyclerView.adapter = selectedUsedGoodsAdapter
        goodsRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        goodsRecyclerView.isNestedScrollingEnabled = false

        postContentViewModel.initPostType(postId = intent.getStringExtra(IntentPassName.POST_ID))
        postContentViewModel.loadRecentHashTagData(authorization = spHelper.authorization)

    }

    private fun onBindEvent() {
        actionBackBtn.setOnClickListener(this) // 뒤로가기
        actionBtn.setOnClickListener(this) // 저장하기
        youtubeThumbnailLayout.setOnClickListener(this) // 유튜브 썸네일 클릭
        hashTagBtn.setOnClickListener(this) // 해시태그 단축키
        userTagBtn.setOnClickListener(this) // 유저태그 단축키
        finishBtn.setOnClickListener(this) // 팝업 내리기
        usedGoodsTitleLayout.setOnClickListener(this) // 사용한 제품 추가 레이아웃

        ViewFunction.showUpKeyboardLayout(view = mainLayout) { visibility ->
            // 키보드 보일경우, 팝업 레이아웃 보여주고 내용 입력란만 보이게함
            if (visibility == View.VISIBLE) {
                popupLayout.visibility = View.VISIBLE
                usedItemLayout.visibility = View.GONE
                scrollView.setBackgroundColor(ResourcesCompat.getColor(resources, R.color.colorFFFFFF, null))
            } else {
                popupLayout.visibility = View.GONE
                usedItemLayout.visibility = View.VISIBLE
                scrollView.setBackgroundColor(ResourcesCompat.getColor(resources, R.color.colorF5F5F5, null))

                postContentViewModel.changeHashTagState(hashTagNowState = HashTagHelper.stateNoTag)
            }
       }
    }

    private fun observeViewModel() {
        // TODO: Post content view model event
        // UI 이벤트 observe
        postContentViewModel.uiData.observe(this, android.arch.lifecycle.Observer { uiData ->
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
                uiData.youtubeThumbnailVisible?.let {
                    youtubeThumbnailLayout.visibility = it
                }
                uiData.youtubeThumbnailUrl?.let {
                    Glide.with(this)
                            .load(it)
                            .into(youtubeThumbnailView)
                }
                uiData.postUploadPictureData?.let {
                    postUploadPictureAdapter.linkData(postUploadPictureData = it)
                }
                uiData.recentHashTagData?.let {
                    recentHashTagAdapter.updateData(recentHashTagData = it)
                }
                uiData.normalPopupVisibility?.let {
                    normalPopupLayout.visibility = it
                }
                uiData.hashTagPopupVisibility?.let {
                    hashTagPopupLayout.visibility = it
                }
                uiData.searchHashTagData?.let {
                    if (it.nextPage <= 2 )  // 요청한 page => 첫페이지
                        searchHashTagAdapter.updateData(searchHashTagData = it)
                    else
                        searchHashTagAdapter.addData(searchHashTagData = it)

                    hashTagRecyclerView.adapter = searchHashTagAdapter
                    hashTagRecyclerView.scrollToPosition(0)
                }
            }
        })

        // post type 체크 이벤트 observe
        postContentViewModel.postTypeData.observe(owner = this, observer = android.arch.lifecycle.Observer { eventData ->
            eventData?.let { isPostUpload ->
                if (isPostUpload) { // 포스트 업로드
                    postContentViewModel.initPostUploadData(imagePathList = intent.getStringArrayListExtra(IntentPassName.IMAGE_PATH_LIST),
                            youTubeVideoId = intent.getStringExtra(IntentPassName.YOUTUBE_VIDEO_ID))
                } else { // 포스트 업데이트 -> 해당 포스트 정보 불러오기
                    postContentViewModel.loadPost(context = this, authorization = spHelper.authorization)
                }
            }
        })

        // search hash tag 이벤트 observe
        postContentViewModel.searchHashTagEvent.observe(owner = this, observer = android.arch.lifecycle.Observer { eventData ->
            eventData?.let { _->
                eventData.status?.let { isLoading ->
                    searchHashTagAdapter.dataLoading = isLoading
                }
            }
        })

        // load post 이벤트 observe
        postContentViewModel.loadPostData.observe(owner = this, observer = android.arch.lifecycle.Observer { eventData ->
            eventData?.let { _->
                eventData.content?.let {
                    editTextContent.setText(it)
                }
                eventData.goodsData?.let {
                    postUsedGoodsViewModel.setSelectedUsedGoodsData(selectedData = it, dataType = AppConstants.SELECTED_GOODS_WITH_INFO)
                }
                eventData.isFail?.let {
                    actionBackBtn.performClick()
                }
            }
        })

        // save post 이벤트 observe
        postContentViewModel.savePostData.observe(owner = this, observer = android.arch.lifecycle.Observer { eventData ->
            eventData?.let { isSuccess ->
                if (isSuccess) {
                    setResult(Activity.RESULT_OK)
                    finish()
                }
            }
        })

        // TODO: Post upload used goods view model event
        // UI 이벤트 observe
        postUsedGoodsViewModel.uiData.observe(this, android.arch.lifecycle.Observer { uiData ->
            uiData?.let { _ ->
                uiData.selectedUsedGoodsData?.let {
                    selectedUsedGoodsAdapter.linkData(selectUsedGoodsData = it)
                }
                uiData.selectedUsedGoodsDataUpdate?.let {
                    selectedUsedGoodsAdapter.notifyDataSetChanged()
                }
                uiData.selectedUsedGoodsDataCnt?.let {
                    textViewUsedGoodsCnt.setText(it, TextView.BufferType.SPANNABLE)
                }
            }
        })

        // finish select 이벤트 observe
        postUsedGoodsViewModel.finishSelectData.observe(owner = this, observer = android.arch.lifecycle.Observer { eventData ->
            eventData?.let {_->
                eventData.selectData?.let {
                    RunActivity.postUploadUsedGoodsActivity(context = this,
                            selectedItem = it)
                }
                eventData.selectData2?.let {
                    postContentViewModel.savePost(authorization = spHelper.authorization,
                            content = getPostContent(),
                            hashTags = hashTagHelper.getAllHashTags(),
                            goodsData = it,
                            imagePaths = intent.getStringArrayListExtra(IntentPassName.IMAGE_PATH_LIST))
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
                R.id.youtubeThumbnailLayout -> { // 유튜브 썸네일 클릭
                    RunActivity.youtubePlayerActivity(context = this, youTubeVideoId = intent.getStringExtra(IntentPassName.YOUTUBE_VIDEO_ID))
                }
                R.id.hashTagBtn -> { // 해시태그 단축키
                    editTextContent.text?.insert(editTextContent.selectionStart, AppConstants.HASH_TAG_SIGN.toString())
                }
                R.id.userTagBtn -> { // 유저태그 단축키
                    editTextContent.text?.insert(editTextContent.selectionStart, AppConstants.USER_TAG_SIGN.toString())
                }
                R.id.finishBtn -> { // 팝업 내리기
                    currentFocus?.clearFocus()
                    v.requestFocus()
                    ViewFunction.hideKeyboard(this, v)
                }
                R.id.actionBtn -> { // 저장하기
                    currentFocus?.clearFocus()
                    v.requestFocus()
                    ViewFunction.hideKeyboard(this, v)
                    postUsedGoodsViewModel.finishSelectUsedGoods(dataNum = 2)
                }
                R.id.usedGoodsTitleLayout -> { // 사용한 제품 추가 레이아웃
                    ViewFunction.hideKeyboard(this, v)
                    postUsedGoodsViewModel.finishSelectUsedGoods(dataNum = 1)
                }
                else -> {}
            }
        }
    }

    private fun getPostContent(): String = editTextContent.text.toString()

    companion object {
        fun newIntent(context: Context, postId: String, youTubeVideoId: String, imagePath: ArrayList<String>): Intent {
            val intent = Intent(context, PostUploadContentActivity::class.java)
            intent.putExtra(IntentPassName.POST_ID, postId)
            intent.putExtra(IntentPassName.YOUTUBE_VIDEO_ID, youTubeVideoId)
            intent.putExtra(IntentPassName.IMAGE_PATH_LIST, imagePath)

            return intent
        }
    }
}
