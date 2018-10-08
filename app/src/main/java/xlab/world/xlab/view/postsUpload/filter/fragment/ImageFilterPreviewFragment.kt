package xlab.world.xlab.view.postsUpload.filter.fragment

import android.graphics.Bitmap
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_image_filter_preview.*
import org.koin.android.ext.android.inject
import xlab.world.xlab.R
import xlab.world.xlab.utils.support.GPUImageFilterData
import java.io.File

class ImageFilterPreviewFragment : Fragment() {
    private val gpuImageFilterData: GPUImageFilterData by inject()

    private var filterIndex
        get() = arguments?.getInt("filterIndex") ?: 0
        set(value) {
            arguments?.putInt("filterIndex", value)
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_image_filter_preview, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        onSetup()

        onBindEvent()
    }

    private fun onSetup() {
        imageView.setImage(getBundleImagePath())
        imageView.filter = gpuImageFilterData.filterList[filterIndex].filter
    }

    private fun onBindEvent() {
    }

    fun changeFilter(filterIndex: Int) {
        this.filterIndex = filterIndex

        imageView.setImage(getBundleImagePath())
        imageView.filter = gpuImageFilterData.filterList[filterIndex].filter
//        imageView.requestRender()
    }

    fun getBitmap(): Bitmap {
        return imageView.gpuImage.bitmapWithFilterApplied
    }

    private fun getBundleImagePath(): File = arguments?.getSerializable("imageFile") as File

    companion object {
        fun newFragment(imageFile: File): ImageFilterPreviewFragment {
            val fragment = ImageFilterPreviewFragment()

            val args = Bundle()
            args.putSerializable("imageFile", imageFile)
            args.putInt("filterIndex", 0)
            fragment.arguments = args

            return fragment
        }
    }
}
