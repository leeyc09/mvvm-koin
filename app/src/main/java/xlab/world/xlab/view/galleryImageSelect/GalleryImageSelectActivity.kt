package xlab.world.xlab.view.galleryImageSelect

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.SimpleItemAnimator
import android.view.View
import kotlinx.android.synthetic.main.action_bar_default.*
import kotlinx.android.synthetic.main.activity_gallery_image_select.*
import org.koin.android.architecture.ext.viewModel
import xlab.world.xlab.R
import xlab.world.xlab.adapter.recyclerView.GalleryAdapter
import xlab.world.xlab.utils.support.AppConstants
import xlab.world.xlab.utils.support.IntentPassName
import xlab.world.xlab.utils.support.ViewFunction
import xlab.world.xlab.utils.view.recyclerView.CustomItemDecoration

class GalleryImageSelectActivity : AppCompatActivity(), View.OnClickListener {
    private val galleryImageSelectViewModel: GalleryImageSelectViewModel by viewModel()

    private lateinit var galleryAdapter: GalleryAdapter

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

        // gallery recycler view & adapter 초기화
        galleryAdapter = GalleryAdapter(context = this,
                selectListener = View.OnClickListener {  })
        recyclerView.adapter = galleryAdapter
        recyclerView.layoutManager = GridLayoutManager(this, 4, GridLayoutManager.VERTICAL, false)
        recyclerView.addItemDecoration(CustomItemDecoration(context = this, offset = 0.5f))
        (recyclerView.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false

        galleryImageSelectViewModel.loadGalleryImage(context = this, page = 1, dataType = AppConstants.GALLERY_ONE)
    }

    private fun onBindEvent() {
        actionBackBtn.setOnClickListener(this) // 뒤로가기

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
                uiData.galleryData?.let {
                    if (it.nextPage <= 2 ) // 요청한 page => 첫페이지
                        galleryAdapter.updateData(galleryData = it)
                    else
                        galleryAdapter.addData(galleryData = it)
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
