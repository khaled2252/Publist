package com.publist.features.wishes

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.publist.R
import com.publist.core.common.data.models.User
import com.publist.core.common.data.models.wish.WishItem
import com.publist.core.utils.Utils.Constants.FLAME_ICON_COMPLETED_MINIMUM
import com.publist.core.utils.Utils.Constants.FLAME_ICON_VIEWED_COUNT_PERCENTAGE
import com.publist.core.utils.Utils.Constants.MINIMUM_WISH_ITEMS
import com.publist.core.utils.Utils.Constants.TOP_USERS_THRESHOLD
import com.publist.core.utils.Utils.loadTopUsersPictures
import com.publist.core.utils.Utils.showLoginPromptForGuest
import kotlinx.android.synthetic.main.item_wish_item.view.*


class WishItemsAdapter(
    private val wishItems: Collection<WishItem>,
    private val isExpanded: Boolean,
    private val user: User?,
    val completeListener: (itemPosition: Int, isDone: Boolean) -> Unit,
    val likeListener: (itemPosition: Int, isLiked: Boolean) -> Unit,
    val viewHolderSpaceClickListener: () -> Unit
) :
    RecyclerView.Adapter<WishItemsAdapter.WishItemViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WishItemViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_wish_item, parent, false)
        return WishItemViewHolder(view)
    }

    override fun getItemCount(): Int {
        return if (isExpanded)
            wishItems.size
        else
            MINIMUM_WISH_ITEMS
    }

    override fun onBindViewHolder(holder: WishItemViewHolder, position: Int) {
        holder.bind(wishItems.elementAt(position), position)
    }

    inner class WishItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(
            wishItem: WishItem,
            position: Int
        ) {
            //Load pre-existing data
            loadTopUsersPictures(wishItem)
            itemView.wishItemTextView.text = wishItem.name
            itemView.completedThisTextView.text =
                itemView.context.getString(R.string.completed, wishItem.completeCount)
            itemView.likeViewsTextView.text =
                itemView.context.getString(R.string.views, wishItem.viewedCount)
            if (wishItem.viewedCount!! * FLAME_ICON_VIEWED_COUNT_PERCENTAGE >= wishItem.completeCount!! && wishItem.completeCount!! > FLAME_ICON_COMPLETED_MINIMUM)
                itemView.flameImageView.visibility = View.VISIBLE

            //Check/complete item
            if (wishItem.done!!) {
                itemView.wishItemTextView.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
                itemView.completeButton.isChecked = true
            } else {
                itemView.wishItemTextView.paintFlags = Paint.ANTI_ALIAS_FLAG
                itemView.completeButton.isChecked = false
            }

            //Like/view item
            if (wishItem.isLiked!!)
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
            itemView.setOnClickListener {
                viewHolderSpaceClickListener()
            }

            itemView.completeButton.setOnClickListener {
                itemView.completeItem(wishItem, position)
            }

            itemView.wishItemTextView.setOnClickListener {
                itemView.completeItem(wishItem, position)
            }

            itemView.likeViewsTextView.setOnClickListener {
                if (user == null)
                    showLoginPromptForGuest(it.context)
                else {
                    wishItem.isLiked = !wishItem.isLiked!!
                    val incrementAmount = if (wishItem.isLiked!!) 1 else -1
                    wishItem.viewedCount =
                        wishItem.viewedCount!! + incrementAmount
                    if (wishItem.viewedCount!! < TOP_USERS_THRESHOLD) {
                        if (wishItem.isLiked!!)
                            wishItem.topViewedUsersId?.add(user.id!!)
                        else
                            wishItem.topViewedUsersId?.remove(user.id)
                    }
                    notifyItemChanged(position)

                    likeListener(
                        position,
                        wishItem.isLiked!!
                    )
                }
            }
        }

        private fun View.completeItem(
            wishItem: WishItem,
            position: Int
        ) {
            if (user == null)
                showLoginPromptForGuest(this.context)
            else {
                //Update Ui then remotely
                wishItem.done = !wishItem.done!! //Opposite of the previous state
                val incrementAmount = if (wishItem.done!!) 1 else -1
                wishItem.completeCount =
                    wishItem.completeCount!! + incrementAmount
                if (wishItem.completeCount!! < TOP_USERS_THRESHOLD) {
                    if (wishItem.done!!)
                        wishItem.topCompletedUsersId?.add(user.id!!)
                    else
                        wishItem.topCompletedUsersId?.remove(user.id)
                }
                notifyItemChanged(position)

                completeListener(
                    position,
                    wishItem.done!!
                )
            }
        }
    }

    private fun WishItemViewHolder.loadTopUsersPictures(wishItem: WishItem) {
        val imageViewArrayList = arrayListOf(
            itemView.userOneImageView,
            itemView.userTwoImageView,
            itemView.userThreeImageView
        )
        when {
            wishItem.done!! -> loadTopUsersPictures(
                wishItem.topCompletedUsersId,
                imageViewArrayList,
                user
            )
            wishItem.viewedCount!! > 0 -> loadTopUsersPictures(
                wishItem.topViewedUsersId,
                imageViewArrayList,
                user
            )
            else -> loadTopUsersPictures(wishItem.topCompletedUsersId, imageViewArrayList, user)
        }
    }

}