package xlab.world.xlab.view.galleryImageSelect

import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.provider.MediaStore
import io.reactivex.Observable
import xlab.world.xlab.data.adapter.GalleryData
import xlab.world.xlab.data.adapter.GalleryListData
import xlab.world.xlab.utils.rx.SchedulerProvider
import xlab.world.xlab.utils.rx.with
import xlab.world.xlab.utils.support.AppConstants
import xlab.world.xlab.utils.support.PrintLog
import xlab.world.xlab.utils.support.SupportData
import xlab.world.xlab.utils.support.TextConstants
import xlab.world.xlab.view.AbstractViewModel
import xlab.world.xlab.view.SingleLiveEvent
import java.io.File

class GalleryImageSelectViewModel(private val scheduler: SchedulerProvider): AbstractViewModel() {
    val tag = "GalleryImageSelect"

    private val galleryOnePage = 40
    private var maxSelectCount: Int = 3 // limit item select count

    private var selectIndexList: ArrayList<Int> = ArrayList()
    private var selectDataList: ArrayList<GalleryListData> = ArrayList()
    private var selectMatrixList: ArrayList<Matrix> = ArrayList()
    private var selectBitmapList: ArrayList<Bitmap?> = ArrayList()

    val loadGalleryImageEvent = SingleLiveEvent<GalleryEvent>()
    val imagePreviewEvent = SingleLiveEvent<ImagePreviewEvent>()
    val uiData = MutableLiveData<UIModel>()

    fun updateMatrix(matrix: Matrix) {
        selectDataList.forEachIndexed { index, data ->
            if (data.isPreview) {
                selectMatrixList[index] = matrix
            }
        }
    }

    fun updateBitmap(bitmap: Bitmap) {
        selectDataList.forEachIndexed { index, data ->
            if (data.isPreview) {
                selectBitmapList[index] = bitmap
            }
        }
    }

    fun updateSelectDataList(index: Int, selectData: GalleryListData) {
        launch {
            Observable.create<GalleryData> {
                selectIndexList.clear()
                selectDataList.clear()
                selectMatrixList.clear()
                selectBitmapList.clear()

                selectIndexList.add(index)
                selectDataList.add(selectData)
                selectMatrixList.add(Matrix())
                selectBitmapList.add(null)
            }.with(scheduler).subscribe {}
        }
    }

    fun addSelectDataList(index: Int, selectData: GalleryListData) {
        launch {
            Observable.create<GalleryData> {
                selectIndexList.add(index)
                selectDataList.add(selectData)
                selectMatrixList.add(Matrix())
                selectBitmapList.add(null)
            }.with(scheduler).subscribe {}
        }
    }

    fun removeSelectDataList(index: Int) {
        launch {
            Observable.create<GalleryData> {
                var removeIndex = -1
                selectIndexList.takeWhile { _-> removeIndex == -1 }.forEachIndexed { i, value ->
                    if (index == value) removeIndex = i
                }
                selectIndexList.removeAt(removeIndex)
                selectMatrixList.removeAt(removeIndex)
                selectDataList.removeAt(removeIndex)
                selectBitmapList.removeAt(removeIndex)
            }.with(scheduler).subscribe {}
        }
    }

    fun initMaxSelectCount(userLevel: Int) {
        maxSelectCount =
                if (userLevel == AppConstants.ADMIN_USER_LEVEL) 10
                else 3
    }

    fun loadGalleryImage(context: Context, page: Int, dataType: Int) {
        loadGalleryImageEvent.value = GalleryEvent(status = true)
        launch {
            Observable.create<GalleryData> {
                val orderBy = MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC LIMIT $galleryOnePage OFFSET ${(page - 1) * galleryOnePage}" // image load order
                val projection = arrayOf(
                        MediaStore.Images.Media._ID,
                        MediaStore.Images.Media.TITLE,
                        MediaStore.Images.Media.DATA,
                        MediaStore.Images.Media.SIZE,
                        MediaStore.Images.Media.DISPLAY_NAME)
                val cursor = context.contentResolver.query(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        projection,
                        null,
                        null,
                        orderBy)

                cursor?.let { _ ->
                    val galleryData = GalleryData(nextPage = page + 1)

                    while (cursor.moveToNext()) {
                        val isSelect = (page == 1 && galleryData.items.isEmpty())
                        galleryData.items.add(GalleryListData(
                                dataType  = dataType,
                                id = cursor.getString(0),
                                title = cursor.getString(1),
                                data = cursor.getString(2),
                                size = cursor.getString(3),
                                displayName = cursor.getString(4),
                                isSelect = isSelect,
                                isPreview = isSelect,
                                selectNum = if(isSelect) 1 else null))
                    }
                    cursor.close()

                    it.onNext(galleryData)
                    it.onComplete()
                }
            }.with(scheduler).subscribe { resultData ->
                PrintLog.d("loadGalleryImage", resultData.toString(), tag)
                uiData.value = UIModel(galleryData = resultData)
                if (resultData.items.isEmpty()) // 갤러리에 이미지가 없는경우
                    uiData.value = UIModel(toastMessage = TextConstants.EMPTY_GALLERY)
                else if (resultData.items.isNotEmpty() && page == 1) {
                    uiData.value = UIModel(imagePreviewData = PreviewData(data = resultData.items.first().data, matrix = Matrix()))
                    imagePreviewEvent.value = ImagePreviewEvent(updateIndex = 0)
                }
            }
        }
    }

