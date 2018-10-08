package xlab.world.xlab.view.postsUpload.filter

import android.arch.lifecycle.MutableLiveData
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.View
import android.widget.LinearLayout
import io.reactivex.Observable
import jp.co.cyberagent.android.gpuimage.GPUImageFilter
import xlab.world.xlab.utils.rx.SchedulerProvider
import xlab.world.xlab.utils.rx.with
import xlab.world.xlab.utils.support.AppConstants
import xlab.world.xlab.utils.support.GPUImageFilterData
import xlab.world.xlab.utils.support.PrintLog
import xlab.world.xlab.utils.support.SupportData
import xlab.world.xlab.view.AbstractViewModel
import xlab.world.xlab.view.SingleLiveEvent
import java.io.File

class ImageFilterViewModel(private val gpuImageFilterData: GPUImageFilterData,
                           private val scheduler: SchedulerProvider): AbstractViewModel() {
    val tag = "ImageFilter"

    private var bitmapList: ArrayList<Bitmap> = ArrayList()
    private var imagePathList: ArrayList<String> = ArrayList()
    private var imageFile: ArrayList<File> = ArrayList()
    private var selectFilterList: ArrayList<Int> = ArrayList()

    val imageFilterEvent = SingleLiveEvent<ChangeFilterData>()
    val uiData = MutableLiveData<UIModel>()

    fun updateBitmap(bitmap: Bitmap, index: Int) {
        bitmapList[index] = bitmap.copy(bitmap.config, true)
    }

    fun initImageFilterData(imagePaths: ArrayList<String>) {
        uiData.value = UIModel(isLoading = true)
        launch {
            Observable.create<Int> {
                this.imagePathList.clear()
                this.imagePathList.addAll(imagePaths)

                this.imagePathList.forEach { path ->
                    imageFile.add(File(path))
                    val bitmap = BitmapFactory.decodeFile(path)
                    bitmapList.add(bitmap)
                    selectFilterList.add(0)
                }
                PrintLog.d("imagePaths", this.imagePathList.toString(), tag)

                it.onNext(this.imagePathList.size)
                it.onComplete()
            }.with(scheduler).subscribe { imagePathSize ->
                if (imagePathSize > 1) { // 여러 이미지
                    uiData.value = UIModel(isLoading = false, oneImagePreViewVisible = View.GONE,
                            manyImagePreViewVisible = View.VISIBLE, imageFileList = imageFile)
                } else { // 단일 이미지
                    uiData.value = UIModel(isLoading = false, oneImagePreViewVisible = View.VISIBLE,
                            manyImagePreViewVisible = View.GONE, imageFile = imageFile.first())
                }

                val filterPreviewData = ArrayList<FilterPreviewData>()
                gpuImageFilterData.filterList.forEachIndexed { index, filterData ->
                    filterPreviewData.add(FilterPreviewData(imageFile = imageFile.first(), isSelect = index == selectFilterList.first(), filterData = filterData))
                }
                uiData.value = UIModel(filterPreviewData = filterPreviewData)
            }
        }
    }

    fun changeSampleImageFilter(position: Int) {
        val filterPreviewData = ArrayList<FilterPreviewData>()
        gpuImageFilterData.filterList.forEachIndexed { index, filterData ->
            filterPreviewData.add(FilterPreviewData(imageFile = imageFile[position], isSelect = index == selectFilterList[position], filterData = filterData))
        }
        uiData.value = UIModel(filterPreviewData = filterPreviewData)
    }

    fun changePreviewFilter(pagerIndex: Int, filterIndex: Int) {
        PrintLog.d("pagerIndex", pagerIndex.toString(), tag)
        PrintLog.d("filterIndex", filterIndex.toString(), tag)
        launch {
            Observable.create<Boolean> {
                selectFilterList[pagerIndex] = filterIndex

                it.onNext(true)
                it.onComplete()
            }.with(scheduler).subscribe {
                if (imagePathList.size > 1) { // 여러 이미지
                    imageFilterEvent.value = ChangeFilterData(filterIndex = filterIndex)
                } else { // 단일 이미지
                    imageFilterEvent.value = ChangeFilterData(singleFilter =
                    FilterPreviewData(imageFile = imageFile.first(), isSelect = false, filterData = gpuImageFilterData.filterList[filterIndex]),
                            filterIndex = filterIndex)
                }
            }
        }
    }

    fun deleteProfileImage() {
        imagePathList.forEach { filePath ->
            PrintLog.d("deleteFile", filePath, tag)
            SupportData.deleteFile(path = filePath)
        }
    }

    fun createImageFileFilteredImage() {
        uiData.value = UIModel(isLoading = true)
        launch {
            Observable.create<ArrayList<String>> {
                val imagePathList = ArrayList<String>()

                bitmapList.forEach { bitmap ->
                    val outFile: File? = SupportData.createTmpFile(type = AppConstants.MEDIA_IMAGE)
                    outFile?.let { _ ->
                        SupportData.saveFile(bitmap = bitmap, path = outFile.absolutePath)
                        imagePathList.add(outFile.absolutePath)
                    }
                }

                it.onNext(imagePathList)
                it.onComplete()
            }.with(scheduler).subscribe{
                uiData.value = UIModel(isLoading = false)
                if (it.isNotEmpty())
                    uiData.value = UIModel(finalImagePathList = it)
            }
        }
    }
}

data class FilterPreviewData(val imageFile: File, val isSelect: Boolean, val filterData: GPUImageFilterData.FilterData)
data class ChangeFilterData(val singleFilter: FilterPreviewData? = null, val filterIndex: Int? = null)
data class UIModel(val isLoading: Boolean? = null,
                   val oneImagePreViewVisible: Int? = null, val manyImagePreViewVisible: Int? = null,
                   val imageFile: File? = null, val imageFileList: ArrayList<File>? = null,
                   val filterPreviewData: ArrayList<FilterPreviewData>? = null,
                   val finalImagePathList: ArrayList<String>? = null)
