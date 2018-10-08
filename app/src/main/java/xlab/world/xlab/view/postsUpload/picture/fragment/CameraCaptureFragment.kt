package xlab.world.xlab.view.postsUpload.picture.fragment

import android.app.Activity
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.action_bar_post_upload.*
import kotlinx.android.synthetic.main.fragment_camera_capture.*
import xlab.world.xlab.R
import xlab.world.xlab.utils.support.IntentPassName
import xlab.world.xlab.utils.support.PrintLog
import xlab.world.xlab.utils.support.RunActivity
import xlab.world.xlab.utils.view.dialog.DefaultProgressDialog
import xlab.world.xlab.view.postsUpload.picture.PostUploadPictureActivity

class CameraCaptureFragment: Fragment(), View.OnClickListener {
    private lateinit var activity: PostUploadPictureActivity

    private var progressDialog: DefaultProgressDialog? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_camera_capture, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        onSetup()

        onBindEvent()

        observeViewModel()
    }

    private fun onSetup() {
        libraryBtn.isEnabled = true
        cameraBtn.isEnabled = false

        actionNextBtn.visibility = View.GONE

        activity = context as PostUploadPictureActivity

        progressDialog = progressDialog ?: DefaultProgressDialog(context = context!!)
    }

    private fun onBindEvent() {
        actionCloseBtn.setOnClickListener(this) // 닫기 버튼
        libraryBtn.setOnClickListener(this) // 라이브러리 버튼

        flashBtn.setOnClickListener(this) // 플래시 버튼
        captureBtn.setOnClickListener(this) // 촬영 버튼
        switchBtn.setOnClickListener(this) // 카메라 전환 버튼
    }

    private fun observeViewModel() {
    }

    override fun onClick(v: View?) {
        v?.let {
            when (v.id) {
                R.id.actionCloseBtn -> { // 닫기버튼
                    (context as Activity).setResult(Activity.RESULT_CANCELED)
                    (context as Activity).finish()
                }
                R.id.libraryBtn -> { // 라이브러리 버튼
                    activity.switchFragment(fragment = this)
                }
                R.id.flashBtn -> { // 플래시 버튼
                    activity.cameraHelper.changeFlashMode { drawable ->
                        drawable?.let { _ ->
                            flashBtn.setImageDrawable(drawable)
                        }
                    }
                }
                R.id.captureBtn -> { // 촬영 버튼
                    progressDialog!!.show()
                    activity.cameraHelper.takePicture { picturePath ->
                        progressDialog!!.dismiss()
                        if (picturePath.isNotEmpty()) {
                            RunActivity.postUploadFilterActivity(context = activity, imagePathList = arrayListOf(picturePath),
                                    youTubeVideoId = activity.intent.getStringExtra(IntentPassName.YOUTUBE_VIDEO_ID))
                        }
                    }
                }
                R.id.switchBtn -> { // 카메라 전환 버튼
                    activity.cameraHelper.changeCameraID { flashEnable ->
                        flashBtn.isEnabled = flashEnable
                    }
                }
            }
        }
    }

    companion object {
        fun newFragment(): CameraCaptureFragment {
            return CameraCaptureFragment()
        }
    }
}