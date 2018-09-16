package xlab.world.xlab.view.follow

import android.app.Activity
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.action_bar_title_with_number.*
import xlab.world.xlab.R

class FollowerActivity : AppCompatActivity(), View.OnClickListener {

    private var resultCode = Activity.RESULT_CANCELED

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_follower)

        onSetup()

        onBindEvent()

        observeViewModel()
    }

    override fun onBackPressed() {
        actionBackBtn.performClick()
    }

    private fun onSetup() {

    }

    private fun onBindEvent() {
        actionBackBtn.setOnClickListener(this) // 뒤로가기
    }

    private fun observeViewModel() {

    }

    override fun onClick(v: View?) {
        v?.let {
            when (v.id) {
                R.id.actionBackBtn -> { //뒤로가기
//                    when (resultCode) {
//                        ResultCodeData.LOGIN_SUCCESS,
//                        Activity.RESULT_OK-> {
//                            setResult(resultCode)
//                        }
//                        else -> {
//                            if (postDetailListener.isChange)
//                                setResult(Activity.RESULT_OK)
//                            else
//                                setResult(Activity.RESULT_CANCELED)
//                        }
//                    }
                    setResult(resultCode)
                    finish()
                }
            }
        }
    }
}
