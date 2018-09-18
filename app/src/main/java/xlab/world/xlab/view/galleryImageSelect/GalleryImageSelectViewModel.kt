package xlab.world.xlab.view.galleryImageSelect

import android.app.Activity
import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.content.Intent
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

class GalleryImageSelectViewModel(private val scheduler: SchedulerProvider): AbstractViewModel() {
    val tag = "GalleryImageSelect"

    private val galleryOnePage = 40
    var singleSelectIndex = 0
    private var singleSelectData: GalleryListData? = null

    val loadGalleryImageEvent = SingleLiveEvent<GalleryEvent>()
    val uiData = MutableLiveData<UIModel>()

    fun setSingleSelectData(selectData: GalleryListData) {
        singleSelectData = selectData
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
                        galleryData.items.add(GalleryListData(
                                dataType  = dataType,
                                id = cursor.getString(0),
                                title = cursor.getString(1),
                                data = cursor.getString(2),
                                size = cursor.getString(3),
                                displayName = cursor.getString(4),
                                selected = (page == 1 && galleryData.items.isEmpty())))
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

    fun changeImageSelect(position: Int, newSelectedData: GalleryListData) {
        launch {
            Observable.create<SelectPosition> {
                singleSelectData?.let { selectData ->
                    if (selectData.data != newSelectedData.data) {
                        val selectedPosition = SelectPosition(oldPosition = singleSelectIndex, newPosition = position)
                        // 기존 선택된 데이터 선택 해제 & 새로운 데이터로 갱신
                        selectData.selected = false
                        singleSelectData = newSelectedData
                        singleSelectIndex = position
                        // 새로 선택된 데이터 선택
                        newSelectedData.selected = true

                        it.onNext(selectedPosition)
                        it.onComplete()
                    }
                }
            }.with(scheduler).subscribe {
                uiData.value = UIModel(galleryUpdatePosition = it.oldPosition)
                uiData.value = UIModel(galleryUpdatePosition = it.newPosition)
                uiData.value = UIModel(oneSelectData = SelectData(position = singleSelectIndex, data = singleSelectData!!.data))
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