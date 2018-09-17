package xlab.world.xlab.view.galleryImageSelect

import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.provider.MediaStore
import io.reactivex.Observable
import org.koin.dsl.module.applicationContext
import xlab.world.xlab.data.adapter.GalleryData
import xlab.world.xlab.data.adapter.GalleryListData
import xlab.world.xlab.utils.rx.SchedulerProvider
import xlab.world.xlab.utils.rx.with
import xlab.world.xlab.utils.support.PrintLog
import xlab.world.xlab.view.AbstractViewModel
import xlab.world.xlab.view.SingleLiveEvent

class GalleryImageSelectViewModel(private val scheduler: SchedulerProvider): AbstractViewModel() {
    val tag = "GalleryImageSelect"

    private val galleryOnePage = 40

    val loadGalleryImageEvent = SingleLiveEvent<GalleryEvent>()
    val uiData = MutableLiveData<UIModel>()

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
            }
        }
    }
}

data class GalleryEvent(val status: Boolean? = null)
data class UIModel(val galleryData: GalleryData? = null)