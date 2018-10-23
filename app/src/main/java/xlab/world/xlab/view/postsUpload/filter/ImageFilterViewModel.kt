package xlab.world.xlab.view.postsUpload.filter

import android.arch.lifecycle.MutableLiveData
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.View
import io.reactivex.Observable
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
    private val viewModelTag = "ImageFilter"

    private var bitmapList: ArrayList<Bitmap> = ArrayList()
    private var imagePathList: ArrayList<String> = ArrayList()
    private var imageFile: ArrayList<File> = ArrayList()
    private var selectFilterList: ArrayList<Int> = ArrayList()

    val imageFilterData = SingleLiveEvent<ChangeFilterModel>()
    val uiData = MutableLiveData<UIModel>()

    // 필터 적용 시킨 이미지로 비트맵 업데이트
    fun updateBitmap(bitmap: Bitmap, index: Int) {
        bitmapList[index] = bitmap.copy(bitmap.config, true)
    }

    // 이미지 필터 데이터 초기화
    fun initImageFilterData(imagePaths: ArrayList<String>) {
        uiData.value = UIModel(isLoading = true)
        launch {
            Observable.create<Int> {
                this.imagePathList.clear()
                this.imagePathList.addAll(imagePaths)
                PrintLog.d("imagePaths", this.imagePathList.toString(), viewModelTag)

                // 이미지 경로로 파일 & 비트맵 생성
                this.imagePathList.forEach { path ->
                    imageFile.add(File(path))
                    val bitmap = BitmapFactory.decodeFile(path)
                    bitmapList.add(bitmap)
                    selectFilterList.add(0)
                }

                it.onNext(this.imagePathList.size)
                it.onComplete()
            }.with(scheduler = scheduler).subscribe { imagePathSize ->
                if (imagePathSize > 1) { // 여러 이미지
                    uiData.value = UIModel(isLoading = false, oneImagePreViewVisible = View.GONE,
                            manyImagePreViewVisible = View.VISIBLE, imageFileList = imageFile)
                } else { // 단일 이미지
                    uiData.value = UIModel(isLoading = false, oneImagePreViewVisible = View.VISIBLE,
                            manyImagePreViewVisible = View.GONE, imageFile = imageFile.first())
                }
            }
        }
    }

    // 프리뷰 이미지 변경 -> 필터 미리보기 데이터 변경
    fun changeSampleImageFilter(position: Int) {
        launch {
            Observable.create<ArrayList<FilterPreviewData>> {
                val filterPreviewData = ArrayList<FilterPreviewData>()
                gpuImageFilterData.filterList.forEachIndexed { index, filterData ->
                    filterPreviewData.add(FilterPreviewData(imageFile = imageFile[position], isSelect = index == selectFilterList[position], filterData = filterData))
                }

                it.onNext(filterPreviewData)
                it.onComplete()
            }.with(scheduler = scheduler).subscribe {
                PrintLog.d("filterPreviewData", it.toString(), viewModelTag)
                uiData.value = UIModel(filterPreviewData = it)
            }
        }
    }

    // 프리뷰 필터 변경
    fun changePreviewFilter(pagerIndex: Int, filterIndex: Int) {
        PrintLog.d("pagerIndex", pagerIndex.toString(), viewModelTag)
        PrintLog.d("filterIndex", filterIndex.toString(), viewModelTag)
        launch {
            Observable.create<Int> {
                // 현재 프리뷰 이미지의 필터 변경
                selectFilterList[pagerIndex] = filterIndex

                it.onNext(filterIndex)
                it.onComplete()
            }.with(scheduler = scheduler).subscribe {
                if (imagePathList.size > 1) { // 여러 이미지
                    imageFilterData.value = ChangeFilterModel(filterIndex = it)
                } else { // 단일 이미지
                    imageFilterData.value = ChangeFilterModel(
                            singleFilter = FilterPreviewData(imageFile = imageFile.first(),
                                    isSelect = false, // <- 의미 X
                                    filterData = gpuImageFilterData.filterList[it]),
                            filterIndex = it)
                }
            }
        }
    }

    fun deleteFilterImage() {
        imagePathList.forEach { filePath ->
            PrintLog.d("deleteFile", filePath, viewModelTag)
            SupportData.deleteFile(path = filePath)
        }
    }
    // 필터 적용 된 이미지 image file 만들기
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
            }.with(scheduler = scheduler).subscribe{
                uiData.value = UIModel(isLoading = false)
                if (it.isNotEmpty())
                    uiData.value = UIModel(finalImagePathList = it)
            }
        }
    }
}

data class FilterPreviewData(val imageFile: File, val isSelect: Boolean, val filterData: GPUImageFilterData.FilterData)
data class ChangeFilterModel(val singleFilter: FilterPreviewData? = null, val filterIndex: Int? = null)
data class UIModel(val isLoading: Boolean? = null,
                   val oneImagePreViewVisible: Int? = null, val manyImagePreViewVisible: Int? = null,
                   val imageFile: File? = null, val imageFileList: ArrayList<File>? = null,
                   val filterPreviewData: ArrayList<FilterPreviewData>? = null,
                   val finalImagePathList: ArrayList<String>? = null)
