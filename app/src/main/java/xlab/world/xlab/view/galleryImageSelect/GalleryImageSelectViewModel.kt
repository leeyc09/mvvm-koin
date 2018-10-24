package xlab.world.xlab.view.galleryImageSelect

import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.provider.MediaStore
import io.reactivex.Observable
import xlab.world.xlab.R
import xlab.world.xlab.data.adapter.GalleryData
import xlab.world.xlab.data.adapter.GalleryListData
import xlab.world.xlab.utils.rx.SchedulerProvider
import xlab.world.xlab.utils.rx.with
import xlab.world.xlab.utils.support.AppConstants
import xlab.world.xlab.utils.support.PrintLog
import xlab.world.xlab.utils.support.SupportData
import xlab.world.xlab.view.AbstractViewModel
import xlab.world.xlab.view.SingleLiveEvent
import java.io.File

class GalleryImageSelectViewModel(private val scheduler: SchedulerProvider): AbstractViewModel() {
    private val viewModelTag = "GalleryImageSelect"

    private val galleryOnePage = 40
    private var maxSelectCount: Int = 3 // limit item select count

    private var galleryData: GalleryData = GalleryData()

    private var selectIndexList: ArrayList<Int> = ArrayList()
    private var selectDataList: ArrayList<GalleryListData> = ArrayList()
    private var selectMatrixList: ArrayList<Matrix> = ArrayList()
    private var selectBitmapList: ArrayList<Bitmap?> = ArrayList()

    val loadGalleryData = SingleLiveEvent<Boolean?>()
    val uiData = MutableLiveData<UIModel>()

    // 유저 레벨에 따른 이미지 선택 갯수 제한
    fun initMaxSelectCount(userLevel: Int) {
        maxSelectCount =
                if (userLevel == AppConstants.ADMIN_USER_LEVEL) 10
                else 3
    }
    // 선택 된 이미지 matrix 정보 저장
    fun updateMatrix(matrix: Matrix) {
        selectDataList.forEachIndexed { index, data ->
            if (data.isPreview) {
                selectMatrixList[index] = matrix
                return
            }
        }
    }
    // 선택 된 이미지 bitmap 정보 저장
    fun updateBitmap(bitmap: Bitmap) {
        selectDataList.forEachIndexed { index, data ->
            if (data.isPreview) {
                selectBitmapList[index] = bitmap
                return
            }
        }
    }
    // 선택 갤러리 데이터 업데이트
    private fun updateSelectDataList(index: Int) {
        launch {
            Observable.create<Any> {
                PrintLog.d("updateSelectDataList Index", index.toString(), viewModelTag)
                selectIndexList.clear()
                selectDataList.clear()
                selectMatrixList.clear()
                selectBitmapList.clear()

                selectIndexList.add(index)
                selectDataList.add(galleryData.items[index])
                selectMatrixList.add(Matrix())
                selectBitmapList.add(null)

                printSelectDataList()
            }.with(scheduler = scheduler).subscribe {}
        }
    }
    // 선택 갤러리 데이터 추가
    private fun addSelectDataList(index: Int) {
        launch {
            Observable.create<Any> {
                PrintLog.d("addSelectDataList Index", index.toString(), viewModelTag)
                selectIndexList.add(index)
                selectDataList.add(galleryData.items[index])
                selectMatrixList.add(Matrix())
                selectBitmapList.add(null)

                printSelectDataList()
            }.with(scheduler = scheduler).subscribe {}
        }
    }
    // 선택 갤러리 데이터 삭제
    private fun removeSelectDataList(index: Int) {
        launch {
            Observable.create<Any> {
                PrintLog.d("removeSelectDataList Index", index.toString(), viewModelTag)
                var removeIndex = 0
                run selectIndexList@ {
                    selectIndexList.forEachIndexed { i, value ->
                        if (index == value) {
                            removeIndex = i
                            return@selectIndexList
                        }
                    }
                }
                selectIndexList.removeAt(removeIndex)
                selectMatrixList.removeAt(removeIndex)
                selectDataList.removeAt(removeIndex)
                selectBitmapList.removeAt(removeIndex)

                printSelectDataList()
            }.with(scheduler = scheduler).subscribe {}
        }
    }
    // 선택 갤러리 출력
    private fun printSelectDataList() {
        PrintLog.d("selectIndexList", selectIndexList.toString(), viewModelTag)
        PrintLog.d("selectDataList", selectDataList.toString(), viewModelTag)
    }
    // 갤러리에 있는 이미지 불러오기
    fun loadGalleryImage(context: Context, page: Int, dataType: Int) {
        loadGalleryData.value = true
        launch {
            Observable.create<GalleryData> {
                // 페이징 처리한 이미지 불러오기 query
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
                                selectNum = if(isSelect) 1 else null)) // 첫번째 페이지 & 첫번째 사진 선택 상태로
                    }
                    cursor.close()

