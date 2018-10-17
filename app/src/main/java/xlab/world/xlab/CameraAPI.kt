package xlab.world.xlab

import android.app.Activity
import android.content.Context
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.util.Size
import android.view.Surface
import xlab.world.xlab.utils.support.PrintLog
import java.util.*

class CameraAPI(private val cameraInterface: CameraInterface) {
    interface CameraInterface {
        fun onCameraDeviceOpened(cameraDevice: CameraDevice, cameraSize: Size)
    }

    private val tag = "CameraAPI"

    private lateinit var cameraSize: Size

    private var captureSession: CameraCaptureSession? = null
    private var cameraDevice: CameraDevice? = null
    private lateinit var previewRequestBuilder: CaptureRequest.Builder

    fun cameraManager(activity: Activity): CameraManager {
        return activity.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    }

    fun cameraCharacteristic(cameraManager: CameraManager): String? {
        try {
            cameraManager.cameraIdList.forEach { cameraId ->
                val characteristic = cameraManager.getCameraCharacteristics(cameraId)
                if (characteristic[CameraCharacteristics.LENS_FACING] == CameraCharacteristics.LENS_FACING_BACK) {
                    val map = characteristic[CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP]
                    map?.let {
                        val sizes = map.getOutputSizes(SurfaceTexture::class.java)
                        cameraSize = sizes[0]

                        sizes.forEach { size ->
                            if (size.width > cameraSize.width) {
                                cameraSize = size
                            }
                        }
                    }
                    return cameraId
                }
            }
        } catch (e: CameraAccessException) {
            PrintLog.e("cameraCharacteristic", e.message!!)
        }

        return null
    }

    fun cameraDevice(cameraManager: CameraManager, cameraId: String?) {
        try {
            cameraId?.let {
                cameraManager.openCamera(cameraId, cameraDeviceStatesCallback, null)
            }
        } catch (e: CameraAccessException) {
            PrintLog.e("cameraDevice", e.message!!)
        }
    }

    fun captureSession(cameraDevice: CameraDevice, surface: Surface) {
        try {
            cameraDevice.createCaptureSession(Collections.singletonList(surface), captureSessionCallback, null)
        } catch (e: CameraAccessException) {
            PrintLog.e("cameraDevice", e.message!!)
        }
    }

    fun captureRequest(cameraDevice: CameraDevice, surface: Surface) {
        try {
            previewRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            previewRequestBuilder.addTarget(surface)
        } catch (e: CameraAccessException) {
            PrintLog.e("cameraDevice", e.message!!)
        }
    }

    fun closeCamera() {
        captureSession?.let {
            captureSession?.close()
            captureSession = null
        }

        cameraDevice?.let {
            cameraDevice?.close()
            cameraDevice = null
        }
    }

    private val cameraDeviceStatesCallback = object: CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice?) {
            camera?.let {
                cameraDevice = camera
                cameraInterface.onCameraDeviceOpened(camera, cameraSize)
            }
        }
        override fun onDisconnected(camera: CameraDevice?) {
            camera?.close()
        }
        override fun onError(camera: CameraDevice?, error: Int) {
            camera?.close()
        }
    }

    private val captureSessionCallback = object: CameraCaptureSession.StateCallback() {
        override fun onConfigured(session: CameraCaptureSession?) {
            try {
                session?.let {
                    captureSession = session
                    previewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)
                    session.setRepeatingRequest(previewRequestBuilder.build(), captureCallback, null)
                }
            } catch (e: CameraAccessException) {
                PrintLog.e("captureSessionCallback onConfigured", e.message!!)
            }
        }
        override fun onConfigureFailed(session: CameraCaptureSession?) {
        }
    }

    private val captureCallback = object: CameraCaptureSession.CaptureCallback() {
    }
}