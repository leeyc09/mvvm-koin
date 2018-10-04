package xlab.world.xlab.view.galleryImageSelect

import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.graphics.Bitmap
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
import kotlin.math.max

class GalleryImageSelectViewModel(private val scheduler: SchedulerProvider): AbstractViewModel() {
    val tag = "GalleryImageSelect"

    private val galleryOnePage = 40
    private var maxSelectCount: Int = 3 // limit item select count

    private var singleSelectIndex = 0
    private var singleSelectData: GalleryListData? = null

    private var multiSelectIndex: ArrayList<Int> = ArrayList()
    private var multiSelectData: ArrayList<GalleryListData> = ArrayList()

    val loadGalleryImageEvent = SingleLiveEvent<GalleryEvent>()
    val uiData = MutableLiveData<UIModel>()

    fun setSingleSelectData(selectData: GalleryListData) {
        singleSelectData = selectData
    }

    fun addMultiSelectData(selectData: GalleryListData) {
        multiSelectData.add(selectData)
        multiSelectIndex.add(0)
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
                                selectNum = if(isSelect) 1 else null))
                    }
                    cursor.close()

                    it.onNext(galleryData)
                    it.onComplete()
                }
            }.with(scheduler).subscribe { resultData ->
                PrintLog.d("loadGalleryImage", resultData.toString(), tag)
                uiData.value = UIModel(galleryData = resultData)
                if (resultData.items.isEmpty())
                    uiData.value = UIModel(toastMessage = TextConstants.EMPTY_GALLERY)
                else if (resultData.items.isNotEmpty() && page == 1) {
                    singleSelectIndex = 0
                    singleSelectData = resultData.items.first()
                    uiData.value = UIModel(oneSelectData = SelectData(position = singleSelectIndex, data = singleSelectData!!.data))
                }
            }
        }
    }

    // 선택 이미지 변경 (이미지 선택 1개)
    fun changeImageSelect(position: Int, newSelectedData: GalleryListData) {
        launch {
            Observable.create<ArrayList<Int>> {
                singleSelectData?.let { selectData ->
                    if (selectData.data != newSelectedData.data) {
                        val updatePosition = arrayListOf(singleSelectIndex, position)
                        // 기존 선택된 데이터 선택 해제 & 새로운 데이터로 갱신
                        selectData.isSelect = false
                        singleSelectData = newSelectedData
                        singleSelectIndex = position
                        // 새로 선택된 데이터 선택
                        newSelectedData.isSelect = true

                        it.onNext(updatePosition)
                        it.onComplete()
                    }
                }
            }.with(scheduler).subscribe {
                it.forEach { position ->
                    uiData.value = UIModel(galleryUpdatePosition = position)
                }
                uiData.value = UIModel(oneSelectData = SelectData(position = singleSelectIndex, data = singleSelectData!!.data))
            }
        }
    }

    fun directSelectImage(position: Int, selectData: GalleryListData) {
        launch {
            Observable.create<ArrayList<Int>> {
                val updatePosition = ArrayList<Int>()
                updatePosition.addAll(multiSelectIndex)

                if (selectData.isSelect) { // 기존 선택 O 이미지
                    if (multiSelectData.size > 1) { // 선택 된 이미지가 2개 이상
                        selectData.selectNum = null
                        multiSelectIndex.remove(position)
                        multiSelectData.remove(selectData)

                        multiSelectData.forEachIndexed { index, data ->
                            data.selectNum = index + 1
                        }

                        selectData.isSelect = false
                        updatePosition.add(position)
                    }
                } else { // 기존 선택 X 이미지
                    if (multiSelectData.size < maxSelectCount) { // 선택 이미지가 max 갯수보다 작을 경우
                        multiSelectIndex.add(position)
                        multiSelectData.add(selectData)

                        selectData.selectNum = multiSelectData.size

                        selectData.isSelect = true
                        updatePosition.add(position)
                    }
                }

                PrintLog.d("multiSelectIndex", multiSelectIndex.toString(), tag)
                PrintLog.d("multiSelectData", multiSelectData.toString(), tag)

                it.onNext(updatePosition)
                it.onComplete()
            }.with(scheduler).subscribe {
                it.forEach { position ->
                    uiData.value = UIModel(galleryUpdatePosition = position)
                }
//                uiData.value = UIModel(oneSelectData = SelectData(position = singleSelectIndex, data = singleSelectData!!.data))
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
                    it.onComplete()
                }?:let{_->
                    uiData.value = UIModel(isLoading = false)
                }
            }.with(scheduler).subscribe{
                uiData.value = UIModel(isLoading = false, finalImagePath = it)
            }
        }
    }
}

data class SelectPosition(val oldPosition: Int, val newPosition: Int)
data class SelectData(val position: Int, val data: String)
data class GalleryEvent(val status: Boolean? = null)
data class UIModel(val isLoading: Boolean? = null, val toastMessage: String? = null,
                   val oneSelectData: SelectData? = null,
                   val galleryData: GalleryData? = null, val galleryUpdatePosition: Int? = null,
                   val finalImagePath: String? = null)