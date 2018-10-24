package xlab.world.xlab.view.petBreed

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.TextView
import kotlinx.android.synthetic.main.action_bar_default.*
import kotlinx.android.synthetic.main.activity_pet_breed.*
import xlab.world.xlab.R
import xlab.world.xlab.adapter.recyclerView.PetBreedAdapter
import xlab.world.xlab.utils.support.IntentPassName
import xlab.world.xlab.utils.support.ViewFunction
import xlab.world.xlab.utils.view.recyclerView.CustomItemDecoration

class PetBreedActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var petBreedAdapter: PetBreedAdapter

    private val breedSelectListener = View.OnClickListener { view ->
        intent.putExtra(IntentPassName.BREED_INDEX, view.tag as String)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pet_breed)

        onSetup()

        onBindEvent()

        observeViewModel()
    }

    override fun onBackPressed() {
        actionBackBtn.performClick()
    }

    private fun onSetup() {
        // 타이틀 설정, 확인 버튼 비활성화
        actionBarTitle.setText(getString(R.string.pet_breed_select), TextView.BufferType.SPANNABLE)
        actionBtn.visibility = View.GONE

        // pet breed adapter & recycler 초기화
        petBreedAdapter = PetBreedAdapter(context = this,
                petType = intent.getStringExtra(IntentPassName.PET_TYPE),
                selectListener = breedSelectListener)
        recyclerView.adapter = petBreedAdapter
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recyclerView.addItemDecoration(CustomItemDecoration(context = this, bottom = 8f))
    }

    private fun onBindEvent() {
        actionBackBtn.setOnClickListener(this) // 뒤로가기
        imageViewDeleteText.setOnClickListener(this) // 검색 지우기

        ViewFunction.onTextChange(editText = editTextPetBreed) {
            petBreedAdapter.searchData(it.toUpperCase())
            imageViewDeleteText.visibility =
                    if (it.isEmpty()) View.INVISIBLE
                    else View.GONE
        }
    }

    private fun observeViewModel() {

    }

    override fun onClick(v: View?) {
        v?.let {
            when (v.id) {
                R.id.actionBackBtn -> { // 뒤로가기
                    setResult(Activity.RESULT_CANCELED)
                    finish()
                }
                R.id.imageViewDeleteText -> { // 검색 지우기
                    editTextPetBreed.setText("")
                }
            }
        }
    }

    companion object {
        fun newIntent(context: Context, petType: String): Intent {
            val intent = Intent(context, PetBreedActivity::class.java)
            intent.putExtra(IntentPassName.PET_TYPE, petType)

            return intent
        }
    }
}
