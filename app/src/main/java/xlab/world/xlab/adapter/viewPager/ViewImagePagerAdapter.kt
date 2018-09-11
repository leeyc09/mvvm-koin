package xlab.world.xlab.adapter.viewPager

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.support.v4.content.res.ResourcesCompat
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import xlab.world.xlab.R

class ViewImagePagerAdapter(context: Context,
                            private val imageUrlList: ArrayList<String>) : PagerAdapter() {

    private val imagePlaceHolder = ColorDrawable(ResourcesCompat.getColor(context.resources, R.color.colorE2E2E2, null))
    private val glideOption = RequestOptions()
            .centerCrop()
            .placeholder(imagePlaceHolder)
            .error(imagePlaceHolder)

    override fun getCount(): Int {
        return imageUrlList.size
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val itemView = LayoutInflater.from(container.context).inflate(R.layout.fragment_image_pager, container, false)
        val imageView = itemView.findViewById<ImageView>(R.id.imageView)
        Glide.with(container.context)
                .load(imageUrlList[position])
                .apply(glideOption)
                .into(imageView)

        container.addView(itemView)
        return itemView
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        (container as ViewPager).removeView(`object` as View)
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }
}