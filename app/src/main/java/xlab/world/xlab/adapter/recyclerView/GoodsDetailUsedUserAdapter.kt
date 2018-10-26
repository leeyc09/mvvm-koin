package xlab.world.xlab.adapter.recyclerView

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.widget.RecyclerView
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import xlab.world.xlab.R
import xlab.world.xlab.data.adapter.*
import xlab.world.xlab.utils.support.PrintLog

class GoodsDetailUsedUserAdapter(private val context: Context,
                                 private val profileListener: View.OnClickListener) : RecyclerView.Adapter<GoodsDetailUsedUserAdapter.ViewHolder>() {

    private var goodsDetailUsedUserData: GoodsDetailUsedUserData = GoodsDetailUsedUserData()
    var dataLoading: Boolean
        get() = this.goodsDetailUsedUserData.isLoading
        set(value) { this.goodsDetailUsedUserData.isLoading = value }
    var dataTotal: Int = -1
        get() = this.goodsDetailUsedUserData.total
    var dataNextPage: Int = 1
        get() = this.goodsDetailUsedUserData.nextPage

    private val glideOption = RequestOptions()
            .circleCrop()
            .placeholder(R.drawable.profile_img_44)
            .error(R.drawable.profile_img_44)

    fun linkData(goodsDetailUsedUserData: GoodsDetailUsedUserData) {
        this.goodsDetailUsedUserData = goodsDetailUsedUserData
        PrintLog.d("goodsDetailUsedUserData", this.goodsDetailUsedUserData.toString(), "")
        notifyDataSetChanged()
    }

    override
    fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolderBind(LayoutInflater.from(parent.context).
                    inflate(R.layout.item_goods_detail_used_user, parent, false))
    }

    override
    fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.display(item = goodsDetailUsedUserData.items[position], position = position)
    }

    override
    fun getItemCount(): Int {
        return goodsDetailUsedUserData.items.size
    }

    // View Holder
    inner class ViewHolderBind(view: View) : ViewHolder(view) {
        private val mainLayout: LinearLayout = view.findViewById(R.id.mainLayout)
        private val imageViewProfileImage: ImageView = view.findViewById(R.id.imageViewProfileImage)
        private val textViewNickName: TextView = view.findViewById(R.id.textViewNickName)

        override fun display(item: GoodsDetailUsedUserListData, position: Int) {
            val offSetDIP =
                    if (position == 0) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20f, context.resources.displayMetrics).toInt()
                    else TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0f, context.resources.displayMetrics).toInt()
            (mainLayout.layoutParams as RecyclerView.LayoutParams).setMargins(offSetDIP, 0, 0, 0)

            // 프로필 이미지
            Glide.with(context)
                    .load(item.userProfile)
                    .apply(glideOption)
                    .into(imageViewProfileImage)
            // 닉네임
            textViewNickName.setText(item.userNickname, TextView.BufferType.SPANNABLE)

            mainLayout.tag = item.userId
            mainLayout.setOnClickListener(profileListener)
        }
    }

    abstract class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        abstract fun display(item: GoodsDetailUsedUserListData, position: Int)
    }
}
