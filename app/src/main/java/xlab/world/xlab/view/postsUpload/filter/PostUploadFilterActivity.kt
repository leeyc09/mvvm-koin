package xlab.world.xlab.view.postsUpload.filter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.res.ResourcesCompat
import android.text.Spannable
import android.text.SpannableString
import android.text.TextUtils
import android.text.style.CharacterStyle
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import jp.co.cyberagent.android.gpuimage.GPUImageView
import kotlinx.android.synthetic.main.action_bar_default_next.*
import kotlinx.android.synthetic.main.activity_post_upload_filter.*
import org.koin.android.architecture.ext.viewModel
import org.koin.android.ext.android.inject
import xlab.world.xlab.R
import xlab.world.xlab.adapter.viewPager.ViewStatePagerAdapter
import xlab.world.xlab.utils.font.FontColorSpan
import xlab.world.xlab.utils.span.FontForegroundColorSpan
import xlab.world.xlab.utils.support.IntentPassName
import xlab.world.xlab.utils.support.PrintLog
import xlab.world.xlab.utils.support.RequestCodeData
import xlab.world.xlab.utils.support.ViewFunction
import xlab.world.xlab.utils.view.dialog.DefaultProgressDialog
import xlab.world.xlab.utils.view.textView.TextViewMediumNoto
import xlab.world.xlab.view.postsUpload.filter.fragment.ImageFilterPreviewFragment

class PostUploadFilterActivity : AppCompatActivity(), View.OnClickListener {
    private val imageFilterViewModel: ImageFilterViewModel by viewModel()
    private val fontColorSpan: FontColorSpan by inject()

    private lateinit var viewPagerAdapter: ViewStatePagerAdapter

