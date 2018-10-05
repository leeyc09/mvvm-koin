package xlab.world.xlab.view.postsUpload.fragment

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
import xlab.world.xlab.utils.support.AppConstants
import xlab.world.xlab.utils.support.PrintLog
import xlab.world.xlab.utils.support.SPHelper
import xlab.world.xlab.utils.support.ViewFunction
import xlab.world.xlab.utils.view.dialog.DefaultProgressDialog
import xlab.world.xlab.utils.view.recyclerView.CustomItemDecoration
import xlab.world.xlab.utils.view.toast.DefaultToast
import xlab.world.xlab.view.galleryImageSelect.GalleryImageSelectViewModel

class GalleryImageSelectFragment: Fragment(), View.OnClickListener {
    private val galleryImageSelectViewModel: GalleryImageSelectViewModel by viewModel()
    private val spHelper: SPHelper by inject()

    private var galleryAdapter: GalleryAdapter? = null

    private var defaultToast: DefaultToast? = null
    private var progressDialog: DefaultProgressDialog? = null

    private val gallerySelectListener = View.OnClickListener { view ->
        val matrix = Matrix()
        imageViewPreview.getDisplayMatrix(matrix)
        galleryImageSelectViewModel.updateMatrix(matrix = matrix)
        galleryImageSelectViewModel.multiSelectImageChange(position = view.tag as Int,
                selectData = galleryAdapter!!.getItem(position = view.tag as Int))
    }
    private val directGallerySelectListener = View.OnClickListener { view ->
        val matrix = Matrix()
        imageViewPreview.getDisplayMatrix(matrix)
        galleryImageSelectViewModel.updateMatrix(matrix = matrix)
        galleryImageSelectViewModel.directMultiSelectImageChange(position = view.tag as Int,
                selectData = galleryAdapter!!.getItem(position = view.tag as Int))
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_gallery_image_select, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        onSetup()

        onBindEvent()

        observeViewModel()
    }

    private fun onSetup() {
        libraryBtn.isEnabled = false
        cameraBtn.isEnabled = true

        appBarLayout.stateListAnimator = null

        swipeRefreshLayout.isEnabled = false

        // Toast 초기화
        defaultToast = defaultToast ?: DefaultToast(context = context!!)
        progressDialog = progressDialog ?: DefaultProgressDialog(context = context!!)

        // gallery recycler view & adapter 초기화
        galleryAdapter = GalleryAdapter(context = context!!,
                selectListener = gallerySelectListener,
                directSelectListener = directGallerySelectListener)
        recyclerView.adapter = galleryAdapter
        recyclerView.layoutManager = GridLayoutManager(context, 4, GridLayoutManager.VERTICAL, false)
        recyclerView.addItemDecoration(CustomItemDecoration(context = context!!, offset = 0.5f))
        (recyclerView.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false

        galleryImageSelectViewModel.initMaxSelectCount(userLevel = spHelper.userLevel)
        galleryImageSelectViewModel.loadGalleryImage(context = context!!, page = 1, dataType = AppConstants.GALLERY_MANY)
    }

    private fun onBindEvent() {
        actionCloseBtn.setOnClickListener(this) // 닫기 버튼
        actionNextBtn.setOnClickListener(this) // 다음버튼

        ViewFunction.onRecyclerViewScrolledDown(recyclerView = recyclerView) {
            ViewFunction.isScrolledRecyclerView(layoutManager = it as GridLayoutManager, isLoading = galleryAdapter!!.dataLoading, total = galleryAdapter!!.dataTotal) { _->
                galleryImageSelectViewModel.loadGalleryImage(context = context!!, page = galleryAdapter!!.dataNextPage, dataType = AppConstants.GALLERY_MANY)
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

                            imageViewPreview.buildDrawingCache()
                            val bitmap: Bitmap = imageViewPreview.drawingCache
                            galleryImageSelectViewModel.updateBitmap(bitmap = bitmap)
                        }
                    }.start()
                }
                uiData.galleryData?.let {
                    if (it.nextPage <= 2 ) { // 요청한 page => 첫페이지
                        galleryAdapter?.updateData(galleryData = it)
                    }
                    else
                        galleryAdapter?.addData(galleryData = it)
                }
                uiData.galleryUpdatePosition?.let {
                    galleryAdapter?.notifyItemChanged(it)
                }
                uiData.finalImagePathList?.let {
                    PrintLog.d("finalImagePathList", it.toString(), galleryImageSelectViewModel.tag)
                }
            }
        })

        // load gallery image 이벤트 observe
        galleryImageSelectViewModel.loadGalleryImageEvent.observe(owner = this, observer = android.arch.lifecycle.Observer { loadGalleryImageEvent ->
            loadGalleryImageEvent?.let { _ ->
                loadGalleryImageEvent.status?.let { isLoading ->
                    galleryAdapter?.dataLoading = isLoading
                }
            }
        })

        // image preview 이벤트 observe
        galleryImageSelectViewModel.imagePreviewEvent.observe(owner = this, observer = android.arch.lifecycle.Observer { eventData ->
            eventData?.let { _ ->
                eventData.updateIndex?.let {
                    galleryImageSelectViewModel.updateSelectDataList(index = it, selectData = galleryAdapter!!.getItem(it))
                }
                eventData.addIndex?.let {
                    galleryImageSelectViewModel.addSelectDataList(index = it, selectData = galleryAdapter!!.getItem(it))
                }
                eventData.removeIndex?.let {
                    galleryImageSelectViewModel.removeSelectDataList(index = it)
                }
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
                }
                R.id.actionNextBtn -> { // 다음버튼
                    imageViewPreview.buildDrawingCache()
                    val bitmap: Bitmap = imageViewPreview.drawingCache
                    galleryImageSelectViewModel.createImageFileBySelectedImageList(lastBitmap = bitmap)
                }
            }
        }
    }

    companion object {
        fun newFragment(): GalleryImageSelectFragment {
            return GalleryImageSelectFragment()
        }
    }
}