    // 선택 이미지 변경 (이미지 단일 선택)
    fun singleSelectImageChange(position: Int, newSelectedData: GalleryListData) {
        launch {
            Observable.create<ArrayList<Int>> {
                if (selectDataList.isNotEmpty()) {
                    if (selectDataList.last() != newSelectedData) {
                        // 기존 선택된 데이터 선택 해제 & 새로운 데이터 선택
                        selectDataList.last().isSelect = false
                        selectDataList.last().isPreview = false

                        newSelectedData.isSelect = true
                        newSelectedData.isPreview = true

                        selectIndexList.add(position)
                        selectDataList.add(newSelectedData)

                        it.onNext(selectIndexList)
                        it.onComplete()
                    }
                }
            }.with(scheduler).subscribe {
                it.forEach { position ->
                    uiData.value = UIModel(galleryUpdatePosition = position)
                }
                uiData.value = UIModel(imagePreviewData = PreviewData(data = selectDataList.last().data, matrix = Matrix()))
                imagePreviewEvent.value = ImagePreviewEvent(updateIndex = selectIndexList.last())
            }
        }
    }

    // 선택 이미지 바로 변경 (이미지 다수 선택)
    fun directMultiSelectImageChange(position: Int, selectData: GalleryListData) {
        launch {
            var previewData = ""
            var previewMatrix = Matrix()
            var addOrRemove = -1 // 0 - remove, 1 - add
            Observable.create<ArrayList<Int>> {
                val updatePosition = ArrayList<Int>()
                updatePosition.addAll(selectIndexList)

                if (selectData.isSelect) { // 기존 선택 O 이미지 -> 선택 해제
                    if (selectDataList.size > 1) { // 선택 된 이미지가 2개 이상
                        selectData.selectNum = null
                        selectData.isSelect = false
                        selectData.isPreview = false

                        var imageNum = 1
                        selectDataList.forEachIndexed { index, data ->
                            // preview image data 이면서 삭제 될 이미지가 아닌 경우
                            if (data.isPreview && selectIndexList[index] != position) {
                                previewData = data.data
                                previewMatrix = selectMatrixList[index]
                            }
                            // 삭제 될 이미지 아닌 경우 이미지 번호
                            if (selectIndexList[index] != position) data.selectNum = imageNum++
                        }

                        if (previewData.isEmpty()) {
                            previewData = selectDataList[selectDataList.size - 2].data
                            previewMatrix = selectMatrixList[selectDataList.size - 2]
                        }

                        addOrRemove = 0
                        updatePosition.add(position)
                    }
                } else { // 기존 선택 X 이미지 -> 선택
                    if (selectDataList.size < maxSelectCount) { // 선택 이미지가 max 갯수보다 작을 경우
                        selectData.selectNum = selectDataList.size + 1
                        selectData.isSelect = true
                        selectData.isPreview = true

                        previewData = selectData.data

                        selectDataList.forEach { data -> data.isPreview = false }

                        addOrRemove = 1
                        updatePosition.add(position)
                    }
                }

                it.onNext(updatePosition)
                it.onComplete()
            }.with(scheduler).subscribe {
                it.forEach { position ->
                    uiData.value = UIModel(galleryUpdatePosition = position)
                }
                if (previewData.isNotEmpty())
                    uiData.value = UIModel(imagePreviewData = PreviewData(data = previewData, matrix = previewMatrix))

                if (addOrRemove == 0) // remove
                    imagePreviewEvent.value = ImagePreviewEvent(removeIndex = position)
                else if (addOrRemove == 1) // add
                    imagePreviewEvent.value = ImagePreviewEvent(addIndex = position)
            }
        }
    }
    // 선택 이미지 체크 후 변경 (이미지 다수 선택)
    fun multiSelectImageChange(position: Int, selectData: GalleryListData) {
        launch {
            var previewData = ""
            var previewMatrix = Matrix()
            var addOrRemove = -1 // 0 - remove, 1 - add
            Observable.create<ArrayList<Int>> {
                val updatePosition = ArrayList<Int>()
                updatePosition.addAll(selectIndexList)

                if (selectData.isSelect) { // 기존 선택 O 이미지
                    if (selectData.isPreview) { // 프리뷰에 있는 이미지 -> 선택 해제
                        if (selectDataList.size > 1) { // 선택 된 이미지가 2개 이상
                            selectData.selectNum = null
                            selectData.isSelect = false
                            selectData.isPreview = false

                            var imageNum = 1
                            selectDataList.forEachIndexed { index, data ->
                                // preview image data 이면서 삭제 될 이미지가 아닌 경우
                                if (data.isPreview && selectIndexList[index] != position) {
                                    previewData = data.data
                                    previewMatrix = selectMatrixList[index]
                                }
                                // 삭제 될 이미지 아닌 경우 이미지 번호
                                if (selectIndexList[index] != position) data.selectNum = imageNum++
                            }

                            if (previewData.isEmpty()) {
                                previewData = selectDataList[selectDataList.size - 2].data
                                previewMatrix = selectMatrixList[selectDataList.size - 2]
                            }

                            addOrRemove = 0
                            updatePosition.add(position)
                        }
                    } else { // 프리뷰 변경
                        selectDataList.forEachIndexed { index, data ->
                            data.isPreview = data.data == selectData.data
                            if (data.isPreview) {
                                previewData = data.data
                                previewMatrix = selectMatrixList[index]
                            }
                        }
                    }
                } else { // 기존 선택 X 이미지 -> 선택
                    if (selectDataList.size < maxSelectCount) { // 선택 이미지가 max 갯수보다 작을 경우
                        selectData.selectNum = selectDataList.size + 1
                        selectData.isSelect = true
                        selectData.isPreview = true

                        previewData = selectData.data

                        selectDataList.forEach { data -> data.isPreview = false }

                        addOrRemove = 1
                        updatePosition.add(position)
                    }
                }

                it.onNext(updatePosition)
                it.onComplete()
            }.with(scheduler).subscribe {
                it.forEach { position ->
                    uiData.value = UIModel(galleryUpdatePosition = position)
                }
                if (previewData.isNotEmpty())
                    uiData.value = UIModel(imagePreviewData = PreviewData(data = previewData, matrix = previewMatrix))

                if (addOrRemove == 0) // remove
                    imagePreviewEvent.value = ImagePreviewEvent(removeIndex = position)
                else if (addOrRemove == 1) // add
                    imagePreviewEvent.value = ImagePreviewEvent(addIndex = position)
            }
        }
    }

