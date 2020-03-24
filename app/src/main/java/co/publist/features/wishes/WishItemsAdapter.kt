package co.publist.features.wishes

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import co.publist.R
import co.publist.core.common.data.models.wish.Item
import co.publist.core.utils.Utils.Constants.MAX_VISIBLE_WISH_ITEMS
import kotlinx.android.synthetic.main.item_wish_item.view.*


class WishItemsAdapter(
    private var itemList: ArrayList<Item>,
    private val moreTextView: TextView,
    private val arrowImageView: ImageView,
    private val adapterIndex: Int,
    val expandListener: (Int) -> Unit
) :
    RecyclerView.Adapter<WishItemsAdapter.WishItemViewHolder>() {
    private val expandableList = ArrayList<Item>()
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
        return itemList.size
    }

    override fun onBindViewHolder(holder: WishItemViewHolder, position: Int) {
        holder.bind(itemList[position])
        if (position >= MAX_VISIBLE_WISH_ITEMS && !isExpanded) {
            expandableList.add(itemList[position])
        }
    }

    inner class WishItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(
            item: Item
        ) {
            itemView.wishItemTextView.text = item.name
            itemView.likeViewsTextView.text = "+ ${item.viewedCount.toString()} Views"
            itemView.completedThisTextView.text = "+ ${item.completeCount.toString()} Completed"
        }
    }

     private fun setExpandingConditions() {
        if (itemList.size <= MAX_VISIBLE_WISH_ITEMS) {
            moreTextView.visibility = View.GONE
            arrowImageView.visibility = View.GONE
        } else {
            val extraWishItemsNumber = itemList.size - MAX_VISIBLE_WISH_ITEMS
            if (extraWishItemsNumber == 1)
                moreTextView.text = "$extraWishItemsNumber More Check Point"
            else
                moreTextView.text = "$extraWishItemsNumber More Check Points"

            moreTextView.setOnClickListener {
                if (!isExpanded) {
                    expandList()
                    expandListener(adapterIndex)
                }
                else
                {
                    //todo navigate to details
                }
            }
        }
    }

    private fun expandList() {
        moreTextView.visibility = View.VISIBLE
        arrowImageView.visibility = View.VISIBLE
        moreTextView.text = moreTextView.context.getString(R.string.go_to_details)
        //animation
//                val anim =  RotateAnimation(0f, -90f, Animation.RELATIVE_TO_SELF,
//                    0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
//                anim.interpolator = LinearInterpolator()
//                anim.duration = 500
//                anim.isFillEnabled = true
//                anim.fillAfter = true
//                arrowImageView.startAnimation(anim)
        itemList.addAll(expandableList)
        isExpanded = true
        notifyDataSetChanged()
    }

    fun collapseExtraWishItems() {
        isExpanded = false
        for (position in MAX_VISIBLE_WISH_ITEMS until itemList.size)
            itemList.removeAt(itemList.size - 1)
        notifyDataSetChanged()
    }

    fun collapseUi()
    {
        val extraWishItemsNumber = (itemList.size+expandableList.size) - MAX_VISIBLE_WISH_ITEMS
        if (extraWishItemsNumber == 1)
            moreTextView.text = "$extraWishItemsNumber More Check Point"
        else
            moreTextView.text = "$extraWishItemsNumber More Check Points"

    }
}