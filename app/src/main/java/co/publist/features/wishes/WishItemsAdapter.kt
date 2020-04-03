package co.publist.features.wishes

import android.content.Intent
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import co.publist.R
import co.publist.core.common.data.models.Mapper
import co.publist.core.common.data.models.wish.Item
import co.publist.core.common.data.models.wish.WishAdapterItem
import co.publist.core.utils.Utils
import co.publist.core.utils.Utils.Constants.DETAILS
import co.publist.core.utils.Utils.Constants.FLAME_ICON_COMPLETED_MINIMUM
import co.publist.core.utils.Utils.Constants.FLAME_ICON_VIEWED_COUNT_PERCENTAGE
import co.publist.core.utils.Utils.Constants.MAX_VISIBLE_WISH_ITEMS
import co.publist.core.utils.Utils.Constants.MINIMUM_WISH_ITEMS
import co.publist.core.utils.Utils.Constants.TOP_USERS_THRESHOLD
import co.publist.core.utils.Utils.get90DegreesAnimation
import co.publist.core.utils.Utils.loadTopUsersPictures
import co.publist.features.wishdetails.WishDetailsActivity
import kotlinx.android.synthetic.main.item_wish_item.view.*


class WishItemsAdapter(
    private val wish: WishAdapterItem,
    private val wishesType: Int,
    private val userId: String,
    private val seeMoreTextView: TextView,
    private val arrowImageView: ImageView,
    private val adapterIndex: Int,
    val expandListener: (adapterIndex: Int) -> Unit,
    val completeListener: (itemId: String, isDone: Boolean) -> Unit,
    val likeListener: (itemId: String, isLiked: Boolean) -> Unit
) :
    RecyclerView.Adapter<WishItemsAdapter.WishItemViewHolder>() {
    var isExpanded = false
    private var itemList: ArrayList<Item> = ArrayList(wish.items!!.values.sortedBy { it.orderId })

    init {
        if (wishesType != DETAILS)
            setExpandingConditions()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WishItemViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_wish_item, parent, false)
        return WishItemViewHolder(view)
    }

    override fun getItemCount(): Int {
        return if (wishesType == DETAILS)
            itemList.size
        else {
            if (!isExpanded)
                MINIMUM_WISH_ITEMS
            else
                itemList.size
        }
    }

    override fun onBindViewHolder(holder: WishItemViewHolder, position: Int) {
        if (!isExpanded && position == itemList.size - 1) //To hide last dashed line if items are not expandable
            holder.itemView.dashed_line.visibility = View.GONE

        holder.bind(itemList[position], position)
    }

    inner class WishItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(
            item: Item,
            position: Int
        ) {
            //Load pre-existing data
            loadTopUsersPictures(item)
            itemView.wishItemTextView.text = item.name
            itemView.completedThisTextView.text =
                seeMoreTextView.context.getString(R.string.completed, item.completeCount)
            itemView.likeViewsTextView.text =
                seeMoreTextView.context.getString(R.string.views, item.viewedCount)
            if (item.viewedCount!! * FLAME_ICON_VIEWED_COUNT_PERCENTAGE >= item.completeCount!! && item.completeCount!! > FLAME_ICON_COMPLETED_MINIMUM)
                itemView.flameImageView.visibility = View.VISIBLE

                //Check/complete item
            if (item.done!!) {
                itemView.wishItemTextView.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
                itemView.completeButton.isChecked = true
            } else {
                itemView.wishItemTextView.paintFlags = Paint.ANTI_ALIAS_FLAG
                itemView.completeButton.isChecked = false
            }

                //Like/view item
            if (item.isLiked!!)
                itemView.likeViewsTextView.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_heart_active,
                    0,
                    0,
                    0
                )
            else
                itemView.likeViewsTextView.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_heart,
                    0,
                    0,
                    0
                )

            //Listeners for actions
            itemView.completeButton.setOnClickListener {
                //Update Ui then remotely
                if (!item.done!!) {//Opposite of previous state
                    itemView.completeButton.isChecked = true
                    item.done = true
                    item.completeCount = item.completeCount?.inc()
                    updateTopCompletedUsersPictures(item,true)
                } else {
                    itemView.completeButton.isChecked = false
                    item.done = false
                    item.completeCount = item.completeCount?.dec()
                    updateTopCompletedUsersPictures(item,false)
                }
                notifyItemChanged(position)

                completeListener(wish.itemsId!![position], item.done!!)
            }

            itemView.likeViewsTextView.setOnClickListener {
                //Update Ui then remotely
                if (!item.isLiked!!) {
                    item.isLiked = true
                    item.viewedCount = item.viewedCount?.inc()
                    updateTopLikedUsersPictures(item,true)
                } else {
                    item.isLiked = false
                    item.viewedCount = item.viewedCount?.dec()
                    updateTopLikedUsersPictures(item,false)
                }
                notifyItemChanged(position)

                likeListener(wish.itemsId!![position], item.isLiked!!)
            }

        }
    }

    private fun setExpandingConditions() {
        if (itemList.size <= MAX_VISIBLE_WISH_ITEMS) {
            seeMoreTextView.visibility = View.GONE
            arrowImageView.visibility = View.GONE
        } else {
            applySeeMoreText()
            (seeMoreTextView.parent as LinearLayout).setOnClickListener {
                if (!isExpanded) {
                    expandList()
                    expandListener(adapterIndex)
                } else {
                    val intent = Intent(it.context, WishDetailsActivity::class.java)
                    intent.putExtra(Utils.Constants.WISH_DETAILS_INTENT, Mapper.mapToWish(wish))
                    it.context.startActivity(intent)
                }
            }
        }
    }

    private fun expandList() {
        isExpanded = true
        notifyDataSetChanged()
        arrowImageView.startAnimation(get90DegreesAnimation())
        seeMoreTextView.text = seeMoreTextView.context.getString(R.string.go_to_details)
    }

    fun collapseList() {
        isExpanded = false
        notifyDataSetChanged()
        arrowImageView.clearAnimation()
        applySeeMoreText()
    }

    private fun applySeeMoreText() {
        val extraWishItemsNumber = (itemList.size) - MAX_VISIBLE_WISH_ITEMS
        seeMoreTextView.text = seeMoreTextView.context.resources.getQuantityString(
            R.plurals.see_more_text,
            extraWishItemsNumber,
            extraWishItemsNumber
        )
    }

    private fun WishItemViewHolder.loadTopUsersPictures(item: Item) {
        when {
            item.done!! -> loadTopUsersPictures(
                item.topCompletedUsersId,
                arrayListOf(
                    itemView.userOneImageView,
                    itemView.userTwoImageView,
                    itemView.userThreeImageView
                )
            )

            item.viewedCount!! > 0 -> loadTopUsersPictures(
                item.topViewedUsersId,
                arrayListOf(
                    itemView.userOneImageView,
                    itemView.userTwoImageView,
                    itemView.userThreeImageView
                )
            )

            else -> loadTopUsersPictures(
                item.topCompletedUsersId,
                arrayListOf(
                    itemView.userOneImageView,
                    itemView.userTwoImageView,
                    itemView.userThreeImageView
                )
            )
        }
    }

    private fun WishItemViewHolder.updateTopCompletedUsersPictures(
        item: Item,
    isAdding : Boolean
    ) {
        if (item.completeCount!! < TOP_USERS_THRESHOLD) {
            if(isAdding)
            item.topCompletedUsersId?.add(userId)
            else
                item.topCompletedUsersId?.remove(userId)

            loadTopUsersPictures(item)
        }
    }

    private fun WishItemViewHolder.updateTopLikedUsersPictures(
        item: Item,
        isAdding : Boolean
    ) {
        if (item.completeCount!! < TOP_USERS_THRESHOLD) {
            if(isAdding)
                item.topViewedUsersId?.add(userId)
            else
                item.topViewedUsersId?.remove(userId)

            loadTopUsersPictures(item)
        }
    }

}