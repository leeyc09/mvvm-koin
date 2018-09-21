package xlab.world.xlab.view.topicEdit

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SimpleItemAnimator
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.action_bar_default.*
import kotlinx.android.synthetic.main.activity_topic_pet_edit.*
import org.koin.android.architecture.ext.viewModel
import org.koin.android.ext.android.inject
import xlab.world.xlab.R
import xlab.world.xlab.adapter.recyclerView.PetHairColorAdapter
import xlab.world.xlab.adapter.recyclerView.PetHairTypeAdapter
import xlab.world.xlab.utils.support.*
import xlab.world.xlab.utils.view.dialog.*
import xlab.world.xlab.utils.view.recyclerView.CustomItemDecoration
import xlab.world.xlab.utils.view.toast.DefaultToast

class TopicPetEditActivity : AppCompatActivity(), View.OnClickListener, View.OnTouchListener {
    private val topicPetEditViewModel: TopicPetEditViewModel by viewModel()
    private val spHelper: SPHelper by inject()
    private val petInfo: PetInfo by inject()
    private val permissionHelper: PermissionHelper by inject()

    private val glideOption = RequestOptions()
            .centerCrop()
            .placeholder(R.drawable.pet_profile_image_default)
            .error(R.drawable.pet_profile_image_default)

    private var petNo: Int? = null

    private lateinit var petHairTypeAdapter: PetHairTypeAdapter
    private lateinit var petHairColorAdapter: PetHairColorAdapter

    private lateinit var defaultToast: DefaultToast
    private lateinit var progressDialog: DefaultProgressDialog
    private lateinit var editCancelDialog: DefaultDialog
    private lateinit var topicDeleteDialog: DefaultOneDialog
    private lateinit var topicColorSelectDialog: TopicColorSelectDialog