                    it.onNext(galleryData)
                    it.onComplete()
                }
            }.with(scheduler = scheduler).subscribe {
                if (page == 1) // 요청한 page => 첫페이지
                    this.galleryData.updateData(galleryData = it)
                else
                    this.galleryData.addData(galleryData = it)

                PrintLog.d("loadGalleryImage", it.toString(), viewModelTag)
                if (page == 1) {
                    uiData.value = UIModel(galleryData = this.galleryData)

                    if (it.items.isEmpty()) // 첫번째 페이지, 갤러리에 이미지가 없는경우
                        uiData.value = UIModel(toastMessage = context.getString(R.string.toast_no_exist_gallery_image))
                    else if (it.items.isNotEmpty()) { // 첫번째 페이지 -> 미리보기 이미지 제공
                        uiData.value = UIModel(imagePreviewData = PreviewData(data = this.galleryData.items.first().data, matrix = Matrix()))
                        // 선택 갤러리 데이터 update
                        updateSelectDataList(index = 0)
                    }
                } else {
                    uiData.value = UIModel(galleryDataUpdate = true)
                }
            }
        }
    }

    // 선택 이미지 변경 (이미지 단일 선택)
    fun singleSelectImageChange(selectIndex: Int) {
        launch {
            Observable.create<ArrayList<Int>> {
                if (selectIndexList.last() != selectIndex) {
                    // 기존 선택된 데이터 선택 해제 & 새로운 데이터 선택
                    selectDataList.last().isSelect = false
                    selectDataList.last().isPreview = false

                    val item = galleryData.items[selectIndex]
                    item.isSelect = true
                    item.isPreview = true

                    selectIndexList.add(selectIndex)
                    selectDataList.add(item)

                    it.onNext(selectIndexList)
                    it.onComplete()
                }
            }.with(scheduler = scheduler).subscribe {
                // 갤러리 데이터 화면 업데이트
                it.forEach { index ->
                    uiData.value = UIModel(galleryUpdateIndex = index)
                }
                // preview data
                uiData.value = UIModel(imagePreviewData = PreviewData(data = selectDataList.last().data, matrix = Matrix()))

                // 선택 갤러리 데이터 update
                updateSelectDataList(index = it.last())
            }
        }
    }

    // 선택 이미지 바로 변경 (이미지 다수 선택)
    fun directMultiSelectImageChange(selectIndex: Int) {
        launch {
            var previewImage: String? = null
            var previewMatrix = Matrix()
            var addOrRemove = -1 // 0 - remove, 1 - add
            Observable.create<ArrayList<Int>> {
                val updatePosition = ArrayList<Int>()
                updatePosition.addAll(selectIndexList)

                val item = galleryData.items[selectIndex]
                if (item.isSelect) { // 기존 선택된 이미지 -> 선택 해제
                    if (selectDataList.size > 1) { // 선택 된 이미지가 2개 이상일 경우만 해제 가능
                        val preViewData = gallerySelectCancel(selectIndex = selectIndex)
                        previewImage = preViewData.data
                        previewMatrix = preViewData.matrix

                        addOrRemove = 0
                        updatePosition.add(selectIndex)
                    }
                } else { // 기존 선택 안된 이미지 -> 선택
                    if (selectDataList.size < maxSelectCount) { // 선택 이미지가 max 갯수보다 작을 경우 -> 선택 가능
                        val preViewData = gallerySelect(selectIndex = selectIndex)
                        previewImage = preViewData.data
                        previewMatrix = preViewData.matrix

                        addOrRemove = 1
                        updatePosition.add(selectIndex)
                    }
                }

                it.onNext(updatePosition)
                it.onComplete()
            }.with(scheduler = scheduler).subscribe {
                // 갤러리 데이터 화면 업데이트
                it.forEach { index ->
                    uiData.value = UIModel(galleryUpdateIndex = index)
                }
                // preview data
                previewImage?.let { data ->
                    uiData.value = UIModel(imagePreviewData = PreviewData(data = data, matrix = previewMatrix))
                }

                // 선택 갤러리 데이터 update
                if (addOrRemove == 0) // remove
                    removeSelectDataList(index = selectIndex)
                else if (addOrRemove == 1) // add
                    addSelectDataList(index = selectIndex)
            }
        }
    }
    // 선택 이미지 체크 후 변경 (이미지 다수 선택)
    fun multiSelectImageChange(selectIndex: Int) {
        launch {
            var previewImage: String? = null
            var previewMatrix = Matrix()
            var addOrRemove = -1 // 0 - remove, 1 - add
            Observable.create<ArrayList<Int>> {
                val updatePosition = ArrayList<Int>()
                updatePosition.addAll(selectIndexList)

                val item = galleryData.items[selectIndex]
                if (item.isSelect) { // 기존 선택된 이미지
                    if (item.isPreview) { // 프리뷰 이미지 -> 선택 해제
                        if (selectDataList.size > 1) { // 선택 된 이미지가 2개 이상일 경우만 해제 가능
                            val preViewData = gallerySelectCancel(selectIndex = selectIndex)
                            previewImage = preViewData.data
                            previewMatrix = preViewData.matrix

                            addOrRemove = 0
                            updatePosition.add(selectIndex)
                        }
                    } else { // 프리뷰 이미지 아닌 경우 -> 프리뷰 변경
                        selectDataList.forEachIndexed { index, data ->
                            data.isPreview = selectIndexList[index] == selectIndex
                            if (data.isPreview) {
                                previewImage = data.data
                                previewMatrix = selectMatrixList[index]
                            }
                        }
                    }
                } else {  // 기존 선택 안된 이미지 -> 선택
                    if (selectDataList.size < maxSelectCount) { // 선택 이미지가 max 갯수보다 작을 경우 -> 선택 가능
                        val preViewData = gallerySelect(selectIndex = selectIndex)
                        previewImage = preViewData.data
                        previewMatrix = preViewData.matrix

                        addOrRemove = 1
                        updatePosition.add(selectIndex)
                    }
                }

                it.onNext(updatePosition)
                it.onComplete()
            }.with(scheduler = scheduler).subscribe {
                it.forEach { index ->
                    uiData.value = UIModel(galleryUpdateIndex = index)
                }
                previewImage?.let { data ->
                    uiData.value = UIModel(imagePreviewData = PreviewData(data = data, matrix = previewMatrix))
                }

                // 선택 갤러리 데이터 update
                if (addOrRemove == 0) // remove
                    removeSelectDataList(index = selectIndex)
                else if (addOrRemove == 1) // add
                    addSelectDataList(index = selectIndex)
            }
        }
    }

    // 갤러리 이미지 선택 해제
    private fun gallerySelectCancel(selectIndex: Int): PreviewData {
        var imageNum = 1
        var previewData: GalleryListData? = null
        var previewMatrix = Matrix()
        selectDataList.forEachIndexed { index, data ->
            if (selectIndexList[index] != selectIndex) { // 선택 해제 될 이미지 아닌 경우
                data.selectNum = imageNum++
                // preview image data 이면서 삭제 될 이미지가 아닌 경우 -> 해당 갤러리 데이터를 preview data 로 설정
                // preview image data 가 삭제 될 이미지인 경우 -> 아래에서 재 설정
                if (data.isPreview) {
                    previewData = data
                    previewMatrix = selectMatrixList[index]
                }
            } else { // 선택 해제 될 이미지 경우
                data.selectNum = null
                data.isSelect = false
                data.isPreview = false
            }
        }

        // preview image data 가 삭제 될 이미지인 경우 -> 기존 preview data 선택 해제
        // select data list 중 가장 마지막 데이터를 preview image 로 설정
        // 가장 마지막 데이터가 삭제 될 이미지인 경우 -> 마지막 이전 데이터로 설정
        if (previewData == null) {
            if (selectIndexList.last() != selectIndex) {
                previewData = selectDataList.last()
                previewMatrix = selectMatrixList.last()
            } else {
                previewData = selectDataList[selectDataList.size - 2]
                previewMatrix = selectMatrixList[selectMatrixList.size - 2]
            }
        }
        previewData!!.isPreview = true

        return PreviewData(data = previewData!!.data, matrix = previewMatrix)
    }

    // 갤러리 이미지 선택
    private fun gallerySelect(selectIndex: Int): PreviewData {
        val item = galleryData.items[selectIndex]
        item.selectNum = selectDataList.size + 1
        item.isSelect = true
        item.isPreview = true

        // 기존 프리뷰 해제
        selectDataList.forEach { data -> data.isPreview = false }

        return PreviewData(data = item.data, matrix = Matrix())
    }
    // 선택 된 갤러리 데이터 image file 만들기
    fun createImageFileBySelectedImageList() {
        uiData.value = UIModel(isLoading = true)
        launch {
            Observable.create<ArrayList<String>> {
                val imagePathList = ArrayList<String>()

                selectBitmapList.forEach { bitmap ->
                    val outFile: File? = SupportData.createTmpFile(type = AppConstants.MEDIA_IMAGE)
                    outFile?.let { _ ->
                        bitmap?.let {_->SupportData.saveFile(bitmap = bitmap, path = outFile.absolutePath)}
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

data class PreviewData(val data: String, val matrix: Matrix)
data class UIModel(val isLoading: Boolean? = null, val toastMessage: String? = null,
                   val overlayVisibility: Int? = null,
                   val imagePreviewData: PreviewData? = null,
                   val galleryData: GalleryData? = null, val galleryDataUpdate: Boolean? = null, val galleryUpdateIndex: Int? = null,
                   val finalImagePathList: ArrayList<String>? = null)