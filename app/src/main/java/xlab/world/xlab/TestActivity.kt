package xlab.world.xlab

import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraDevice
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Size
import android.view.Surface
import android.view.TextureView
import kotlinx.android.synthetic.main.activity_test.*
import org.koin.android.ext.android.inject
import xlab.world.xlab.utils.support.AppConstants
import xlab.world.xlab.utils.support.PermissionHelper
import xlab.world.xlab.utils.support.PrintLog

class TestActivity : AppCompatActivity(), CameraAPI.CameraInterface, TextureView.SurfaceTextureListener {

    private lateinit var camera: CameraAPI
    private val permissionHelper: PermissionHelper by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)

        textureView.surfaceTextureListener = this

        camera = CameraAPI(cameraInterface = this)

        if (!permissionHelper.hasPermission(this, permissionHelper.cameraPermissions)) { // check permission granted
            permissionHelper.requestCameraPermissions(this) // request permission
        }
    }

    override
    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            AppConstants.PERMISSION_REQUEST_CAMERA_CODE -> {
                if (permissionHelper.resultRequestPermissions(grantResults)) { // all permission grant
                } else {
                    PrintLog.d("onRequestPermissionsResult", "Permission Denied")
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        if (textureView.isAvailable) {
            openCamera()
        } else {
            textureView.surfaceTextureListener = this
        }
    }

    override fun onPause() {
        closeCamera()
        super.onPause()
    }

    private fun openCamera() {
        val cameraManager = camera.cameraManager(activity = this)
        val cameraId = camera.cameraCharacteristic(cameraManager = cameraManager)
        camera.cameraDevice(cameraManager = cameraManager, cameraId = cameraId)
    }

    private fun closeCamera() {
        camera.closeCamera()
    }

    override fun onCameraDeviceOpened(cameraDevice: CameraDevice, cameraSize: Size) {
        val texture = textureView.surfaceTexture
        texture.setDefaultBufferSize(cameraSize.width, cameraSize.height)
        val surface = Surface(texture)

        camera.captureSession(cameraDevice = cameraDevice, surface = surface)
        camera.captureRequest(cameraDevice = cameraDevice, surface = surface)
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {
        openCamera()
    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
        return true
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {
    }
}
