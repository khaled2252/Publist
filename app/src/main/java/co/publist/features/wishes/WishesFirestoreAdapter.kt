package co.publist.features.wishes

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import co.publist.R
import co.publist.core.common.data.models.User
import co.publist.core.common.data.models.wish.Wish
import co.publist.core.common.data.models.wish.WishAdapterItem
import co.publist.core.utils.DataBindingAdapters.loadProfilePicture
import co.publist.core.utils.DataBindingAdapters.loadWishImage
import co.publist.core.utils.Utils
import co.publist.core.utils.Utils.Constants.DETAILS
import co.publist.core.utils.Utils.Constants.LISTS
import co.publist.core.utils.Utils.Constants.WISH_DETAILS_INTENT
import co.publist.databinding.ItemWishBinding
import co.publist.features.wishdetails.WishDetailsActivity
import com.firebase.ui.common.ChangeEventType
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.DocumentSnapshot
import org.ocpsoft.prettytime.PrettyTime
import java.util.*
import kotlin.collections.ArrayList


class WishesFirestoreAdapter(
    options: FirestoreRecyclerOptions<WishAdapterItem>,
    val wishesType: Int,
    val correspondingPublicWishes : ArrayList<Wish>,
    val doneItemsList: ArrayList<String>,
    val likedItemsList: ArrayList<String>,
    val user: User,
    val displayPlaceHolder: (Boolean) -> Unit,
    val unFavoriteListener: (wish: WishAdapterItem) -> Unit,
    val detailsListener: (wish: WishAdapterItem) -> Unit,
    val completeListener: (itemId: String, wish: WishAdapterItem, isDone: Boolean) -> Unit,
    val likeListener: (itemId: String, wish: WishAdapterItem, isLiked: Boolean) -> Unit,
    val seenCountListener : (wishId : String) -> Unit

) :
    FirestoreRecyclerAdapter<WishAdapterItem, WishesFirestoreAdapter.WishViewHolder>(options) {
    val wishItemsAdapterArrayList = ArrayList<WishItemsAdapter>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WishViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemWishBinding.inflate(inflater)
        binding.executePendingBindings()
        return WishViewHolder(binding)
    }

    override fun onDataChanged() {
        if (itemCount == 0)
            displayPlaceHolder(true)
        else
            displayPlaceHolder(false)

        super.onDataChanged()
    }

    override fun onChildChanged(
        type: ChangeEventType,
        snapshot: DocumentSnapshot,
        newIndex: Int,
        oldIndex: Int
    ) {
        if(type != ChangeEventType.CHANGED) //To Update only when adding/removing (To avoid UI conflict when updating UI manually in WishItemsAdapter)
        super.onChildChanged(type, snapshot, newIndex, oldIndex)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onBindViewHolder(holder: WishViewHolder, position: Int, wish: WishAdapterItem) {
        if(wish.itemsId!!.size> Utils.Constants.MAX_VISIBLE_WISH_ITEMS) // To avoid recycling seeMore layout for expandable wish Items
            holder.setIsRecyclable(false)

        seenCountListener(wish.wishId!!)
        applyPublicWishDataToReceivedWish(wish,correspondingPublicWishes[position])
        holder.bind(wish)
    }

    private fun applyPublicWishDataToReceivedWish(wish: WishAdapterItem, publicWish: Wish) {
        wish.title = publicWish.title
        wish.creator = publicWish.creator
        wish.category = publicWish.category
        wish.categoryId = publicWish.categoryId
        wish.photoName = publicWish.photoName
        wish.wishPhotoURL = publicWish.wishPhotoURL
        wish.itemsId = publicWish.itemsId
        wish.items = publicWish.items
        wish.date = publicWish.date
    }

    inner class WishViewHolder(private val binding: ItemWishBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(
            wish: WishAdapterItem
        ) {
            if(wishesType == DETAILS)
                binding.seeMoreLayout.visibility = View.GONE
            else {
                binding.root.setOnClickListener {
                    val intent = Intent(it.context, WishDetailsActivity::class.java)
                    intent.putExtra(WISH_DETAILS_INTENT, wish)
                    it.context.startActivity(intent)
                }
            }

            //Apply done,liked items in this wish
            for (itemMap in wish.items!!)
            {
                if (likedItemsList.contains(itemMap.key))
                    itemMap.value.isLiked = true
                if (doneItemsList.contains(itemMap.key))
                    itemMap.value.done = true
            }

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
                        if(wish.items?.values?.any {it.done==true}!!)
                        {
                            val builder = AlertDialog.Builder(this.context!!)

                            builder.setTitle(context.getString(R.string.remove_wish_title))
                            builder.setMessage(context.getString(R.string.remove_wish_message))
                            builder.setPositiveButton(this.context.getString(R.string.yes)) { _, _ ->
                                this@WishViewHolder.adapterPosition
                                unFavoriteListener(wish)
                            }
                            builder.setNegativeButton(this.context.getString(R.string.cancel)) { _, _ ->
                            }
                            builder.create().show()
                        }
                        else
                        {
                            wish.isFavorite = false
                            unFavoriteListener(wish)
                        }
                    }
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
            loadProfilePicture(binding.profilePictureImageView, wish.creator?.imagePath)
            binding.userNameTextView.text = wish.creator?.name

            //Load wish Image
            loadWishImage(binding.wishImageView, wish.wishPhotoURL)

            //Load wish Items
            wish.items = wish.items?.toList()?.sortedBy { it.second.orderId }?.toMap() //sort Items map by orderId
            val wishItemsAdapter = WishItemsAdapter(
                wish,
                LISTS,
                user,
                binding.seeMoreTextView,
                binding.arrowImageView,
                wishItemsAdapterArrayList.size
                , expandListener = { wishItemsAdapterIndex ->
                    //Collapse all other lists except for the current one expanding
                    for (adapterIndex in 0 until wishItemsAdapterArrayList.size) {
                        if (adapterIndex != wishItemsAdapterIndex && wishItemsAdapterArrayList[adapterIndex].isExpanded)
                            wishItemsAdapterArrayList[adapterIndex].collapseList()
                    }
                },completeListener = { itemId, isDone ->
                    val incrementAmount = if(isDone) 1 else -1
                    wish.items!![itemId]?.completeCount = wish.items!![itemId]?.completeCount!!+incrementAmount
                    completeListener(itemId, wish, isDone)
                },likeListener = {itemId, isLiked ->
                    val incrementAmount = if(isLiked) 1 else -1
                    wish.items!![itemId]?.viewedCount = wish.items!![itemId]?.viewedCount!!+incrementAmount
                    likeListener(itemId,wish,isLiked)
                })
            wishItemsAdapterArrayList.add(wishItemsAdapter)
            binding.wishItemsRecyclerView.adapter = wishItemsAdapter
        }

    }
}