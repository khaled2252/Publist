package co.publist.features.wishes

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import co.publist.R
import co.publist.core.common.data.models.wish.Item
import co.publist.core.utils.Utils.Constants.MAX_VISIBLE_WISH_ITEMS
import co.publist.core.utils.Utils.Constants.MINIMUM_WISH_ITEMS
import co.publist.core.utils.Utils.get90DegreesAnimation
import kotlinx.android.synthetic.main.item_wish_item.view.*


class WishItemsAdapter(
    private var itemList: ArrayList<Item>,
    private val seeMoreTextView: TextView,
    private val arrowImageView: ImageView,
    private val adapterIndex: Int,
    val expandListener: (Int) -> Unit
) :
    RecyclerView.Adapter<WishItemsAdapter.WishItemViewHolder>() {
    var isExpanded = false

    init {
        setExpandingConditions()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WishItemViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_wish_item, parent, false)
        return WishItemViewHolder(view)
    }

    override fun getItemCount(): Int {
        return if (!isExpanded)
            MINIMUM_WISH_ITEMS
        else
            itemList.size
    }

    override fun onBindViewHolder(holder: WishItemViewHolder, position: Int) {
        holder.bind(itemList[position])
    }

    inner class WishItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(
            item: Item
        ) {
            itemView.wishItemTextView.text = item.name
            val likeString = "+ ${item.viewedCount.toString()} Views"
            val completeString = "+ ${item.completeCount.toString()} Completed"
            itemView.likeViewsTextView.text = likeString
            itemView.completedThisTextView.text = completeString
        }
    }

    private fun setExpandingConditions() {
        if (itemList.size <= MAX_VISIBLE_WISH_ITEMS) {
            seeMoreTextView.visibility = View.GONE
            arrowImageView.visibility = View.GONE
        } else {
            renderSeeMoreUi()
            (seeMoreTextView.parent as LinearLayout).setOnClickListener {
                if (!isExpanded) {
                    expandList()
                    expandListener(adapterIndex)
                } else {
                    //todo navigate to details
                }
            }
        }
    }

    private fun expandList() {
        seeMoreTextView.text = seeMoreTextView.context.getString(R.string.go_to_details)
        arrowImageView.startAnimation(get90DegreesAnimation())
        isExpanded = true
        notifyDataSetChanged()
    }

    fun collapseList() {
        isExpanded = false
        notifyDataSetChanged()
        arrowImageView.clearAnimation()
        renderSeeMoreUi()
    }

    private fun renderSeeMoreUi() {
        val extraWishItemsNumber = (itemList.size) - MAX_VISIBLE_WISH_ITEMS
        val seeMoreStringOne = "$extraWishItemsNumber More Check Point"
        val seeMoreStringMany = "$extraWishItemsNumber More Check Points"
        if (extraWishItemsNumber == 1)
            seeMoreTextView.text = seeMoreStringOne
        else
            seeMoreTextView.text = seeMoreStringMany
    }
}