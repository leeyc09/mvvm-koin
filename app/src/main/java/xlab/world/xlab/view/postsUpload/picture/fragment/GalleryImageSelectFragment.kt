package xlab.world.xlab.view.postsUpload.picture.fragment

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Bundle
import android.os.CountDownTimer
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.SimpleItemAnimator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.action_bar_post_upload.*
import kotlinx.android.synthetic.main.fragment_gallery_image_select.*
import org.koin.android.architecture.ext.viewModel
import org.koin.android.ext.android.inject
import xlab.world.xlab.R
import xlab.world.xlab.adapter.recyclerView.GalleryAdapter
import xlab.world.xlab.utils.support.*
import xlab.world.xlab.utils.view.dialog.DefaultProgressDialog
import xlab.world.xlab.utils.view.recyclerView.CustomItemDecoration
import xlab.world.xlab.utils.view.toast.DefaultToast
import xlab.world.xlab.view.galleryImageSelect.GalleryImageSelectViewModel
import xlab.world.xlab.view.postsUpload.picture.PostUploadPictureActivity

class GalleryImageSelectFragment: Fragment(), View.OnClickListener {
    private val galleryImageSelectViewModel: GalleryImageSelectViewModel by viewModel()
    private val spHelper: SPHelper by inject()

    private var needInitData
        get() = arguments?.getBoolean("needInitData") ?: true
        set(value) {
            arguments?.putBoolean("needInitData", value)
        }

    private lateinit var activity: PostUploadPictureActivity

    private var defaultToast: DefaultToast? = null
    private var progressDialog: DefaultProgressDialog? = null

    private var galleryAdapter: GalleryAdapter? = null

    // 사진 이미지 선택 리스너
    private lateinit var gallerySelectListener: View.OnClickListener
    // 사진 바로 선택 리스너 (이미지 위 동그란원)
    private var directGallerySelectListener: View.OnClickListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_gallery_image_select, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        onSetup()

        onBindEvent()

