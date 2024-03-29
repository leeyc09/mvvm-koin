package xlab.world.xlab.utils.camera

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.SurfaceTexture
import android.graphics.drawable.Drawable
import android.hardware.Camera
import android.media.CamcorderProfile
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.AsyncTask
import android.support.v4.content.res.ResourcesCompat
import android.view.*
import xlab.world.xlab.R
import xlab.world.xlab.utils.asyncTask.ImageSaveTask
import xlab.world.xlab.utils.support.AppConstants
import xlab.world.xlab.utils.support.PrintLog
import xlab.world.xlab.utils.support.SupportData
import java.io.File
import java.io.IOException

class CameraHelper(private val context: Context,
                   private val textureView: TextureView) : TextureView.SurfaceTextureListener, ICameraHelper {

    private val helperTag = "Camera"

    enum class CameraMode { PICTURE, VIDEO }

    var cameraMode: CameraMode = CameraMode.PICTURE // 카메라 모드 (사진, 동영상)
    var isCameraReloading = false

    private var cameraRotate = 0
    private var saveRotate = 0
    private val recordTimeMax = 10 * 1000

    var cameraID = Camera.CameraInfo.CAMERA_FACING_BACK // 전면, 후면 카메라 모드
    var cameraFlash = Camera.Parameters.FLASH_MODE_OFF // 플레시 on, off, auto 모드

    private var camcorderProfile: CamcorderProfile? = null
    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    private var outputFile: File? = null
    var isRecording: Boolean = false

    private var camera: Camera? = null

    private lateinit var surfaceTexture: SurfaceTexture
    private var isSurfaceCreated: Boolean = false

    private var cameraBestSize: Camera.Size? = null
    private var isStoped = false

    init {
        this.textureView.surfaceTextureListener = this
    }

    override
    fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {
    }

    override
    fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {
    }

    override
    fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
        releaseCamera()
        return false
    }

    override
    fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {
        this.surfaceTexture = surface!!
        this.isSurfaceCreated = true
        cameraReload()
    }

    // 사진 촬영
    override
    fun takePicture(picturePath: (String) -> Unit) {
        this.saveRotate = cameraRotate // 카메라 회전 반영

        try {
            camera?.takePicture(null, null, Camera.PictureCallback{ data, camera ->
                val pictureFile: File? = SupportData.createReleaseFile(AppConstants.MEDIA_IMAGE)
                if (pictureFile != null) {
                    camera!!.stopPreview()
                    val isFrontCam = isFrontCamera()
                    // 촬영 된 데이터 image file 로 저장
                    ImageSaveTask(data = data, pictureFile = pictureFile, isFrontCam = isFrontCam, saveRotate = saveRotate.toFloat()) { path ->
                        picturePath(path)
                    }.execute()
                }
            })
        } catch (e: Exception) {
            PrintLog.e("takePicture", e.message!!, helperTag)
            picturePath("")
        }
    }

    override
    fun recordVideo() {
        MediaPrepareTask().execute()
    }

    override
    fun stopRecordVideo() {
        try {
            mediaRecorder!!.stop() // stop the recording
        } catch (e: RuntimeException) {
            PrintLog.e("RuntimeException", e.message!!, helperTag)
            outputFile!!.delete()
        }
        releaseMediaRecorder() // release the media recorder object
        camera!!.lock() // take camera access back from media recorder

        // inform the user that recording has stopped
        isRecording = false

        try {
            val path = SupportData.moveFile(outputFile!!, SupportData.createReleaseFile(AppConstants.MEDIA_VIDEO)!!)
        } catch (e: IOException) {
            PrintLog.e("IOException", e.message!!, helperTag)
        }
    }

    // 플래시 모드 변경
    override
    fun changeFlashMode(flashModeImage: (Drawable?) -> Unit) {
        val parameters: Camera.Parameters = camera!!.parameters

        try {
            // 플래시 상태
            // on -> auto -> off -> on 으로 변경
            parameters.flashMode = when(parameters.flashMode) {
                Camera.Parameters.FLASH_MODE_ON -> Camera.Parameters.FLASH_MODE_AUTO
                Camera.Parameters.FLASH_MODE_AUTO -> Camera.Parameters.FLASH_MODE_OFF
                Camera.Parameters.FLASH_MODE_OFF -> Camera.Parameters.FLASH_MODE_ON
                else -> parameters.flashMode
            }

            cameraFlash = parameters.flashMode
            camera!!.parameters = parameters

            // 플래시 상태에 따른 이미지
            val flashDrawable: Drawable = when(cameraFlash) {
                Camera.Parameters.FLASH_MODE_ON -> ResourcesCompat.getDrawable(context.resources, R.drawable.flash_on, null)!!
                Camera.Parameters.FLASH_MODE_OFF -> ResourcesCompat.getDrawable(context.resources, R.drawable.flash_off, null)!!
                Camera.Parameters.FOCUS_MODE_AUTO -> ResourcesCompat.getDrawable(context.resources, R.drawable.flash_auto, null)!!
                else -> ResourcesCompat.getDrawable(context.resources, R.drawable.flash_on, null)!!
            }

            flashModeImage(flashDrawable)
        } catch (e: Exception) {
            PrintLog.e("changeFlashMode", e.message!!, helperTag)
            flashModeImage(null)
        }
    }

    // 카메라 전환
    override
    fun changeCameraID(flashEnable: (Boolean) -> Unit) {
        this.cameraID = when (this.cameraID) {
            Camera.CameraInfo.CAMERA_FACING_FRONT -> Camera.CameraInfo.CAMERA_FACING_BACK
            Camera.CameraInfo.CAMERA_FACING_BACK -> Camera.CameraInfo.CAMERA_FACING_FRONT
            else -> this.cameraID
        }
        cameraReload()

        flashEnable(cameraID == Camera.CameraInfo.CAMERA_FACING_BACK)
    }

    fun cameraReload() {
        if (!isCameraReloading) {
            PrintLog.d("cameraReload", "", helperTag)
            isCameraReloading = true
            CameraPrepareTask().execute()
        }
    }

    // 카메라 세팅
    private fun cameraPrepare() {
        releaseCamera()

        camera = Camera.open(cameraID)
        setCameraDisplayOrientation(cameraID = cameraID, camera = camera!!)
        val parameters: Camera.Parameters = camera!!.parameters
        val sizeList: List<Camera.Size> = camera!!.parameters.supportedPreviewSizes
        cameraBestSize = sizeList[0]

        sizeList.forEach { size ->
            if ( (size.width * size.height) > (cameraBestSize!!.width * cameraBestSize!!.height) ) {
                cameraBestSize = size
            }
        }
        PrintLog.d("cameraBestSize", "w: " + cameraBestSize!!.width + "  h: " + cameraBestSize!!.height, helperTag)

        camcorderProfile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH)
        camcorderProfile!!.videoFrameWidth = 640
        camcorderProfile!!.videoFrameHeight = 480

        parameters.setPreviewSize(cameraBestSize!!.width, cameraBestSize!!.height)
        if (this.cameraID == Camera.CameraInfo.CAMERA_FACING_BACK) {
            parameters.focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE
            parameters.flashMode = cameraFlash
        }
        camera!!.parameters = parameters
    }

    private fun releaseCamera() {
        if (camera != null) {
            PrintLog.d("releaseCamera", "", helperTag)
            camera!!.stopPreview()
            camera!!.setPreviewCallback(null)
            camera!!.release()
            camera = null
        }
    }

    private fun releaseMediaRecorder() {
        if (mediaRecorder != null) {
            mediaRecorder!!.reset()
            mediaRecorder!!.release()
            mediaRecorder = null

            if (camera != null){
                camera!!.lock()
            }
        }
    }

    private fun setCameraDisplayOrientation(cameraID: Int, camera: Camera) {
        val info: Camera.CameraInfo = Camera.CameraInfo()
        Camera.getCameraInfo(cameraID, info)

        val rotation: Int = (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay.rotation

        val degrees = when (rotation) {
            Surface.ROTATION_0 -> 0
            Surface.ROTATION_90 -> 90
            Surface.ROTATION_180 -> 180
            Surface.ROTATION_270 -> 270
            else -> 0
        }

        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            cameraRotate = (info.orientation + degrees) % 360
            cameraRotate = (360 - cameraRotate) % 360
        } else {
            cameraRotate = (info.orientation - degrees + 360) % 360
        }
        camera.setDisplayOrientation(cameraRotate)
    }

    private fun prepareVideoRecorder(): Boolean {
        mediaRecorder = MediaRecorder()

        if (camera != null) {
            // unlock and set camera/max time to media recorder
            camera!!.unlock()
            mediaRecorder!!.setCamera(camera!!)
            mediaRecorder!!.setMaxDuration(recordTimeMax)

            // set sources
            mediaRecorder!!.setVideoSource(MediaRecorder.VideoSource.CAMERA)
            mediaRecorder!!.setAudioSource(MediaRecorder.AudioSource.DEFAULT)

            // set format
//            mediaRecorder!!.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
//            mediaRecorder!!.setVideoEncoder(MediaRecorder.VideoEncoder.MPEG_4_SP)
//            mediaRecorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

            // set a camcorder profile (requires API level 8 or higher
            mediaRecorder!!.setProfile(camcorderProfile)
            val recorderRotate = when(cameraID) {
                Camera.CameraInfo.CAMERA_FACING_BACK -> cameraRotate
                Camera.CameraInfo.CAMERA_FACING_FRONT -> cameraRotate + 180
                else -> cameraRotate
            }
            mediaRecorder!!.setOrientationHint(recorderRotate)

            // set output file
            outputFile = SupportData.createTmpFile(AppConstants.MEDIA_VIDEO)
            if (outputFile == null) {
                PrintLog.d("outputFile", "null", helperTag)
                return false
            }
            mediaRecorder!!.setOutputFile(outputFile!!.path)

            // prepare configured media recorder
            try {
                mediaRecorder!!.prepare()
            } catch (e: IllegalStateException) {
                PrintLog.e("IllegalStateException preparing MediaRecorder", e.message!!, helperTag)
                releaseMediaRecorder()
                return false
            } catch (e: IOException) {
                PrintLog.e("IOException preparing MediaRecorder", e.message!!, helperTag)
                releaseMediaRecorder()
                return false
            }

            return true
        }
        return false
    }

    private fun isFrontCamera(): Boolean {
        return (cameraID == Camera.CameraInfo.CAMERA_FACING_FRONT)
    }

    inner class CameraPrepareTask: AsyncTask<Void, Void, Boolean>() {

        override fun doInBackground(vararg params: Void?): Boolean {
            return try {
                cameraPrepare()
                true
            } catch (e: Exception) {
                PrintLog.e("takePicture", e.message!!, helperTag)
                false
            }
        }

        override fun onPostExecute(result: Boolean?) {
            PrintLog.d("CameraPrepareTask result", result.toString(), helperTag)
            super.onPostExecute(result)

            if (result!!) {
                try {
                    camera!!.setPreviewTexture(surfaceTexture)
                } catch (e: IOException) {
                    PrintLog.e(this.javaClass.name, e.message!!)
                }
                camera!!.startPreview()
                isCameraReloading = false
            }
        }
    }

    private inner class MediaPrepareTask: AsyncTask<Void, Void, Boolean>() {

        override fun doInBackground(vararg params: Void?): Boolean {
            val prepareVideoRecorder = prepareVideoRecorder()
            PrintLog.d("prepareVideoRecorder", prepareVideoRecorder.toString(), helperTag)
            if (prepareVideoRecorder) {
                mediaRecorder!!.start()
                isRecording = true
            } else {
                releaseMediaRecorder()
                return false
            }
            return false
        }

        override fun onPostExecute(result: Boolean?) {
            PrintLog.d("MediaPrepareTask result", result.toString(), helperTag)
            super.onPostExecute(result)
        }
    }
}