package xlab.world.xlab.view.profile.fragment

import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_profile_album.*
import xlab.world.xlab.R

class ProfileAlbumFragment: Fragment(), View.OnClickListener {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_profile_album, container, false)
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

    override fun onClick(v: View?) {
        v?.let {
            when (v.id) {

            }
        }
    }

    companion object {
        fun newFragment(): ProfileAlbumFragment {
            return ProfileAlbumFragment()
        }
    }
}