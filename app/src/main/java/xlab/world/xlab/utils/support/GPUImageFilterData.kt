package xlab.world.xlab.utils.support

import android.graphics.PointF
import jp.co.cyberagent.android.gpuimage.*
import jp.co.cyberagent.android.gpuimage.GPUImageFilter

class GPUImageFilterData {

    enum class FilterType {
        ORIGINAL, HAZE, GAMMA, CONTRAST, EXPOSURE, LEVELS_MIN, SEPIA, GRAY_SCALE, VIGNETTE
    }

    val filterList = ArrayList<FilterData>()

    init {
        addFilter("Original",
                createFilterForType(FilterType.ORIGINAL))
        addFilter("01",
                createFilterForType(FilterType.HAZE))
        addFilter("02",
                createFilterForType(FilterType.GAMMA))
        addFilter("03",
                createFilterForType(FilterType.CONTRAST))
        addFilter("04",
                createFilterForType(FilterType.EXPOSURE))
        addFilter("05",
                createFilterForType(FilterType.LEVELS_MIN))
        addFilter("06",
                createFilterForType(FilterType.SEPIA))
        addFilter("07",
                createFilterForType(FilterType.GRAY_SCALE))
        addFilter("08",
                createFilterForType(FilterType.VIGNETTE))
    }

    private fun addFilter(name: String, filter: GPUImageFilter) {
        filterList.add(FilterData(name = name, filter = filter))
    }

    private fun createFilterForType(type: FilterType): GPUImageFilter {
        return when (type) {
            FilterType.ORIGINAL -> GPUImageFilter()
            FilterType.HAZE -> GPUImageHazeFilter()
            FilterType.GAMMA -> GPUImageGammaFilter(2.0f)
            FilterType.CONTRAST -> GPUImageContrastFilter(2.0f)
            FilterType.EXPOSURE -> GPUImageExposureFilter(0.0f)
            FilterType.LEVELS_MIN -> levelsFilter()
            FilterType.SEPIA -> GPUImageSepiaFilter()
            FilterType.GRAY_SCALE -> GPUImageGrayscaleFilter()
            FilterType.VIGNETTE -> vignetteFilter()
        }
    }

    private fun levelsFilter(): GPUImageLevelsFilter {
        val levelsFilter = GPUImageLevelsFilter()
        levelsFilter.setMin(0.0f, 3.0f, 1.0f)
        return levelsFilter
    }

    private fun vignetteFilter(): GPUImageVignetteFilter {
        val centerPoint = PointF()
        centerPoint.x = 0.5f
        centerPoint.y = 0.5f
        return GPUImageVignetteFilter(centerPoint, floatArrayOf(0f, 0f, 0f), 0.3f, 0.75f);
    }

    inner class FilterData(val name: String,
                           val filter: GPUImageFilter)
}