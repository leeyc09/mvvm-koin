package xlab.world.xlab.utils.camera

import android.graphics.drawable.Drawable

interface ICameraHelper {
    fun takePicture(picturePath: (String) -> Unit)
    fun recordVideo()
    fun stopRecordVideo()
    fun changeFlashMode(flashModeImage: (Drawable?) -> Unit)
    fun changeCameraID(flashEnable: (Boolean) -> Unit)
}