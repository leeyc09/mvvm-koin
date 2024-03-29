package xlab.world.xlab.view.galleryImageSelect

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.SimpleItemAnimator
import android.view.View
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.action_bar_default.*
import kotlinx.android.synthetic.main.activity_gallery_image_select.*
import org.koin.android.architecture.ext.viewModel
import xlab.world.xlab.R
import xlab.world.xlab.adapter.recyclerView.GalleryAdapter
import xlab.world.xlab.utils.support.*
import xlab.world.xlab.utils.view.dialog.DefaultProgressDialog
import xlab.world.xlab.utils.view.recyclerView.CustomItemDecoration
import xlab.world.xlab.utils.view.toast.DefaultToast

class GalleryImageSelectActivity : AppCompatActivity(), View.OnClickListener {
    private val galleryImageSelectViewModel: GalleryImageSelectViewModel by viewModel()

    private lateinit var defaultToast: DefaultToast
    private lateinit var progressDialog: DefaultProgressDialog

    private lateinit var galleryAdapter: GalleryAdapter

    private val gallerySelectListener = View.OnClickListener { view ->
        galleryImageSelectViewModel.singleSelectImageChange(selectIndex = view.tag as Int)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery_image_select)

        onSetup()

        onBindEvent()

        observeViewModel()
    }

    override fun onBackPressed() {
        actionBackBtn.performClick()
    }

    private fun onSetup() {
        // 타이틀 비활성화
        actionBarTitle.visibility = View.GONE
        // 이미지 프리뷰 overlay 활성 & 비활성
        imageViewOverlay.visibility =
                if (intent.getBooleanExtra(IntentPassName.WITH_CIRCLE_OVERLAY, true)) View.VISIBLE
                else View.GONE

        // appBarLayout 애니메이션 없애기
        appBarLayout.stateListAnimator = null
        // swipe refresh 비활성화
        swipeRefreshLayout.isEnabled = false

        // Toast, Dialog 초기화
        defaultToast = DefaultToast(context = this)
        progressDialog = DefaultProgressDialog(context = this)

        // gallery recycler view & adapter 초기화
        galleryAdapter = GalleryAdapter(context = this,
                selectListener = gallerySelectListener,
                directSelectListener = null)
        recyclerView.adapter = galleryAdapter
        recyclerView.layoutManager = GridLayoutManager(this, 4, GridLayoutManager.VERTICAL, false)
        recyclerView.addItemDecoration(CustomItemDecoration(context = this, offset = 0.5f))
        (recyclerView.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false

        galleryImageSelectViewModel.loadGalleryImage(context = this, page = 1, dataType = AppConstants.GALLERY_ONE)
    }

    private fun onBindEvent() {
        actionBackBtn.setOnClickListener(this) // 뒤로가기
        actionBtn.setOnClickListener(this) // 사진 선택 완료

        ViewFunction.onRecyclerViewScrolledDown(recyclerView = recyclerView) {
            ViewFunction.isScrolledRecyclerView(layoutManager = it as GridLayoutManager, isLoading = galleryAdapter.dataLoading, total = galleryAdapter.dataTotal) { _ ->
                galleryImageSelectViewModel.loadGalleryImage(context = this, page = galleryAdapter.dataNextPage, dataType = AppConstants.GALLERY_ONE)
            }
        }
    }

    private fun observeViewModel() {
        // UI 이벤트 observe
        galleryImageSelectViewModel.uiData.observe(this, android.arch.lifecycle.Observer { uiData ->
            uiData?.let { _ ->
                uiData.isLoading?.let {
                    if (it && !progressDialog.isShowing)
                        progressDialog.show()
                    else if (!it && progressDialog.isShowing)
                        progressDialog.dismiss()
                }
                uiData.toastMessage?.let {
                    defaultToast.showToast(message = it)
                    actionBackBtn.performClick()
                }
                uiData.imagePreviewData?.let {
                    imageViewPreview.setDisplayMatrix(it.matrix)
                    Glide.with(this)
                            .load(it.data)
                            .into(imageViewPreview)
                }
                uiData.galleryData?.let {
                    galleryAdapter.linkData(galleryData = it)
                }
                uiData.galleryDataUpdate?.let {
                    galleryAdapter.notifyDataSetChanged()
                }
                uiData.galleryUpdateIndex?.let {
                    galleryAdapter.notifyItemChanged(it)
                }
                uiData.finalImagePathList?.let {
                    val intent = Intent()
                    intent.putExtra(IntentPassName.IMAGE_URL, it.last())
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                }
            }
        })

        // load gallery image 이벤트 observe
        galleryImageSelectViewModel.loadGalleryData.observe(owner = this, observer = android.arch.lifecycle.Observer { eventData ->
            eventData?.let { isLoading ->
                galleryAdapter.dataLoading = isLoading
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
                R.id.actionBtn -> {  // 사진 선택 완료
                    // 이미지 bitmap 저장
                    imageViewPreview.isDrawingCacheEnabled = true
                    val bitmap: Bitmap = imageViewPreview.drawingCache
                    galleryImageSelectViewModel.updateBitmap(bitmap = bitmap.copy(bitmap.config, true))
                    imageViewPreview.isDrawingCacheEnabled = false

                    galleryImageSelectViewModel.createImageFileBySelectedImageList()
                }
            }
        }
    }

    companion object {
        fun newIntent(context: Context, withOverlay: Boolean): Intent {
            val intent = Intent(context, GalleryImageSelectActivity::class.java)
            intent.putExtra(IntentPassName.WITH_CIRCLE_OVERLAY, withOverlay)

            return intent
        }
    }
}
