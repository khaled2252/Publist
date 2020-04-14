package co.publist.features.wishes

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import co.publist.R
import co.publist.core.common.data.models.User
import co.publist.core.common.data.models.wish.WishAdapterItem
import co.publist.core.utils.DataBindingAdapters
import co.publist.core.utils.Utils.Constants.DETAILS
import co.publist.core.utils.Utils.Constants.MAX_VISIBLE_WISH_ITEMS
import co.publist.core.utils.Utils.Constants.WISH_DETAILS_INTENT
import co.publist.databinding.ItemWishBinding
import co.publist.features.wishdetails.WishDetailsActivity
import org.ocpsoft.prettytime.PrettyTime
import java.util.*
import kotlin.collections.ArrayList

class WishesAdapter(
    val list: ArrayList<WishAdapterItem>,
    val wishesType: Int,
    val user: User?,
    val favoriteListener: (wish: WishAdapterItem, isFavoriting: Boolean) -> Unit,
    val detailsListener: (wish: WishAdapterItem) -> Unit,
    val completeListener: (itemId: String, wish: WishAdapterItem, isDone: Boolean) -> Unit,
    val likeListener: (itemId: String, wish: WishAdapterItem, isLiked: Boolean) -> Unit,
    val seenCountListener: (wishId: String) -> Unit
) :
    RecyclerView.Adapter<WishesAdapter.WishViewHolder>() {
    val wishItemsAdapterArrayList = ArrayList<WishItemsAdapter>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WishViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemWishBinding.inflate(inflater)
        binding.executePendingBindings()
        return WishViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WishViewHolder, position: Int) {
        val wish = list[position]
        if (wish.itemsId!!.size > MAX_VISIBLE_WISH_ITEMS) // To avoid recycling seeMore layout for expandable wish Items
            holder.setIsRecyclable(false)

        if (wish.wishId != null)//Fixme checking because of iOS bug where some wishes are without wishId
            seenCountListener(wish.wishId!!)

        holder.bind(wish, position)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class WishViewHolder(private val binding: ItemWishBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(
            wish: WishAdapterItem,
            position: Int
        ) {
            if (wishesType == DETAILS)
                binding.seeMoreLayout.visibility = View.GONE
            else {
                binding.root.setOnClickListener {
                    val intent = Intent(it.context, WishDetailsActivity::class.java)
                    intent.putExtra(WISH_DETAILS_INTENT, wish)
                    it.context.startActivity(intent)
                }
            }

            if (wish.isCreator)
                binding.wishActionImageView.apply {
                    setImageResource(R.drawable.ic_dots)
                    setOnClickListener {
                        detailsListener(wish)
                    }
                }
            else
                binding.wishActionImageView.apply {
                    if (wish.isFavorite)
                        setImageResource(R.drawable.ic_heart_active)
                    else
                        setImageResource(R.drawable.ic_heart)

                    setOnClickListener {
                        favoriteWish(
                            wish,
                            position,
                            isFavoriting = !wish.isFavorite
                        ) //Taking the opposite of current state
                    }
                }

            binding.categoryNameTextView.text = wish.category!![0].name?.capitalize()
            binding.titleTextView.text = wish.title

            //Load ago time
            val prettyTime = PrettyTime(Locale.getDefault())
            val date = wish.date?.toDate()
            val timeAgo = ". " + prettyTime.format(date)
            binding.timeTextView.text = timeAgo

            //Load creator data
            DataBindingAdapters.loadProfilePicture(
                binding.profilePictureImageView,
                wish.creator?.imagePath
            )
            binding.userNameTextView.text = wish.creator?.name

            //Load wish Image
            DataBindingAdapters.loadWishImage(binding.wishImageView, wish.wishPhotoURL)

            //Load wish Items
            wish.items = wish.items?.toList()?.sortedBy { it.second.orderId }
                ?.toMap() //sort Items map by orderId
            val wishItemsAdapter = WishItemsAdapter(
                wish,
                wishesType,
                user,
                binding.seeMoreTextSwitcher,
                binding.arrowImageView,
                wishItemsAdapterArrayList.size
                , expandListener = { wishItemsAdapterIndex ->
                    //Collapse all other lists except for the current one expanding
                    for (adapterIndex in 0 until wishItemsAdapterArrayList.size) {
                        if (adapterIndex != wishItemsAdapterIndex && wishItemsAdapterArrayList[adapterIndex].isExpanded)
                            wishItemsAdapterArrayList[adapterIndex].collapseList()
                    }

                }, completeListener = { itemId, isDone ->
                    //completed item's wish is added to favorites according to business
                    if (!wish.isCreator && !wish.isFavorite && isDone)
                        binding.wishActionImageView.favoriteWish(wish, position, true)

                    //Add wish first to favorites then increment the done item(for some reason item is incremented twice if incremented in WishItemsAdapter)
                    val incrementAmount = if (isDone) 1 else -1
                    wish.items!![itemId]?.completeCount =
                        wish.items!![itemId]?.completeCount!! + incrementAmount
                    completeListener(itemId, wish, isDone)
                }, likeListener = { itemId, isLiked ->
                    val incrementAmount = if (isLiked) 1 else -1
                    wish.items!![itemId]?.viewedCount =
                        wish.items!![itemId]?.viewedCount!! + incrementAmount
                    likeListener(itemId, wish, isLiked)
                })
            wishItemsAdapterArrayList.add(wishItemsAdapter)

            val mLayoutManager =
                object : LinearLayoutManager(binding.wishItemsRecyclerView.context) {
                    //Added because this recyclerView is in nestedScrollView in activity_home
                    // ,To make outer recyclerView (Wishes) scroll when user touched inner recyclerView (Items)
                    override fun canScrollVertically(): Boolean {
                        return false
                    }
                }

            binding.wishItemsRecyclerView.layoutManager = mLayoutManager
            binding.wishItemsRecyclerView.adapter = wishItemsAdapter
        }

        private fun ImageView.favoriteWish(
            wish: WishAdapterItem,
            position: Int,
            isFavoriting: Boolean
        ) {

            if (isFavoriting) {
                //Update Ui
                setImageResource(R.drawable.ic_heart_active)
                wish.isFavorite = isFavoriting
                //Update remotely
                favoriteListener(wish, isFavoriting)
            } else {
                if (wish.items?.values?.any { it.done == true }!!) {
                    val builder = AlertDialog.Builder(this.context!!)

                    builder.setTitle(context.getString(R.string.remove_wish_title))
                    builder.setMessage(context.getString(R.string.remove_wish_message))
                    builder.setPositiveButton(this.context.getString(R.string.yes)) { _, _ ->
                        setImageResource(R.drawable.ic_heart)
                        wish.isFavorite = isFavoriting
                        favoriteListener(wish, isFavoriting)
                        for (item in wish.items!!.values) {
                            if (item.done!!) {
                                item.done = false
                                item.completeCount = item.completeCount?.dec()
                                item.topCompletedUsersId?.remove(user!!.id)
                            }
                        }
                        notifyItemChanged(position)
                    }
                    builder.setNegativeButton(this.context.getString(R.string.cancel)) { _, _ ->
                    }
                    builder.create().show()
                } else {
                    setImageResource(R.drawable.ic_heart)
                    wish.isFavorite = isFavoriting
                    favoriteListener(wish, isFavoriting)
                }

            }

        }
    }
}