    fun createImageFileBySelectedImage(bitmap: Bitmap) {
        uiData.value = UIModel(isLoading = true)
        launch {
            Observable.create<String> {
                val outFile: File? = SupportData.createTmpFile(type = AppConstants.MEDIA_IMAGE)
                outFile?.let { _ ->
                    SupportData.saveFile(bitmap = bitmap, path = outFile.absolutePath)
                    it.onNext(outFile.absolutePath)
                }?:let{_->
                    it.onNext("")
                }
                it.onComplete()
            }.with(scheduler).subscribe{
                uiData.value = UIModel(isLoading = false)
                if (it.isNotEmpty())
                    uiData.value = UIModel(finalImagePath = it)
            }
        }
    }

    fun createImageFileBySelectedImageList(lastBitmap: Bitmap) {
        uiData.value = UIModel(isLoading = true)
        launch {
            Observable.create<ArrayList<String>> {
                val imagePathList = ArrayList<String>()
                selectDataList.forEachIndexed { index, data ->
                    if (data.isPreview) {
                        PrintLog.d("isPreview index", index.toString(), tag)
                        selectBitmapList[index] = lastBitmap
                    }
                }

                selectBitmapList.forEach { bitmap ->
                    val outFile: File? = SupportData.createTmpFile(type = AppConstants.MEDIA_IMAGE)
                    outFile?.let { _ ->
                        bitmap?.let {_->SupportData.saveFile(bitmap = bitmap, path = outFile.absolutePath)}
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

data class PreviewData(val data: String, val matrix: Matrix)
data class ImagePreviewEvent(val updateIndex: Int? = null, val addIndex: Int? = null, val removeIndex: Int? = null)
data class GalleryEvent(val status: Boolean? = null)
data class UIModel(val isLoading: Boolean? = null, val toastMessage: String? = null,
//                   val imagePreviewData: SelectData? = null,
                   val imagePreviewData: PreviewData? = null,
                   val galleryData: GalleryData? = null, val galleryUpdatePosition: Int? = null,
                   val finalImagePath: String? = null, val finalImagePathList: ArrayList<String>? = null)