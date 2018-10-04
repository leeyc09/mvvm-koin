package xlab.world.xlab.utils.camera

interface ICameraHelper {
    fun takePicture(picturePath: (String) -> Unit)
    fun recordVideo()
    fun stopRecordVideo()
    fun changeFlashMode(flashMode: (String) -> Unit)
    fun changeCameraID(cameraID: (Int) -> Unit)
}