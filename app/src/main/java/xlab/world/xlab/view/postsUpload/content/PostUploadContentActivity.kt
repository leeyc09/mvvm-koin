package xlab.world.xlab.view.postsUpload.content

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.bumptech.glide.Glide
import com.google.android.youtube.player.YouTubeStandalonePlayer
import kotlinx.android.synthetic.main.action_bar_default.*
import kotlinx.android.synthetic.main.activity_post_upload_content.*
import org.koin.android.architecture.ext.viewModel
import org.koin.android.ext.android.inject
import xlab.world.xlab.R
import xlab.world.xlab.adapter.recyclerView.PostThumbnailAdapter
import xlab.world.xlab.adapter.recyclerView.PostUploadPictureAdapter
import xlab.world.xlab.adapter.recyclerView.RecentHashTagAdapter
import xlab.world.xlab.adapter.recyclerView.SearchHashTagAdapter
import xlab.world.xlab.utils.font.CustomFont
import xlab.world.xlab.utils.listener.DefaultListener
import xlab.world.xlab.utils.support.*
import xlab.world.xlab.utils.view.dialog.DefaultProgressDialog
import xlab.world.xlab.utils.view.hashTag.HashTagHelper
import xlab.world.xlab.utils.view.recyclerView.CustomItemDecoration

class PostUploadContentActivity : AppCompatActivity(), View.OnClickListener {
    private val postContentViewModel: PostContentViewModel by viewModel()
    private val spHelper: SPHelper by inject()

    private lateinit var progressDialog: DefaultProgressDialog

    private lateinit var hashTagHelper: HashTagHelper

    private lateinit var postUploadPictureAdapter: PostUploadPictureAdapter
    private lateinit var recentHashTagAdapter: RecentHashTagAdapter
    private lateinit var searchHashTagAdapter: SearchHashTagAdapter

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
                    RequestCodeData.POST_UPLOAD -> {
//                        val selectedData = data!!.getParcelableArrayListExtra<UsedItemSelectListData>(PostUploadUsedItemActivity.selectedItemStr)
//                        val newData = UsedItemSelectData()
//                        newData.items.addAll(selectedData)
//                        usedItemSelectAdapter.updateData(newData)
//                        textViewUsedItemNum.text = usedItemSelectAdapter.itemCount.toString()
                    } // used item update
                }
            }
        }
    }

    private fun onSetup() {
        actionBarTitle.visibility = View.GONE
        actionBtn.isEnabled = true

        popupLayout.visibility = View.GONE
        normalPopupLayout.visibility = View.VISIBLE
        hashTagPopupLayout.visibility = View.GONE

        progressDialog = DefaultProgressDialog(context = this)

        // hash tag helper setting
        val hashTagCharsColor = hashMapOf(AppConstants.HASH_TAG_SIGN to ResourcesCompat.getColor(resources, R.color.color000000, null))
        val hashTagCharsFont = hashMapOf(AppConstants.HASH_TAG_SIGN to CustomFont.getTypeface(CustomFont.notoSansCJKkrBold, this)!!)
        val additionalHashTagChar = ArrayList<Char>()
        hashTagHelper = HashTagHelper(
                hashTagCharsColor = hashTagCharsColor,
                hashTagCharsFont = hashTagCharsFont,
                onHashTagWritingListener = hashTagWritingListener,
                onHashTagClickListener = null,
                additionalHashTagChar = additionalHashTagChar)
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

        postContentViewModel.initViewModelData(imagePaths = intent.getStringArrayListExtra(IntentPassName.IMAGE_PATH_LIST),
                youTubeVideoId = intent.getStringExtra(IntentPassName.YOUTUBE_VIDEO_ID))
        postContentViewModel.loadRecentHashTagData(authorization = spHelper.authorization)

    }

    private fun onBindEvent() {
        actionBackBtn.setOnClickListener(this) // 뒤로가기
        actionBtn.setOnClickListener(this) // 저장하기
        youtubeThumbnailLayout.setOnClickListener(this) // 유튜브 썸네일 클릭
        hashTagBtn.setOnClickListener(this) // 해시태그 단축키
        userTagBtn.setOnClickListener(this) // 유저태그 단축키
        finishBtn.setOnClickListener(this) // 팝업 내리기

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
        // UI 이벤트 observe
        postContentViewModel.uiData.observe(this, android.arch.lifecycle.Observer { uiData ->
            uiData?.let { _ ->
                uiData.isLoading?.let {
                    if (it && !progressDialog.isShowing)
                        progressDialog.show()
                    else if (!it && progressDialog.isShowing)
                        progressDialog.dismiss()
                }
                uiData.youtubeThumbnailVisible?.let {
                    youtubeThumbnailLayout.visibility = it
                    if (it == View.VISIBLE) {
                        val youTubeThumbnail = SupportData.getYoutubeThumbnailUrl(videoId = "1JMwgtMxReE",
                                quality = SupportData.YOUTUBE_THUMB_480x360)
                        Glide.with(this)
                                .load(youTubeThumbnail)
                                .into(youtubeThumbnailView)
                    }
                }
                uiData.postUploadPictureData?.let {
                    postUploadPictureAdapter.updateData(postUploadPictureData = it)
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

        // search hash tag 이벤트 observe
        postContentViewModel.searchHashTagEvent.observe(owner = this, observer = android.arch.lifecycle.Observer { loadPostsEventData ->
            loadPostsEventData?.let { _->
                loadPostsEventData.status?.let { isLoading ->
                    searchHashTagAdapter.dataLoading = isLoading
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
                    val intent = YouTubeStandalonePlayer.createVideoIntent(this, getString(R.string.app_api_key), intent.getStringExtra(IntentPassName.YOUTUBE_VIDEO_ID))
                    startActivity(intent)
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

                    val hashTags = hashTagHelper.getAllHashTags()
                    PrintLog.d("hashTags", hashTags.toString(), postContentViewModel.tag)
                }
//                R.id.usedItemTitleLayout -> { // 사용한 제품 추가 레이아웃
//                    currentFocus?.clearFocus()
//                    v.requestFocus()
//                    ViewFunction.hideKeyboard(this, v)
//
//                    val intent = PostUploadUsedItemActivity.newIntent(this, usedItemSelectData.items)
//                    startActivityForResult(intent, RequestCodeData.POST_UPLOAD)
//                }
                else -> {}
            }
        }
    }

    companion object {
        fun newIntent(context: Context, imagePath: ArrayList<String>, youTubeVideoId: String): Intent {
            val intent = Intent(context, PostUploadContentActivity::class.java)
            intent.putExtra(IntentPassName.IMAGE_PATH_LIST, imagePath)
            intent.putExtra(IntentPassName.YOUTUBE_VIDEO_ID, youTubeVideoId)

            return intent
        }
    }
}
