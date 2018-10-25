package xlab.world.xlab.adapter.recyclerView

import android.app.Activity
import android.content.Context
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.RecyclerView
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import org.koin.android.ext.android.inject
import xlab.world.xlab.R
import xlab.world.xlab.data.adapter.*
import xlab.world.xlab.utils.support.SPHelper

class UserRecommendAdapter(private val context: Context,
                           private val followListener: View.OnClickListener,
                           private val profileListener: View.OnClickListener) : RecyclerView.Adapter<UserRecommendAdapter.ViewHolder>() {

    private var userRecommendData: UserRecommendData = UserRecommendData()
    var dataLoading: Boolean
        get() = this.userRecommendData.isLoading
        set(value) { this.userRecommendData.isLoading = value }
    var dataTotal: Int = -1
        get() = this.userRecommendData.total
    var dataNextPage: Int = 1
        get() = this.userRecommendData.nextPage

    private val glideOption = RequestOptions()
            .circleCrop()
            .placeholder(R.drawable.profile_img_44)
            .error(R.drawable.profile_img_44)

    fun linkData(userRecommendData: UserRecommendData) {
        this.userRecommendData = userRecommendData
        notifyDataSetChanged()
    }

    override
    fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolderBind(LayoutInflater.from(parent.context).
                    inflate(R.layout.item_user_recommend, parent, false))
    }

    override
    fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.display(item = userRecommendData.items[position], position = position)
    }

    override
    fun getItemCount(): Int {
        return userRecommendData.items.size
    }

    // View Holder
    inner class ViewHolderBind(view: View) : ViewHolder(view) {
        private val mainLayout: FrameLayout = view.findViewById(R.id.mainLayout)
        private val imageViewProfile: ImageView = view.findViewById(R.id.imageViewProfile)
        private val textViewNick: TextView = view.findViewById(R.id.textViewNick)
        private val followBtn: ImageView = view.findViewById(R.id.followBtn)

        override fun display(item: UserRecommendListData, position: Int) {
            val offSetDIP =
                    if (position == 0) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 28f, context.resources.displayMetrics).toInt()
                    else TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0f, context.resources.displayMetrics).toInt()
            (mainLayout.layoutParams as RecyclerView.LayoutParams).setMargins(offSetDIP, 0, 0, 0)

            // user profile 이미지
            Glide.with(context)
                    .load(item.profileImage)
                    .apply(glideOption)
                    .into(imageViewProfile)

            // 닉네임
            textViewNick.setText(item.nickName, TextView.BufferType.SPANNABLE)

            // follow 상태
            followBtn.isSelected = item.isFollowing

            // profile touch 이벤트
            mainLayout.tag = item.userId
            mainLayout.setOnClickListener(profileListener)

            // follow touch 이벤트
            followBtn.tag = position
            followBtn.setOnClickListener(followListener)
        }
    }

    abstract class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        abstract fun display(item: UserRecommendListData, position: Int)
    }
}
