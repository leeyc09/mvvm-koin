package xlab.world.xlab.view.topicDetail

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.action_bar_topic.*
import org.koin.android.ext.android.inject
import xlab.world.xlab.R
import xlab.world.xlab.adapter.viewPager.ViewStatePagerAdapter
import xlab.world.xlab.utils.listener.DefaultListener
import xlab.world.xlab.utils.support.*

class TopicPetDetailActivity : AppCompatActivity(), View.OnClickListener {
    private val spHelper: SPHelper by inject()

    private var userId: String = ""

    private var resultCode = Activity.RESULT_CANCELED

    private lateinit var viewPagerAdapter: ViewStatePagerAdapter

    private lateinit var defaultListener: DefaultListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_topic_pet_detail)

        onSetup()

        onBindEvent()

        observeViewModel()
    }

    override fun onBackPressed() {
        actionBackBtn.performClick()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        PrintLog.d("resultCode", resultCode.toString(), this::class.java.name)
        PrintLog.d("requestCode", requestCode.toString(), this::class.java.name)

        when (resultCode) {
            Activity.RESULT_OK -> {
                if (this.resultCode == Activity.RESULT_CANCELED ||
                        this.resultCode == ResultCodeData.TOPIC_DELETE)
                    this.resultCode = Activity.RESULT_OK
                when (requestCode) {
                    RequestCodeData.TOPIC_ADD -> {
//                        textViewTopicNum.visibility = View.VISIBLE
//                        imageViewSlash.visibility = View.VISIBLE
//                        textViewTopicTotalNum.visibility = View.VISIBLE
//
//                        totalTopicNum++
//                        topicNum = 1
//
//                        (0 until viewPagerAdapter.count).forEach { index ->
//                            val frag = viewPagerAdapter.getItem(index) as TopicPetFragment
//                            frag.petData = null
//                        }
//                        val frag = TopicPetFragment()
//                        frag.pageNum = totalTopicNum
//                        frag.petData = null
//                        viewPagerAdapter.addFragment(frag, "")
//
//                        viewPagerImage.setCurrentItem(topicNum - 1, false)
//
//                        loadPetData(topicNum) { petData ->
//                            setPetInfoData(petData)
//                        }
//
//                        textViewTopicNum.text = topicNum.toString()
//                        textViewTopicTotalNum.text = totalTopicNum.toString()
                    } // update pet list
                    RequestCodeData.TOPIC_EDIT -> {
//                        val frag = viewPagerAdapter.getItem(topicNum - 1) as TopicPetFragment
//                        frag.petData = null
//                        viewPagerAdapter.notifyDataSetChanged()
//                        loadPetData(topicNum) { petData ->
//                            setPetInfoData(petData)
//                        }
                    } //update pet
                    RequestCodeData.GOODS_DETAIL -> { // update pet used item list
//                        loadPetUsedItemData(1, currentPetId, { petUsedItems ->
//                            petItemAdapter.updateData(petUsedItems)
//                        }, { isEnd ->
//                            isLoadingPetUsedItem = !isEnd
//                        })
                    }
                }
            }
            ResultCodeData.TOPIC_DELETE -> {
//                if (this.resultCode == Activity.RESULT_CANCELED)
//                    this.resultCode = ResultCodeData.TOPIC_DELETE
//                when (requestCode) {
//                    RequestCodeData.TOPIC_EDIT -> {
//                        XlabLog.d("delete topic", "delete topic")
//                        totalTopicNum--
//                        if (totalTopicNum > 0) {
//                            if (totalTopicNum == 1) {
//                                textViewTopicNum.visibility = View.GONE
//                                imageViewSlash.visibility = View.GONE
//                                textViewTopicTotalNum.visibility = View.GONE
//                            }
//
//                            (0 until viewPagerAdapter.count).forEach { index ->
//                                val frag = viewPagerAdapter.getItem(index) as TopicPetFragment
//                                frag.petData = null
//                            }
//                            viewPagerAdapter.removeFragment(viewPagerAdapter.count - 1)
//                            topicNum = 1
//
//                            viewPagerImage.setCurrentItem(topicNum - 1, false)
//
//                            loadPetData(topicNum) { petData ->
//                                setPetInfoData(petData)
//                            }
//
//                            textViewTopicNum.text = topicNum.toString()
//                            textViewTopicTotalNum.text = totalTopicNum.toString()
//                        } else {
//                            setResult(ResultCodeData.TOPIC_DELETE)
//                            finish()
//                        }
//                    }
//                }
            }
            ResultCodeData.LOGOUT_SUCCESS -> {
                setResult(ResultCodeData.LOGOUT_SUCCESS)
                finish()
            }
            ResultCodeData.LOGIN_SUCCESS -> {
                this.resultCode = ResultCodeData.LOGIN_SUCCESS
//                if (userId == SPHelper(this).userId) {
//                    actionAddBtn.visibility = View.VISIBLE
//                    petEditBtn.visibility = View.VISIBLE
//                } else {
//                    actionAddBtn.visibility = View.GONE
//                    petEditBtn.visibility = View.GONE
//                }
//
//                loadPetData(topicNum) { petData ->
//                    setPetInfoData(petData)
//                }
            }
        }
    }

    private fun onSetup() {
        userId = intent.getStringExtra(IntentPassName.USER_ID)
    }

    private fun onBindEvent() {
        actionBackBtn.setOnClickListener(this) // 뒤로가기
    }

    private fun observeViewModel() {

    }

    override fun onClick(v: View?) {
        v?.let {
            when (v.id) {
                R.id.actionBackBtn -> { // 뒤로가기
                    setResult(resultCode)
                    finish()
                }
            }
        }
    }

    companion object {
        fun newIntent(context: Context, userId: String, petNo: Int, petTotal: Int): Intent {
            val intent = Intent(context, TopicPetDetailActivity::class.java)
            intent.putExtra(IntentPassName.USER_ID, userId)
            intent.putExtra(IntentPassName.PET_NO, petNo)
            intent.putExtra(IntentPassName.PET_TOTAL, petTotal)

            return intent
        }
    }
}
