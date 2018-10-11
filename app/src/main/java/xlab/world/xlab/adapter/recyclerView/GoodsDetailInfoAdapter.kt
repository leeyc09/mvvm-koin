package xlab.world.xlab.adapter.recyclerView

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.support.constraint.ConstraintLayout
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.widget.RecyclerView
import android.text.Html
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import xlab.world.xlab.R
import xlab.world.xlab.data.adapter.*
import xlab.world.xlab.utils.support.AppConstants
import xlab.world.xlab.utils.support.PrintLog
import xlab.world.xlab.utils.view.textView.TextViewRegularNoto
import xlab.world.xlab.utils.view.webView.DefaultWebViewClient
import java.io.Serializable

class GoodsDetailInfoAdapter(private val context: Context,
                             private val moreInfoListener: View.OnClickListener,
                             private val necessaryInfoListener: View.OnClickListener,
                             private val deliveryListener: View.OnClickListener,
                             private val inquiryListener: View.OnClickListener) : RecyclerView.Adapter<GoodsDetailInfoAdapter.ViewHolder>() {

    private val goodsDetailInfoData: GoodsDetailInfoData = GoodsDetailInfoData()

    private val defaultHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 400f, context.resources.displayMetrics).toInt()

    fun getItem(position: Int): GoodsDetailInfoListData {
        return goodsDetailInfoData.items[position]
    }

    fun updateData(goodsDetailInfoData: GoodsDetailInfoData) {
        this.goodsDetailInfoData.items.clear()
        this.goodsDetailInfoData.items.addAll(goodsDetailInfoData.items)

        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return goodsDetailInfoData.items[position].dataType
    }

    override
    fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when (viewType) {
            AppConstants.ADAPTER_HEADER -> HeaderViewHolderBind(LayoutInflater.from(parent.context).
                    inflate(R.layout.item_goods_detail_info_header, parent, false))
            AppConstants.ADAPTER_CONTENT -> ContentViewHolderBind(LayoutInflater.from(parent.context).
                    inflate(R.layout.item_goods_detail_info_content, parent, false))
            AppConstants.ADAPTER_FOOTER -> FooterViewHolderBind(LayoutInflater.from(parent.context).
                    inflate(R.layout.item_goods_detail_info_footer, parent, false))
            else -> ContentViewHolderBind(LayoutInflater.from(parent.context).
                    inflate(R.layout.item_goods_detail_info_content, parent, false))
        }
    }

    override
    fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.display(item = goodsDetailInfoData.items[position], position = position)
    }

    override
    fun getItemCount(): Int {
        return goodsDetailInfoData.items.size
    }

    // header view holder
    inner class HeaderViewHolderBind(view: View) : ViewHolder(view) {
        private val mainLayout: ConstraintLayout = view.findViewById(R.id.mainLayout)
        private val textViewTitle: TextView = view.findViewById(R.id.textViewTitle)
        private val textViewSubTitle: TextView = view.findViewById(R.id.textViewSubTitle)

        override fun display(item: GoodsDetailInfoListData, position: Int) {
            val offSetDIP =
                    if (position == 0) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 31f, context.resources.displayMetrics).toInt()
                    else TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0f, context.resources.displayMetrics).toInt()

            (mainLayout.layoutParams as RecyclerView.LayoutParams).setMargins(0, offSetDIP, 0, 0)
            textViewTitle.setText(item.headerTitle, TextView.BufferType.SPANNABLE)
            textViewSubTitle.setText(item.headerSubTitle, TextView.BufferType.SPANNABLE)
        }
    }

    // content view holder
    inner class ContentViewHolderBind(view: View) : ViewHolder(view) {
        private val mainLayout: LinearLayout = view.findViewById(R.id.mainLayout)
        private val goodsDescriptionLayout: LinearLayout = view.findViewById(R.id.goodsDescriptionLayout)
        private val detailMoreLayout: LinearLayout = view.findViewById(R.id.detailMoreLayout)
        private val imageViewMoreArrow: ImageView = view.findViewById(R.id.imageViewMoreArrow)

        override fun display(item: GoodsDetailInfoListData, position: Int) {
            val offSetDIP = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0f, context.resources.displayMetrics).toInt()
            (mainLayout.layoutParams as RecyclerView.LayoutParams).setMargins(0, offSetDIP, 0, 0)

            // 상품 상세 설명 추가
            if (goodsDescriptionLayout.childCount == 0) {
                if (item.detailUrl.isNotEmpty()) { // 상품 상세 설명이 이미지 file
                    addGoodsDetailImage(item.detailUrl, goodsDescriptionLayout) { measureHeight ->
                        setGoodsDescriptionHeight(measureHeight = measureHeight, isShowAllDescription = item.isShowAllDescription)
                    }
                } else { // 상품 상세 설명이 text file
                    addGoodsDetailText(item.detailText, goodsDescriptionLayout) { measureHeight ->
                        setGoodsDescriptionHeight(measureHeight = measureHeight, isShowAllDescription = item.isShowAllDescription)
                    }
                }
            }

            detailMoreLayout.tag = position
            detailMoreLayout.setOnClickListener(moreInfoListener)
        }

        private fun addGoodsDetailImage(detailUrl: ArrayList<String>, viewGroup: LinearLayout, viewHeight: (Int) -> Unit) {
            val webView = WebView(context)
            webView.overScrollMode = RecyclerView.OVER_SCROLL_NEVER
            webView.settings.loadWithOverviewMode = true
            webView.settings.useWideViewPort = true

            var webViewUrl = "<html><head><style>body{margin:0;padding:0;}</style></head><body>"
            detailUrl.forEach { imageUrl ->
                webViewUrl += "<img width=\"100%\" src=\"$imageUrl\"><br>"
            }
            webViewUrl += "</body></html>"
            val webViewClientListener = object: DefaultWebViewClient.Listener {
                override fun onPageStarted(url: String?) {
                }
                override fun shouldOverrideUrlLoading(url: String?): Boolean {
                    return true
                }
                override fun onPageFinished(url: String?) {
                    webView.measure(0, 0)
                    val height = webView.measuredHeight
                    val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, height)
                    webView.layoutParams = params
                    viewHeight(height)
                }
                override fun onWebViewFinished(url: String?) {
                }
            }
            webView.loadUrl(webViewUrl)
            webView.loadDataWithBaseURL(null, webViewUrl, "html/css", "utf-8", null)
            webView.webViewClient = DefaultWebViewClient(webViewClientListener, true, null)
            viewGroup.addView(webView)
        }

        private fun addGoodsDetailText(detailText: String, viewGroup: LinearLayout, viewHeight: (Int) -> Unit) {
            val textView = TextViewRegularNoto(context)
            textView.setText(Html.fromHtml(detailText), TextView.BufferType.SPANNABLE)
            textView.setTextColor(ResourcesCompat.getColor(context.resources, R.color.color000000, null))

            textView.measure(0, 0)
            val height = textView.measuredHeight
            val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, height)
            val offSetDIP = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20f, context.resources.displayMetrics).toInt()
            params.setMargins(offSetDIP, 0, offSetDIP, 0)
            textView.layoutParams = params
            viewHeight(height)

            // add child
            viewGroup.addView(textView)
        }

        // 상품 상세 설명이 기본 height 보다 큰 경우 -> 일부분만 미리보기
        private fun setGoodsDescriptionHeight(measureHeight: Int, isShowAllDescription: Boolean) {
            if (measureHeight > defaultHeight) {
                detailMoreLayout.visibility = View.VISIBLE
                if (isShowAllDescription) {
                    goodsDescriptionLayout.layoutParams.height = measureHeight
                    imageViewMoreArrow.rotation = 270f
                } else {
                    goodsDescriptionLayout.layoutParams.height = defaultHeight
                    imageViewMoreArrow.rotation = 90f
                }
            } else {
                detailMoreLayout.visibility = View.GONE
            }
        }
    }

    // footer view holder
    inner class FooterViewHolderBind(view: View) : ViewHolder(view) {
        private val mainLayout: ConstraintLayout = view.findViewById(R.id.mainLayout)
        private val textViewTitle: TextView = view.findViewById(R.id.textViewTitle)

        override fun display(item: GoodsDetailInfoListData, position: Int) {
            val offSetDIP = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0f, context.resources.displayMetrics).toInt()
            (mainLayout.layoutParams as RecyclerView.LayoutParams).setMargins(0, offSetDIP, 0, 0)
            textViewTitle.setText(item.footerTitle, TextView.BufferType.SPANNABLE)

            mainLayout.tag = item.necessaryInfo
            val listener = when (item.footerIndex) {
                0 -> necessaryInfoListener
                1 -> deliveryListener
                2 -> inquiryListener
                else -> null
            }
            mainLayout.setOnClickListener(listener)
        }
    }

    abstract class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        abstract fun display(item: GoodsDetailInfoListData, position: Int)
    }

    data class NecessaryInfo(val deliveryNo: String, val goodsName: String,
                             val origin: String, val maker: String): Serializable
}