    private val topicColorListener = object:TopicColorSelectDialog.Listener {
        override fun onTopicColorSelect(selectColorStr: String) {
            topicPetEditViewModel.enableSaveData(topicColor = selectColorStr)
            topicColorBtn.tag = selectColorStr
            topicColorBtn.setBackgroundColor(Color.parseColor("#$selectColorStr"))
        }
    }
    private val petHairTypeListener = View.OnClickListener { view ->
        topicPetEditViewModel.hairTypeSelect(hairType = view.tag as String)
    }
    private val petHairColorListener = View.OnClickListener { view ->
        val position = view.tag as Int
        topicPetEditViewModel.hairColorSelect(position = position, hairColorData = petHairColorAdapter.getItem(position = position))
    }
    private val topicDeleteListener = object: DefaultOneDialog.Listener {
        override fun onOkayTouch(tag: Any?) {
            topicPetEditViewModel.deletePet(authorization = spHelper.authorization)
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

        topicPetEditViewModel.deleteProfileImage()
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
                        val imageUri = data!!.getStringExtra(IntentPassName.IMAGE_URL)
                        topicPetEditViewModel.setNewProfileImage(petImage = imageUri)
                    }
                    RequestCodeData.TOPIC_BREED -> {
                        val breedIndex = data!!.getStringExtra(IntentPassName.BREED_INDEX)
                        topicPetEditViewModel.setBreedDetailData(breedIndex = breedIndex, petData = null)
                    }
                }
            }
        }
    }

    private fun onSetup() {
        petNo = intent.getStringExtra(IntentPassName.PET_NO)?.toInt()
        topicPetEditViewModel.isAddPet = petNo?.let{_->false}?:let{_->true}

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
        }

        defaultToast = DefaultToast(context = this)
        progressDialog = DefaultProgressDialog(context = this)
        editCancelDialog = DialogCreator.editCancelDialog(context= this)
        topicDeleteDialog = DialogCreator.deletePetDialog(context = this, listener = topicDeleteListener)
        topicColorSelectDialog = DialogCreator.topicColorSelectDialog(selectPosition = 0, listener = topicColorListener)

        // petHairTypeAdapter adapter & recycler 초기화
        petHairTypeAdapter = PetHairTypeAdapter(context = this,
                selectListener = petHairTypeListener)
        hairTypeRecyclerView.adapter = petHairTypeAdapter
        hairTypeRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        hairTypeRecyclerView.addItemDecoration(CustomItemDecoration(context = this, right = 20f))
        (hairTypeRecyclerView.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false

        // petHairColorAdapter adapter & recycler 초기화
        petHairColorAdapter = PetHairColorAdapter(context = this,
                selectListener = petHairColorListener)
        hairColorRecyclerView.adapter = petHairColorAdapter
        hairColorRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        hairColorRecyclerView.addItemDecoration(CustomItemDecoration(context = this, right = 6f))
        (hairColorRecyclerView.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false

        petNo?.let {
            topicPetEditViewModel.loadPetData(userId = spHelper.userId, petNo = it, topicColorList = resources.getStringArray(R.array.topicColorStringList))
            topicPetEditViewModel.enableSaveData()
        } ?: let {
            topicColorBtn.tag = topicColorStrArray[0]
            topicColorBtn.setBackgroundColor(Color.parseColor("#${topicColorStrArray[0]}"))
            topicPetEditViewModel.enableSaveData(topicColor = topicColorStrArray[0])
        }
    }

    private fun onBindEvent() {
        actionBackBtn.setOnClickListener(this) // 뒤로가기
        actionBtn.setOnClickListener(this) // 토픽 (추가, 수정)
        topicColorBtn.setOnClickListener(this) // 토픽 색상 변경
        imageViewPet.setOnClickListener(this) // 토픽 이미지 변경
        textViewDog.setOnClickListener(this) // 강아지 선택
        textViewCat.setOnClickListener(this) // 고양이 선택
        editTextPetName.setOnTouchListener(this) // 이름 지우기
        textViewPetFemale.setOnClickListener(this) // 암컷 선택
        textViewPetMale.setOnClickListener(this) // 수컷 선택
        neuteredBtn.setOnClickListener(this) // 중성화 선택
        breedLayout.setOnClickListener(this) // 품종 선택
        topicDeleteBtn.setOnClickListener(this) // pet 삭제

        // pet 이름 지우기 버튼 활성화
        ViewFunction.onFocusChange(editText = editTextPetName) { hasFocus ->
            editTextPetName.setCompoundDrawablesWithIntrinsicBounds(0, 0,
                    if (hasFocus) R.drawable.textdelete_black else 0, 0)
        }

        ViewFunction.onTextChange(editText = editTextPetName) {
            topicPetEditViewModel.enableSaveData(petName = it)
        }

        // pet 생년월일 입력 이벤트
        ViewFunction.birthWatcher(editText = editTextPetBirth) {
            topicPetEditViewModel.birthRegex(petBirth = it)
        }
        // pet weight 입력 이벤트
        ViewFunction.onTextChange(editText = editTextPetWeight) {
            topicPetEditViewModel.enableSaveData(petWeight = try { it.toFloat()} catch (e: NumberFormatException) {-1f})
        }
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
                uiData.topicColorData?.let {
                    topicColorBtn.tag = it.topicColor
                    topicColorBtn.setBackgroundColor(Color.parseColor("#${it.topicColor}"))
                    topicPetEditViewModel.enableSaveData(topicColor = it.topicColor)
                    topicColorSelectDialog = DialogCreator.topicColorSelectDialog(selectPosition = it.colorIndex, listener = topicColorListener)
                }
                uiData.petImage?.let {
                    Glide.with(this)
                            .load(it)
                            .apply(glideOption)
                            .into(imageViewPet)
                    topicPetEditViewModel.enableSaveData()
                }
                uiData.petType?.let {
                    textViewDog.isSelected = it
                    textViewCat.isSelected = !it
                }
                uiData.petName?.let {
                    editTextPetName.setText(it)
                }
                uiData.petGender?.let {
                    textViewPetFemale.isSelected = it
                    textViewPetMale.isSelected = !it
                }
                uiData.petNeutered?.let {
                    neuteredBtn.isSelected = it
                }
                uiData.breedName?.let {
                    textViewPetBreed.setText(it, TextView.BufferType.SPANNABLE)
                }
                uiData.isBreedSelect?.let {
                    textViewPetBreed.isSelected = it
                }
                uiData.breedDetailVisibility?.let {
                    breedDetailLayout.visibility = it
                }
                uiData.hairTypeData?.let {
                    petHairTypeAdapter.updateData(petHairTypeData = it)
                }
                uiData.hairTypeVisibility?.let {
                    hairTypeLayout.visibility = it
                    petHairTypeAdapter.notifyDataSetChanged()
                }
                uiData.hairTypeUpdateValue?.let {
                    petHairTypeAdapter.changeSelectType(hairType = it)
                    topicPetEditViewModel.enableSaveData()
                }
                uiData.hairColorData?.let {
                    petHairColorAdapter.updateData(petHairColorData = it)
                }
                uiData.hairColorVisibility?.let {
                    hairColorLayout.visibility = it
                    petHairColorAdapter.notifyDataSetChanged()
                }
                uiData.hairColorUpdateIndex?.let {
                    petHairColorAdapter.notifyItemChanged(it)
                    topicPetEditViewModel.enableSaveData()
                }
                uiData.petBirth?.let {
                    editTextPetBirth.setText(it)
                }
                uiData.birthRegexVisibility?.let {
                    layoutConfirmBirth.visibility = it
                    topicPetEditViewModel.enableSaveData()
                }
                uiData.petWeight?.let {
                    editTextPetWeight.setText(it)
                }
                uiData.saveEnable?.let {
                    actionBtn.isEnabled = it
                }
            }
        })

        // enable save pet 이벤트 observe
        topicPetEditViewModel.enableSaveDataEvent.observe(owner = this, observer = android.arch.lifecycle.Observer { enableSaveDataEvent ->
            enableSaveDataEvent?.let { _ ->
                enableSaveDataEvent.status?.let { backPopUpDialog ->
                    actionBackBtn.tag = backPopUpDialog
                }
            }
        })

        // save or delete pet 이벤트 observe
        topicPetEditViewModel.saveDeletePetEvent.observe(owner = this, observer = android.arch.lifecycle.Observer {
            it?.let { _ ->
                it.status?.let { saveOrDelete -> // true - (update, add) false - delete
                    val resultCode =
                            if (saveOrDelete) Activity.RESULT_OK
                            else ResultCodeData.TOPIC_DELETE
                    setResult(resultCode)
                    finish()
                }
            }
        })
    }

    override fun onClick(v: View?) {
        v?.let {
            currentFocus?.clearFocus()
            v.requestFocus()
            ViewFunction.hideKeyboard(context = this, view = v)
            when (v.id) {
                R.id.actionBackBtn -> { // 뒤로가기
                    if (actionBackBtn.tag as Boolean) {
                        editCancelDialog.show()
                        return
                    }
                    setResult(Activity.RESULT_CANCELED)
                    finish()
                }
                R.id.actionBtn -> { // 토픽 (추가, 수정)
                    topicPetEditViewModel.savePet(authorization = spHelper.authorization)
                }
                R.id.topicColorBtn -> { // 토픽 색상 변경
                    topicColorSelectDialog.show(supportFragmentManager, "TopicColorSelectDialog")
                }
                R.id.imageViewPet -> { // 토픽 이미지 변경
                    if (!permissionHelper.hasPermission(context = this, permissions = permissionHelper.galleryPermissions)) {
                        permissionHelper.requestGalleryPermissions(context = this)
                        return
                    }
                    RunActivity.galleryImageSelectActivity(context = this, withOverlay = false)
                }
                R.id.textViewDog, R.id.textViewCat -> { // 강아지, 고양이 선택
                    if (!v.isSelected)
                        topicPetEditViewModel.enableSaveData(petType = v.tag as String)
                }
                R.id.textViewPetFemale, R.id.textViewPetMale -> { // 암컷, 수컷 선택
                    if (!v.isSelected)
                        topicPetEditViewModel.enableSaveData(petGender = v.tag as String)
                }
                R.id.neuteredBtn -> { // 중성화 선택
                    topicPetEditViewModel.enableSaveData(petNeutered = v.isSelected)
                }
                R.id.breedLayout -> { // 품종 선택
                    if (textViewDog.isSelected) RunActivity.petBreedActivity(context= this, petType = petInfo.dogCode)
                    else if (textViewCat.isSelected) RunActivity.petBreedActivity(context = this, petType = petInfo.catCode)
                }
                R.id.topicDeleteBtn -> { // pet 삭제
                    topicDeleteDialog.show()
                }
            }
        }
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        v?.let {
            when (v.id) {
                R.id.editTextPetName -> { // 이름 지우기
                    ViewFunction.onDrawableTouch(v as EditText, event!!) { isTouch ->
                        if (isTouch) {
                            v.setText("")
                            v.performClick()
                        }
                    }
                }
            }
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
