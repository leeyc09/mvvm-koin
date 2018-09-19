package xlab.world.xlab.view.galleryImageSelect

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.SimpleItemAnimator
import android.view.View
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.github.chrisbanes.photoview.PhotoViewAttacher
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

    private lateinit var galleryAdapter: GalleryAdapter

    private lateinit var defaultToast: DefaultToast
    private lateinit var progressDialog: DefaultProgressDialog

    private val gallerySelectListener = View.OnClickListener { view ->
        val position = view.tag as Int
        galleryImageSelectViewModel.changeImageSelect(position = position,
                newSelectedData = galleryAdapter.getItem(position))
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
        appBarLayout.stateListAnimator = null
        setSupportActionBar(toolbar)

        actionBarTitle.visibility = View.GONE

        imageViewOverlay.visibility =
                if (intent.getBooleanExtra(IntentPassName.WITH_CIRCLE_OVERLAY, true)) View.VISIBLE
                else View.GONE

//        imageViewPreview.scaleType = ImageView.ScaleType.FIT_CENTER
        imageViewPreview.getDisplayMatrix(Matrix())

        // Toast 초기화
        defaultToast = DefaultToast(context = this)
        progressDialog = DefaultProgressDialog(context = this)

        // gallery recycler view & adapter 초기화
        galleryAdapter = GalleryAdapter(context = this,
                selectListener = gallerySelectListener)
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
                uiData.oneSelectData?.let {
                    galleryImageSelectViewModel.setSingleSelectData(selectData = galleryAdapter.getItem(it.position))
                    imageViewPreview.setDisplayMatrix(Matrix())
                    Glide.with(this)
                            .load(it.data)
                            .into(imageViewPreview)
                }
                uiData.galleryData?.let {
                    if (it.nextPage <= 2 ) { // 요청한 page => 첫페이지
                        galleryAdapter.updateData(galleryData = it)
                    }
                    else
                        galleryAdapter.addData(galleryData = it)
                }
                uiData.galleryUpdatePosition?.let {
                    galleryAdapter.notifyItemChanged(it)
                }
                uiData.finalImagePath?.let {
                    val intent = Intent()
                    intent.putExtra(IntentPassName.IMAGE_URL,it)
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                }
            }
        })

        // load gallery image 이벤트 observe
        galleryImageSelectViewModel.loadGalleryImageEvent.observe(owner = this, observer = android.arch.lifecycle.Observer { loadGalleryImageEvent ->
            loadGalleryImageEvent?.let { _ ->
                loadGalleryImageEvent.status?.let { isLoading ->
                    galleryAdapter.dataLoading = isLoading
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
                R.id.actionBtn -> {  // 사진 선택 완료
                    imageViewPreview.buildDrawingCache()
                    val bitmap: Bitmap = imageViewPreview.drawingCache
                    galleryImageSelectViewModel.createImageFileBySelectedImage(bitmap = bitmap)
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