        observeViewModel()
    }

    private fun onSetup() {
        // 라이브러리 전환 버튼 비활성화, 카메라 전환 버튼 활성화
        libraryBtn.isEnabled = false
        cameraBtn.isEnabled = true

        // appBarLayout 애니메이션 없애기
        appBarLayout.stateListAnimator = null
        // swipe refresh 비활성화
        swipeRefreshLayout.isEnabled = false

        activity = context as PostUploadPictureActivity

        // Toast, Dialog 초기화
        defaultToast = defaultToast ?: DefaultToast(context = context!!)
        progressDialog = progressDialog ?: DefaultProgressDialog(context = context!!)

        // listener 초기화
        val youtubeId = activity.intent.getStringExtra(IntentPassName.YOUTUBE_VIDEO_ID)
        gallerySelectListener =
                if (youtubeId.isEmpty()) View.OnClickListener { view ->
                    savePreviewMatrix()
                    savePreviewBitmap()

                    galleryImageSelectViewModel.multiSelectImageChange(selectIndex = view.tag as Int)
                } else View.OnClickListener { view ->
                    galleryImageSelectViewModel.singleSelectImageChange(selectIndex = view.tag as Int)
                }
        directGallerySelectListener =
                if (youtubeId.isEmpty()) View.OnClickListener { view ->
                    savePreviewMatrix()
                    savePreviewBitmap()

                    galleryImageSelectViewModel.directMultiSelectImageChange(selectIndex = view.tag as Int)
                } else null


        // gallery recycler view & adapter 초기화
        galleryAdapter = galleryAdapter ?: GalleryAdapter(context = context!!,
                selectListener = gallerySelectListener,
                directSelectListener = directGallerySelectListener)
        recyclerView.adapter = galleryAdapter
        recyclerView.layoutManager = GridLayoutManager(context, 4, GridLayoutManager.VERTICAL, false)
        if (recyclerView.itemDecorationCount < 1)
            recyclerView.addItemDecoration(CustomItemDecoration(context = context!!, offset = 0.5f))
        (recyclerView.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false

        galleryImageSelectViewModel.initMaxSelectCount(userLevel = spHelper.userLevel)
        if (needInitData) {
            galleryImageSelectViewModel.loadGalleryImage(context = context!!, page = 1,
                    dataType = if (youtubeId.isEmpty()) AppConstants.GALLERY_MANY else AppConstants.GALLERY_ONE)
        }
    }

    private fun onBindEvent() {
        actionCloseBtn.setOnClickListener(this) // 닫기 버튼
        cameraBtn.setOnClickListener(this) // 카메라 버튼
        actionNextBtn.setOnClickListener(this) // 다음버튼

        ViewFunction.onRecyclerViewScrolledDown(recyclerView = recyclerView) {
            ViewFunction.isScrolledRecyclerView(layoutManager = it as GridLayoutManager, isLoading = galleryAdapter!!.dataLoading, total = galleryAdapter!!.dataTotal) { _->
                val youtubeId = activity.intent.getStringExtra(IntentPassName.YOUTUBE_VIDEO_ID)
                galleryImageSelectViewModel.loadGalleryImage(context = context!!, page = galleryAdapter!!.dataNextPage,
                        dataType = if (youtubeId.isEmpty()) AppConstants.GALLERY_MANY else AppConstants.GALLERY_ONE)
            }
        }
    }

    private fun observeViewModel() {
        // UI 이벤트 observe
        galleryImageSelectViewModel.uiData.observe(this, android.arch.lifecycle.Observer { uiData ->
            uiData?.let { _ ->
                uiData.isLoading?.let {
                    if (it && !progressDialog!!.isShowing)
                        progressDialog!!.show()
                    else if (!it && progressDialog!!.isShowing)
                        progressDialog!!.dismiss()
                }
                uiData.toastMessage?.let {
                    defaultToast?.showToast(message = it)
                    actionNextBtn.isEnabled = false
                }
                uiData.imagePreviewData?.let {
                    imageViewPreview.visibility = View.INVISIBLE
                    Glide.with(this)
                            .load(it.data)
                            .into(imageViewPreview)
                    object: CountDownTimer(50, 50) {
                        override fun onTick(millisUntilFinished: Long) {
                        }

                        override fun onFinish() {
                            imageViewPreview.visibility = View.VISIBLE
                            imageViewPreview.setDisplayMatrix(it.matrix)
                        }
                    }.start()
                }
                uiData.galleryData?.let {
                    galleryAdapter?.linkData(galleryData = it)
                }
                uiData.galleryUpdateIndex?.let {
                    galleryAdapter?.notifyItemChanged(it)
                }
                uiData.finalImagePathList?.let {
                    RunActivity.postUploadFilterActivity(context = activity,
                            postId = activity.intent.getStringExtra(IntentPassName.POST_ID),
                            youTubeVideoId = activity.intent.getStringExtra(IntentPassName.YOUTUBE_VIDEO_ID),
                            imagePathList = it)
                }
            }
        })

        // load gallery image 이벤트 observe
        galleryImageSelectViewModel.loadGalleryData.observe(owner = this, observer = android.arch.lifecycle.Observer { eventData ->
            eventData?.let { isLoading ->
                galleryAdapter?.dataLoading = isLoading
                needInitData = false
            }
        })
    }

    override fun onClick(v: View?) {
        v?.let {
            when (v.id) {
                R.id.actionCloseBtn -> { // 닫기버튼
                    (context as Activity).setResult(Activity.RESULT_CANCELED)
                    (context as Activity).finish()
                }
                R.id.cameraBtn -> { // 카메라버튼
                    activity.switchFragment(fragment = this)
                }
                R.id.actionNextBtn -> { // 다음버튼
                    savePreviewBitmap()
                    galleryImageSelectViewModel.createImageFileBySelectedImageList()
                }
            }
        }
    }

    private fun savePreviewMatrix() {
        // 이미지 matrix 저장
        val matrix = Matrix()
        imageViewPreview.getDisplayMatrix(matrix)
        galleryImageSelectViewModel.updateMatrix(matrix = matrix)
    }

    private fun savePreviewBitmap() {
        // 이미지 bitmap 저장
        imageViewPreview.isDrawingCacheEnabled = true
        val bitmap: Bitmap = imageViewPreview.drawingCache
        galleryImageSelectViewModel.updateBitmap(bitmap = bitmap.copy(bitmap.config, true))
        imageViewPreview.isDrawingCacheEnabled = false
    }

    companion object {
        fun newFragment(): GalleryImageSelectFragment {
            val fragment = GalleryImageSelectFragment()

            val args = Bundle()
            args.putBoolean("needInitData", true)
            fragment.arguments = args

            return fragment
        }
    }
}