    private lateinit var progressDialog: DefaultProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_upload_filter)

        onSetup()

        onBindEvent()

        observeViewModel()
    }

    override fun onDestroy() {
        super.onDestroy()
        imageFilterViewModel.deleteProfileImage()
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
                    RequestCodeData.POST_UPLOAD -> { // upload finish
                        setResult(Activity.RESULT_OK)
                        finish()
                    }
                }
            }
        }
    }

    private fun onSetup() {
        actionBarTitle.visibility = View.GONE
        actionBtn.isEnabled = true

        progressDialog = DefaultProgressDialog(context = this)

        viewPagerAdapter = ViewStatePagerAdapter(supportFragmentManager)

        imageFilterViewModel.initImageFilterData(imagePaths = intent.getStringArrayListExtra(IntentPassName.IMAGE_PATH_LIST))
    }

    private fun onBindEvent() {
        actionBackBtn.setOnClickListener(this) // 뒤로가기
        actionBtn.setOnClickListener(this) // 다음버튼

        ViewFunction.onViewPagerChangePosition(viewPager = imageViewPreviewPager) { position ->
            imageFilterViewModel.changeSampleImageFilter(position = position)
        }
    }

    private fun observeViewModel() {
        // UI 이벤트 observe
        imageFilterViewModel.uiData.observe(this, android.arch.lifecycle.Observer { uiData ->
            uiData?.let { _ ->
                uiData.isLoading?.let {
                    if (it && !progressDialog.isShowing)
                        progressDialog.show()
                    else if (!it && progressDialog.isShowing)
                        progressDialog.dismiss()
                }
                uiData.oneImagePreViewVisible?.let {
                    imagePreview.visibility = it
                }
                uiData.manyImagePreViewVisible?.let {
                    imageViewPreviewPager.visibility = it
                    tabLayoutDot.visibility = it
                }
                uiData.imageFileList?.let {
                    it.forEach { imageFile ->
                        val fragment = ImageFilterPreviewFragment.newFragment(imageFile = imageFile)
                        viewPagerAdapter.addFragment(fragment = fragment, title = "")
                    }
                    imageViewPreviewPager.pageMargin = 20
                    imageViewPreviewPager.adapter = viewPagerAdapter

                    tabLayoutDot.setupWithViewPager(imageViewPreviewPager, true)
                    (0 until tabLayoutDot.tabCount).forEach { index ->
                        val tab: View = (tabLayoutDot.getChildAt(0) as ViewGroup).getChildAt(index)
                        val params = tab.layoutParams as ViewGroup.MarginLayoutParams
                        val marginDp = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3f, resources.displayMetrics).toInt()
                        params.setMargins(marginDp, 0, marginDp, 0)
                        tab.requestLayout()
                    }
                }
                uiData.imageFile?.let {
                    imagePreview.setImage(it)
                }
                uiData.filterPreviewData?.let {
                    if (filterList.childCount > 1)
                        changeSampleImageFilter(it)
                    else
                        createFilterPreviewScrollView(it)
                }
                uiData.finalImagePathList?.let {
                    PrintLog.d("finalImagePathList", it.toString(), imageFilterViewModel.tag)
                }
            }
        })

        // image preview 이벤트 observe
        imageFilterViewModel.imageFilterEvent.observe(owner = this, observer = android.arch.lifecycle.Observer { eventData ->
            eventData?.let { _ ->
                eventData.singleFilter?.let {
                    imagePreview.setImage(it.imageFile)
                    imagePreview.filter = it.filterData.filter

                    imageFilterViewModel.updateBitmap(bitmap = imagePreview.gpuImage.bitmapWithFilterApplied, index = imageViewPreviewPager.currentItem)
                }
                eventData.filterIndex?.let {
                    if (viewPagerAdapter.count > 1) {
                        val fragment = viewPagerAdapter.getItem(imageViewPreviewPager.currentItem) as ImageFilterPreviewFragment
                        fragment.changeFilter(filterIndex = it)

                        imageFilterViewModel.updateBitmap(bitmap = fragment.getBitmap(), index = imageViewPreviewPager.currentItem)
                    }

                    (0 until filterList.childCount).forEach { index ->
                        val imageFilter = filterList.getChildAt(index) as LinearLayout
                        val overlayImageView = (imageFilter.getChildAt(0) as FrameLayout).getChildAt(1) as ImageView
                        val textView = imageFilter.getChildAt(1) as TextView

                        if (index == it) {
                            overlayImageView.visibility = View.VISIBLE
                            changeFont(textView, fontColorSpan.notoBold000000)
                        } else {
                            overlayImageView.visibility = View.GONE
                            changeFont(textView, fontColorSpan.notoMediumBFBFBF)
                        }
                    }
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
                R.id.actionBtn -> { // 다음버튼
                    imageFilterViewModel.createImageFileFilteredImage()
                }
            }
        }
    }

    private fun createFilterPreviewScrollView(filterPreviewData: ArrayList<FilterPreviewData>) {
        val metrics = resources.displayMetrics
        // set filter select list view
        filterPreviewData.forEachIndexed { index, previewData ->
            // 레이아웃 틀 잡
            val linearLayout = LinearLayout(this)
            val imageWidthDIP = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 72f, metrics).toInt()
            val linearLayoutOffSetDIP = when(index) {
                0 -> TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20f, metrics).toInt()
                1 -> TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24f, metrics).toInt()
                else -> TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4f, metrics).toInt()
            }
            linearLayout.layoutParams = LinearLayout.LayoutParams(imageWidthDIP, LinearLayout.LayoutParams.WRAP_CONTENT)
            (linearLayout.layoutParams as LinearLayout.LayoutParams).setMargins(linearLayoutOffSetDIP, 0, 0, 0)
            linearLayout.orientation = LinearLayout.VERTICAL

            // image 틀
            val frameLayout = FrameLayout(this)
            frameLayout.layoutParams = FrameLayout.LayoutParams(imageWidthDIP, imageWidthDIP)

            // image
            val gpuImageView = GPUImageView(this)
            gpuImageView.layoutParams = FrameLayout.LayoutParams(imageWidthDIP, imageWidthDIP)
            gpuImageView.setImage(previewData.imageFile)
            gpuImageView.filter = previewData.filterData.filter

            // image overlay
            val overlayImageView = ImageView(this)
            overlayImageView.layoutParams = FrameLayout.LayoutParams(imageWidthDIP, imageWidthDIP)
            overlayImageView.background = ColorDrawable(ResourcesCompat.getColor(resources, R.color.colorWhite60, null))
            overlayImageView.visibility = if (previewData.isSelect) View.VISIBLE else View.GONE

            frameLayout.addView(gpuImageView)
            frameLayout.addView(overlayImageView)

            // filter name
            val textView = TextViewMediumNoto(context = this)
            val textViewMarginDIP = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4f, metrics).toInt()
            textView.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            (textView.layoutParams as LinearLayout.LayoutParams).setMargins(0, textViewMarginDIP, 0, 0)
            textView.textSize = 13f
            textView.setSingleLine(true)
            textView.ellipsize = TextUtils.TruncateAt.END
            textView.setText(previewData.filterData.name, TextView.BufferType.SPANNABLE)
            changeFont(textView = textView, font = if (previewData.isSelect) fontColorSpan.notoBold000000 else fontColorSpan.notoMediumBFBFBF)

            linearLayout.addView(frameLayout)
            linearLayout.addView(textView)
            // 필터 샘플 터치 이벤트
            linearLayout.filterClickAction(index = index)

            filterList.addView(linearLayout)
        }
    }

    private fun changeSampleImageFilter(filterPreviewData: ArrayList<FilterPreviewData>) {
        filterPreviewData.forEachIndexed { index, previewData ->
            val imageFilter = filterList.getChildAt(index) as LinearLayout

            val gpuImageView = (imageFilter.getChildAt(0) as FrameLayout).getChildAt(0) as GPUImageView
            val overlayImageView = (imageFilter.getChildAt(0) as FrameLayout).getChildAt(1) as ImageView
            val textView = imageFilter.getChildAt(1) as TextView

            gpuImageView.setImage(previewData.imageFile)
            gpuImageView.filter = previewData.filterData.filter

            if (previewData.isSelect) {
                overlayImageView.visibility = View.VISIBLE
                changeFont(textView, fontColorSpan.notoBold000000)
            } else {
                overlayImageView.visibility = View.GONE
                changeFont(textView, fontColorSpan.notoMediumBFBFBF)
            }
        }
    }

    private fun changeFont(textView: TextView, font: FontForegroundColorSpan) {
        val spannable = SpannableString(textView.text)

        val spannableSpans = spannable.getSpans(0, textView.text.length, CharacterStyle::class.java)
        spannableSpans.forEach { span ->
            spannable.removeSpan(span)
        }
        spannable.setSpan(font, 0, textView.text.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        textView.setText(spannable, TextView.BufferType.SPANNABLE)
    }

    private fun LinearLayout.filterClickAction(index: Int) {
        this.setOnClickListener {
            val pagerIndex = imageViewPreviewPager.currentItem
            imageFilterViewModel.changePreviewFilter(pagerIndex = pagerIndex, filterIndex = index)
        }
    }

    companion object {
        fun newIntent(context: Context, imagePath: ArrayList<String>):Intent {
            val intent = Intent(context, PostUploadFilterActivity::class.java)
            intent.putExtra(IntentPassName.IMAGE_PATH_LIST, imagePath)

            return intent
        }
    }
}
