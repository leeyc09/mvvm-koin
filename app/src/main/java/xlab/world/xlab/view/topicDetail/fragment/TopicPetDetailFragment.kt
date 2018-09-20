package xlab.world.xlab.view.topicDetail.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import xlab.world.xlab.R

class TopicPetDetailFragment: Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_image_pager, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        onSetup()

        onBindEvent()

        observeViewModel()
    }

    private fun onSetup() {

    }

    private fun onBindEvent() {

    }

    private fun observeViewModel() {

    }

    private fun getBundlePetNo(): Int = arguments?.getInt("petNo") ?: 0

    companion object {
        fun newFragment(petNo: Int): TopicPetDetailFragment {
            val fragment =  TopicPetDetailFragment()

            val args = Bundle()
            args.putInt("petNo", petNo)
            fragment.arguments = args

            return fragment
        }
    }
}