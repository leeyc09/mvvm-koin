package xlab.world.xlab.view.main.fragment

import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.swipe_recycler_view.*
import xlab.world.xlab.R

class FeedShopFragment: Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.swipe_recycler_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        onSetup(view)

        onBindEvent()

        observeViewModel()
    }

    private fun onSetup(rootView: View) {
        mainLayout.setBackgroundColor(Color.GRAY)
    }

    private fun onBindEvent() {

    }

    private fun observeViewModel() {
    }

    fun scrollToTop() {
        recyclerView.scrollToPosition(0)
    }

    companion object {
        fun newFragment(): FeedShopFragment {
            return FeedShopFragment()
        }
    }
}