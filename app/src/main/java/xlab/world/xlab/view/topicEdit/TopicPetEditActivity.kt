package xlab.world.xlab.view.topicEdit

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.action_bar_default.*
import kotlinx.android.synthetic.main.activity_topic_pet_edit.*
import org.koin.android.architecture.ext.viewModel
import org.koin.android.ext.android.inject
import xlab.world.xlab.R
import xlab.world.xlab.utils.support.*
import xlab.world.xlab.utils.view.dialog.DefaultProgressDialog
import xlab.world.xlab.utils.view.dialog.DialogCreator
import xlab.world.xlab.utils.view.dialog.TopicColorSelectDialog
import xlab.world.xlab.utils.view.toast.DefaultToast

class TopicPetEditActivity : AppCompatActivity(), View.OnClickListener, View.OnTouchListener {
    private val topicPetEditViewModel: TopicPetEditViewModel by viewModel()
    private val spHelper: SPHelper by inject()
    private val letterOrDigitInputFilter: LetterOrDigitInputFilter by inject()
    private val permissionHelper: PermissionHelper by inject()

    private val glideOption = RequestOptions()
            .circleCrop()
            .placeholder(R.drawable.pet_profile_image_default)
            .error(R.drawable.pet_profile_image_default)

    private var petNo: Int? = null

    private lateinit var defaultToast: DefaultToast
    private lateinit var progressDialog: DefaultProgressDialog
    private lateinit var topicColorSelectDialog: TopicColorSelectDialog

    private val topicColorListener = object:TopicColorSelectDialog.Listener {
        override fun onTopicColorSelect(selectColorStr: String) {
            topicColorBtn.tag = selectColorStr
            topicColorBtn.setBackgroundColor(Color.parseColor("#$selectColorStr"))
//            actionBtn.enableSave()
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_topic_pet_edit)

        onSetup()

        onBindEvent()

        observeViewModel()
    }

    override fun onDestroy() {
        super.onDestroy()

//        profileEditViewModel.deleteProfileImage()
    }

    override fun onBackPressed() {
        actionBackBtn.performClick()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            AppConstants.PERMISSION_REQUEST_GALLERY_CODE -> {
                if (permissionHelper.resultRequestPermissions(results = grantResults))
                    imageViewPet.performClick()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        PrintLog.d("resultCode", resultCode.toString(), this::class.java.name)
        PrintLog.d("requestCode", requestCode.toString(), this::class.java.name)

        when (resultCode) {
            Activity.RESULT_OK -> {
                when (requestCode) {
                    RequestCodeData.GALLARY_IMAGE_SELECT -> { // pet 이미지 수정
                        // profile image change success
//                        val imageUri = data!!.getStringExtra("imageUri")
//                        tmpProfileImageList.add(imageUri)
//
//                        Glide.with(this)
//                                .load(imageUri)
//                                .apply(glideOption)
//                                .into(imageViewPet)
//
//                        actionBtn.enableSave()
                    }
                    RequestCodeData.TOPIC_BREED -> {
//                        val breedIndex = data!!.getStringExtra("breedIndex")
//                        breedDetailSetup(breedIndex, null)
                    }
                }
            }
        }
    }

    private fun onSetup() {
        petNo = intent.getStringExtra(IntentPassName.PET_NO)?.toInt()

        val topicColorStrArray = resources.getStringArray(R.array.topicColorStringList)
        petNo?.let { // pet 수정
            actionBarTitle.setText(getString(R.string.topic_pet_edit), TextView.BufferType.SPANNABLE)
//            breedDetailVisibility = View.VISIBLE
            breedDetailLayout.visibility = View.VISIBLE
            topicDeleteBtn.visibility = View.VISIBLE
        } ?:let { // pet 추가
            actionBarTitle.setText(getString(R.string.topic_pet_add), TextView.BufferType.SPANNABLE)
//            breedDetailVisibility = View.GONE
            breedDetailLayout.visibility = View.GONE
            topicDeleteBtn.visibility = View.GONE
            topicColorBtn.tag = topicColorStrArray[0]
            topicColorBtn.setBackgroundColor(Color.parseColor("#${topicColorStrArray[0]}"))
        }


        defaultToast = DefaultToast(context = this)
        progressDialog = DefaultProgressDialog(context = this)
        topicColorSelectDialog = DialogCreator.topicColorSelectDialog(selectPosition = 0, listener = topicColorListener)

        topicPetEditViewModel.existChangedData(topicColor = topicColorBtn.tag as String)
    }

    private fun onBindEvent() {
        actionBackBtn.setOnClickListener(this) // 뒤로가기
        actionBtn.setOnClickListener(this) // 토픽 (추가, 수정)
        topicColorBtn.setOnClickListener(this) // 토픽 색상 변경
    }

    private fun observeViewModel() {
        // UI 이벤트 observe
        topicPetEditViewModel.uiData.observe(this, android.arch.lifecycle.Observer { uiData ->
            uiData?.let { _ ->
                uiData.isLoading?.let {
                    if (it && !progressDialog.isShowing)
                        progressDialog.show()
                    else if (!it && progressDialog.isShowing)
                        progressDialog.dismiss()
                }
                uiData.toastMessage?.let {
                    defaultToast.showToast(message = it)
                }
                uiData.saveEnable?.let {
                    actionBtn.isEnabled = it
                }
            }
        })
    }

    override fun onClick(v: View?) {
        v?.let {
            when (v.id) {
                R.id.actionBackBtn -> { // 뒤로가기
//                    if (actionBtn.isEnabled) {
//                        editCancelDialog.show()
//                        return
//                    }
                    setResult(Activity.RESULT_CANCELED)
                    finish()
                }
                R.id.actionBtn -> { // 토픽 (추가, 수정)

                }
                R.id.topicColorBtn -> { // 토픽 색상 변경
                    topicColorSelectDialog.show(supportFragmentManager, "TopicColorSelectDialog")
                }
            }
        }
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        v?.let {
//            when (v.id) {
//                v.performClick()
//            }
            v.performClick()
        }
        return false
    }

    companion object {
        fun newIntent(context: Context, petNo: Int?): Intent {
            val intent = Intent(context, TopicPetEditActivity::class.java)
            intent.putExtra(IntentPassName.PET_NO, petNo?.toString())

            return intent
        }
    }
}
