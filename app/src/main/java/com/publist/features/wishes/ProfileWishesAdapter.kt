package com.publist.features.wishes

import android.content.Intent
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.publist.R
import com.publist.core.common.data.models.User
import com.publist.core.common.data.models.wish.WishAdapterItem
import com.publist.core.utils.DataBindingAdapters
import com.publist.core.utils.Utils.Constants.DETAILS
import com.publist.core.utils.Utils.Constants.GENERAL_TYPE
import com.publist.core.utils.Utils.Constants.LISTS
import com.publist.core.utils.Utils.Constants.LOADING_MORE
import com.publist.core.utils.Utils.Constants.MAX_VISIBLE_WISH_ITEMS
import com.publist.core.utils.Utils.Constants.WISH_DETAILS_INTENT
import com.publist.databinding.ItemWishBinding
import com.publist.features.wishdetails.WishDetailsActivity
import org.ocpsoft.prettytime.PrettyTime
import java.util.*


class ProfileWishesAdapter(
    val wishesType: Int,
    val user: User,
    val unFavoriteListener: (wish: WishAdapterItem) -> Unit,
    val detailsListener: (wish: WishAdapterItem) -> Unit,
    val completeListener: (itemId: String, wishId: String, isCreator: Boolean, isDone: Boolean) -> Unit,
    val likeListener: (itemId: String, wishId: String, isLiked: Boolean) -> Unit,
    val seenCountListener: (wishId: String) -> Unit,
    val seeMoreListener: (position: Int) -> Unit,
    val getCategoryNameById: (categoryId: String) -> String
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private lateinit var loadMoreView: View
    private val wishList = ArrayList<WishAdapterItem?>()
    var loadMoreViewHeight = 0
    var currentExpandedWishId: String? = null
    var doneItemsList = ArrayList<String>()
    var likedItemsList = ArrayList<String>()
    var currentRemovedWish: WishAdapterItem? = null
    var currentRemovedWishPosition = -1
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            LOADING_MORE -> {
                loadMoreView = LayoutInflater.from(parent.context).inflate(
                    R.layout.item_load_more, parent, false
                )
                LoadMoreViewHolder(loadMoreView)
            }
            else -> {
                val inflater = LayoutInflater.from(parent.context)
                val binding = ItemWishBinding.inflate(inflater, parent, false)
                binding.executePendingBindings()
                WishViewHolder(binding)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when {
            wishList[position] == null -> LOADING_MORE
            else -> GENERAL_TYPE
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is WishViewHolder) {
            val wish = wishList[position]
            seenCountListener(wish?.wishId!!)
            holder.bind(wish, position)
        }
    }

    override fun getItemCount(): Int {
        return wishList.size
    }

    fun addWishes(wishesArray: ArrayList<WishAdapterItem>) {
        val startPosition = wishList.size
        wishList.addAll(wishesArray)
        notifyItemRangeInserted(startPosition, wishesArray.size)
    }

    fun addDoneAndLikedItems(doneItems: ArrayList<String>, likedItems: ArrayList<String>) {
        doneItemsList = doneItems
        likedItemsList = likedItems
    }

    fun renderLoadMoreUi(isLoading: Boolean) {
        if (isLoading) {
            wishList.add(null)
            notifyItemInserted(wishList.size + 1)
        } else {
            if (::loadMoreView.isInitialized)
                loadMoreViewHeight = loadMoreView.measuredHeight

            wishList.remove(null)
            notifyDataSetChanged()
        }
    }

    inner class LoadMoreViewHolder(view: View) : RecyclerView.ViewHolder(view)
    inner class WishViewHolder(private val binding: ItemWishBinding) :
        RecyclerView.ViewHolder(binding.root) {
        lateinit var holderWish: WishAdapterItem
        var isExpanded = false
        fun bind(
            wish: WishAdapterItem,
            position: Int
        ) {
            holderWish = wish
            binding.apply {
                //Apply done,liked items in this wish
                for (itemMap in wish.items!!) {
                    if (likedItemsList.contains(itemMap.key))
                        itemMap.value.isLiked = true
                    if (doneItemsList.contains(itemMap.key))
                        itemMap.value.done = true
                }

                //Setup edit dots or favorite icon
                if (wish.isCreator)
                    wishActionImageView.apply {
                        setImageResource(R.drawable.ic_dots)
                        setOnClickListener {
                            detailsListener(wish)
                        }
                    }
                else
                    binding.wishActionImageView.apply {
                        if (wishesType == LISTS) {
                            wish.isCreator = true
                            setImageResource(R.drawable.ic_dots)
                            setOnClickListener {
                                detailsListener(wish)
                            }
                        } else {
                            wish.isFavorite = true
                            setImageResource(R.drawable.ic_heart_active)
                            setOnClickListener {
                                if (wish.items?.values?.any { it.done == true }!!) {
                                    val builder = AlertDialog.Builder(this.context!!)
                                    builder.setTitle(context.getString(R.string.remove_wish_title))
                                    builder.setMessage(context.getString(R.string.remove_wish_message))
                                    builder.setPositiveButton(this.context.getString(R.string.yes)) { _, _ ->
                                        removeWish(wish, adapterPosition, true)
                                        showUndoSnackbar(this)
                                    }
                                    builder.setNegativeButton(this.context.getString(R.string.cancel)) { _, _ ->
                                    }
                                    builder.create().show()
                                } else {
                                    removeWish(wish, adapterPosition, true)
                                    showUndoSnackbar(this)
                                }
                            }
                        }
                    }

                //Category , Title
                categoryNameTextView.text = getCategoryNameById(wish.categoryId!![0])
                titleTextView.text = wish.title

                //Load ago time
                val prettyTime = PrettyTime(Locale.getDefault())
                val date = wish.date?.toDate()
                val timeAgo = ". " + prettyTime.format(date)
                timeTextView.text = timeAgo

                //Load creator data
                DataBindingAdapters.loadProfilePicture(
                    profilePictureImageView,
                    wish.creator?.imagePath
                )
                userNameTextView.text = wish.creator?.name

                //Load wish Image
                DataBindingAdapters.loadWishImage(wishImageView, wish.wishPhotoURL)

                //Load wish Items
                //sort Items map by orderId
                wish.items = wish.items?.toList()?.sortedBy { it.second.orderId }?.toMap()

                //Setup expanding conditions
                if (wishesType == DETAILS) {
                    seeMoreLayout.visibility = View.GONE
                    setWishItemsAdapter(true) //Expanded wish in details screen
                }
                else {
                    root.setOnClickListener {//Navigate to details screen when clicking on empty spaces in wish (while not in details)
                        val intent = Intent(it.context, WishDetailsActivity::class.java)
                        intent.putExtra(WISH_DETAILS_INTENT, wish)
                        it.context.startActivity(intent)
                    }

                    if (wish.items!!.size <= MAX_VISIBLE_WISH_ITEMS) {
                        seeMoreTextSwitcher.visibility = View.GONE
                        arrowImageView.visibility = View.GONE
                    } else {//Expandable wish
                        seeMoreTextSwitcher.visibility = View.VISIBLE
                        arrowImageView.visibility = View.VISIBLE
                        setTextSwitcherFactory()
                        applySeeMoreText()
                        seeMoreLayout.setOnClickListener {
                            if (!isExpanded) {
                                notifyDataSetChanged() // To reload all wishes thus collapsing the other expanded one
                                expandWish(position, true)
                                currentExpandedWishId = holderWish.wishId
                            } else {
                                val intent = Intent(it.context, WishDetailsActivity::class.java)
                                intent.putExtra(WISH_DETAILS_INTENT, wish)
                                it.context.startActivity(intent)
                            }
                        }
                        if (holderWish.wishId == currentExpandedWishId) //Expand wish that was already expanded (was collapsed due to recycling)
                            expandWish(position, false)
                        else
                            collapseWish()
                    }
                    setWishItemsAdapter(isExpanded)
                }
            }


        }

        private fun setWishItemsAdapter(isExpanded: Boolean) {
            val wishItemsAdapter = WishItemsAdapter(
                holderWish.items!!.values,
                isExpanded,
                user,
                completeListener = { itemPosition, isDone ->
                    completeListener(
                        holderWish.itemsId!!.elementAt(itemPosition),
                        holderWish.wishId!!,
                        holderWish.isCreator,
                        isDone
                    )
                }, likeListener = { itemPosition, isLiked ->
                    likeListener(
                        holderWish.itemsId!!.elementAt(itemPosition),
                        holderWish.wishId!!,
                        isLiked
                    )
                }, viewHolderSpaceClickListener = {
                    val intent = Intent(
                        binding.wishItemsRecyclerView.context,
                        WishDetailsActivity::class.java
                    )
                    intent.putExtra(WISH_DETAILS_INTENT, holderWish)
                    binding.wishItemsRecyclerView.context.startActivity(intent)
                })
            binding.wishItemsRecyclerView.setHasFixedSize(true) // To avoid recyclerView scrolling up when doing action on wish
            binding.wishItemsRecyclerView.adapter = wishItemsAdapter
        }

        private fun setTextSwitcherFactory() {
            binding.apply {
                if (seeMoreTextSwitcher.childCount == 0) {
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
                        textView.textSize =
                            textSizeDimen / textView.resources.displayMetrics.scaledDensity
                        return@setFactory textView
                    }
                }
            }
        }

        private fun applySeeMoreText() {
            binding.apply {
                val extraWishItemsNumber = (holderWish.items!!.size) - MAX_VISIBLE_WISH_ITEMS
                seeMoreTextSwitcher.setCurrentText(
                    seeMoreTextSwitcher.context.resources.getQuantityString(
                        R.plurals.see_more_text,
                        extraWishItemsNumber,
                        extraWishItemsNumber
                    )
                )
            }
        }

        private fun expandWish(position: Int, withAnimation: Boolean) {
            if (withAnimation) {
                //seeMoreListener(position)
                binding.seeMoreTextSwitcher.setText(binding.seeMoreTextSwitcher.context.getString(R.string.go_to_details))
            } else
                binding.seeMoreTextSwitcher.setCurrentText(
                    binding.seeMoreTextSwitcher.context.getString(
                        R.string.go_to_details
                    )
                )

            isExpanded = true
            setWishItemsAdapter(isExpanded)
            binding.arrowImageView.setImageResource(R.drawable.ic_details)
        }

        private fun collapseWish() {
            isExpanded = false
            setWishItemsAdapter(isExpanded)
            binding.arrowImageView.setImageResource(R.drawable.ic_down)
            applySeeMoreText()
        }
    }

    private fun removeWish(
        wish: WishAdapterItem,
        position: Int,
        isRemoving: Boolean
    ) {
        if (isRemoving) {
            currentRemovedWish = wish
            currentRemovedWishPosition = position
            wishList.removeAt(position)
            notifyItemRemoved(position)
            notifyDataSetChanged()
        } else {
            wishList.add(position, wish)
            notifyItemInserted(position)
        }
    }

    private fun showUndoSnackbar(view: View) {
        val snackbar: Snackbar = Snackbar.make(
            view, view.context.getString(R.string.wish_removed),
            Snackbar.LENGTH_LONG
        )
        var isDeleted = true
        snackbar.addCallback(object : Snackbar.Callback() {
            override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                if (isDeleted)
                    unFavoriteListener(currentRemovedWish!!)
                super.onDismissed(transientBottomBar, event)
            }
        })
        snackbar.setAction(view.context.getString(R.string.undo)) {
            removeWish(currentRemovedWish!!, currentRemovedWishPosition, false)
            isDeleted = false
            seeMoreListener(currentRemovedWishPosition)
        }
        snackbar.show()
    }
}