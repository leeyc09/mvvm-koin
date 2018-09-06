package xlab.world.xlab.adapter.viewPager

import android.content.Context
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.view.View


class ViewStatePagerAdapter(private val manager: FragmentManager): FragmentStatePagerAdapter(manager) {
    private val fragmentList = ArrayList<Fragment>() // store fragments
    private val fragmentTitleList = ArrayList<String>() // store title of each fragment

    override fun getItem(position: Int): Fragment {
        return fragmentList[position]
    }

    override fun getCount(): Int {
        return fragmentList.size
    }

    override fun getItemPosition(`object`: Any): Int {
        return PagerAdapter.POSITION_NONE
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return fragmentTitleList[position]
    }

    fun addFragment(fragment: Fragment, title: String) {
        fragmentList.add(fragment) // add fragment
        fragmentTitleList.add(title) // add title of fragment
        notifyDataSetChanged()
    }

    fun addFragmentAtBegin(fragment: Fragment, title: String) {
        fragmentList.add(0, fragment) // add fragment
        fragmentTitleList.add(0, title) // add title of fragment
        notifyDataSetChanged()
    }

    fun removeFragment(position: Int) {
        val fragment = fragmentList[position]
        fragmentList.removeAt(position)
        fragmentTitleList.removeAt(position)
        destroyFragmentView(fragment)
        notifyDataSetChanged()
    }

    private fun destroyFragmentView(fragment: Fragment) {
        val trans = manager.beginTransaction()
        trans.remove(fragment)
        trans.commit()
    }
}