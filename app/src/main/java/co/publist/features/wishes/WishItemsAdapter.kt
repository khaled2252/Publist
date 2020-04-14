package co.publist.features.wishes

import android.content.Intent
import android.graphics.Paint
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextSwitcher
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import co.publist.R
import co.publist.core.common.data.models.User
import co.publist.core.common.data.models.wish.WishAdapterItem
import co.publist.core.common.data.models.wish.WishItem
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
    private val user: User?,
    private val seeMoreTextSwitcher: TextSwitcher,
    private val arrowImageView: ImageView,
    private val adapterIndex: Int,
    val expandListener: (adapterIndex: Int) -> Unit,
    val completeListener: (itemId: String, isDone: Boolean) -> Unit,
    val likeListener: (itemId: String, isLiked: Boolean) -> Unit
) :
    RecyclerView.Adapter<WishItemsAdapter.WishItemViewHolder>() {
    var isExpanded = false
    private var wishItemList: ArrayList<WishItem> = ArrayList(wish.items!!.values)

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
            wishItemList.size
        else {
            if (!isExpanded)
                MINIMUM_WISH_ITEMS
            else
                wishItemList.size
        }
    }

    override fun onBindViewHolder(holder: WishItemViewHolder, position: Int) {
        holder.bind(wishItemList[position], position)
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
            itemView.completeButton.setOnClickListener {
                //Update Ui then remotely
                if (!wishItem.done!!) {//Opposite of previous state
                    wishItem.done = true
                    updateTopCompletedUsersPictures(wishItem, true)
                } else {
                    wishItem.done = false
                    updateTopCompletedUsersPictures(wishItem, false)
                }
                notifyItemChanged(position)

                completeListener(wish.items!!.keys.elementAt(position), wishItem.done!!)
            }

            itemView.likeViewsTextView.setOnClickListener {
                //Update Ui then remotely
                if (!wishItem.isLiked!!) {
                    wishItem.isLiked = true
                    updateTopLikedUsersPictures(wishItem, true)
                } else {
                    wishItem.isLiked = false
                    updateTopLikedUsersPictures(wishItem, false)
                }
                notifyItemChanged(position)

                likeListener(wish.items!!.keys.elementAt(position), wishItem.isLiked!!)
            }

        }
    }

    private fun setExpandingConditions() {
        if (wishItemList.size <= MAX_VISIBLE_WISH_ITEMS) {
            seeMoreTextSwitcher.visibility = View.GONE
            arrowImageView.visibility = View.GONE
        } else {
            seeMoreTextSwitcher.setFactory {
                val textView = TextView(seeMoreTextSwitcher.context)
                textView.typeface = ResourcesCompat.getFont(
                    seeMoreTextSwitcher.context,
                    R.font.sfprodisplaysemibold
                )
                textView.setTextColor(
                    ContextCompat.getColor(
                        seeMoreTextSwitcher.context,
                        R.color.sunsetOrange
                    )
                )
                textView.gravity = Gravity.CENTER
                val textSizeDimen =
                    textView.resources.getDimension(R.dimen.wish_item_see_more_text_size)
                textView.textSize = textSizeDimen / textView.resources.displayMetrics.scaledDensity
                return@setFactory textView
            }
            applySeeMoreText()
            (seeMoreTextSwitcher.parent as LinearLayout).setOnClickListener {
                if (!isExpanded) {
                    expandList()
                    expandListener(adapterIndex)
                } else {
                    val intent = Intent(it.context, WishDetailsActivity::class.java)
                    intent.putExtra(Utils.Constants.WISH_DETAILS_INTENT, wish)
                    it.context.startActivity(intent)
                }
            }
        }
    }

    private fun expandList() {
        isExpanded = true
        notifyItemRangeChanged(MINIMUM_WISH_ITEMS, wishItemList.size - MINIMUM_WISH_ITEMS)
        arrowImageView.startAnimation(get90DegreesAnimation())
        seeMoreTextSwitcher.setText(seeMoreTextSwitcher.context.getString(R.string.go_to_details))
    }

    fun collapseList() {
        isExpanded = false
        notifyItemRangeChanged(MINIMUM_WISH_ITEMS, wishItemList.size - MINIMUM_WISH_ITEMS)
        arrowImageView.clearAnimation()
        applySeeMoreText()
    }

    private fun applySeeMoreText() {
        val extraWishItemsNumber = (wishItemList.size) - MAX_VISIBLE_WISH_ITEMS
        seeMoreTextSwitcher.setText(
            seeMoreTextSwitcher.context.resources.getQuantityString(
                R.plurals.see_more_text,
                extraWishItemsNumber,
                extraWishItemsNumber
            )
        )

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

    private fun updateTopCompletedUsersPictures(
        wishItem: WishItem,
        isAdding: Boolean
    ) {
        if (wishItem.completeCount!! < TOP_USERS_THRESHOLD) {
            if (isAdding)
                wishItem.topCompletedUsersId?.add(user!!.id!!)
            else
                wishItem.topCompletedUsersId?.remove(user!!.id)
        }
    }

    private fun updateTopLikedUsersPictures(
        wishItem: WishItem,
        isAdding: Boolean
    ) {
        if (wishItem.completeCount!! < TOP_USERS_THRESHOLD) {
            if (isAdding)
                wishItem.topViewedUsersId?.add(user!!.id!!)
            else
                wishItem.topViewedUsersId?.remove(user!!.id)
        }
    }

}