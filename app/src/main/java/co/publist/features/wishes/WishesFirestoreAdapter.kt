package co.publist.features.wishes

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
import co.publist.R
import co.publist.core.common.data.models.User
import co.publist.core.common.data.models.wish.WishAdapterItem
import co.publist.core.utils.DataBindingAdapters
import co.publist.core.utils.Utils.Constants.DETAILS
import co.publist.core.utils.Utils.Constants.LISTS
import co.publist.core.utils.Utils.Constants.MAX_VISIBLE_WISH_ITEMS
import co.publist.core.utils.Utils.Constants.NOT_EXPANDABLE
import co.publist.core.utils.Utils.Constants.WISH_DETAILS_INTENT
import co.publist.core.utils.Utils.get90DegreesAnimation
import co.publist.databinding.ItemWishBinding
import co.publist.features.wishdetails.WishDetailsActivity
import com.firebase.ui.common.ChangeEventType
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.DocumentSnapshot
import org.ocpsoft.prettytime.PrettyTime
import java.util.*


class WishesFirestoreAdapter(
    options: FirestoreRecyclerOptions<WishAdapterItem>,
    val wishesType: Int,
    val doneItemsList: ArrayList<String>,
    val likedItemsList: ArrayList<String>,
    val user: User,
    val displayPlaceHolder: (Boolean) -> Unit,
    val unFavoriteListener: (wish: WishAdapterItem) -> Unit,
    val detailsListener: (wish: WishAdapterItem) -> Unit,
    val completeListener: (itemId: String, wishId: String, isCreator: Boolean, isDone: Boolean) -> Unit,
    val likeListener: (itemId: String, wishId: String, isLiked: Boolean) -> Unit,
    val seenCountListener: (wishId: String) -> Unit,
    val scrollListener: (position: Int) -> Unit

) :
    FirestoreRecyclerAdapter<WishAdapterItem, WishesFirestoreAdapter.WishViewHolder>(options) {
    val expandableViewHolders = mutableMapOf<Int, WishViewHolder>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WishViewHolder {
        return if (viewType == NOT_EXPANDABLE) {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ItemWishBinding.inflate(inflater)
            binding.executePendingBindings()
            WishViewHolder(binding)
        } else { //Return a viewHolder with corresponding binding of the expandable wish
            WishViewHolder(expandableViewHolders[viewType]!!.holderBinding)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (expandableViewHolders.keys.contains(position)) //Return expandable wish position as unique viewType
            position
        else
            return NOT_EXPANDABLE
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
        if (type != ChangeEventType.CHANGED) //To Update only when adding/removing (To avoid UI conflict when updating UI manually in WishItemsAdapter)
            super.onChildChanged(type, snapshot, newIndex, oldIndex)
    }


    override fun onBindViewHolder(holder: WishViewHolder, position: Int, wish: WishAdapterItem) {
        if (wish.wishId != null)//Fixme checking because of iOS bug where some wishes are without wishId
            seenCountListener(wish.wishId!!)

        if (!expandableViewHolders.containsKey(position)) //To avoid binding expandable items
            holder.bind(wish, position)
    }

    inner class WishViewHolder(private val binding: ItemWishBinding) :
        RecyclerView.ViewHolder(binding.root) {
        lateinit var holderWish: WishAdapterItem
        var holderBinding = binding
        var isExpanded = false
        fun bind(
            wish: WishAdapterItem,
            position: Int
        ) {
            holderWish = wish
            binding.apply {
                if (wishesType == DETAILS)
                    seeMoreLayout.visibility = View.GONE
                else {
                    root.setOnClickListener {
                        val intent = Intent(it.context, WishDetailsActivity::class.java)
                        intent.putExtra(WISH_DETAILS_INTENT, wish)
                        it.context.startActivity(intent)
                    }
                }

                //Apply done,liked items in this wish
                for (itemMap in wish.items!!) {
                    if (likedItemsList.contains(itemMap.key))
                        itemMap.value.isLiked = true
                    if (doneItemsList.contains(itemMap.key))
                        itemMap.value.done = true
                }

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
                                        unFavoriteListener(wish)
                                    }
                                    builder.setNegativeButton(this.context.getString(R.string.cancel)) { _, _ ->
                                    }
                                    builder.create().show()
                                } else {
                                    unFavoriteListener(wish)
                                }
                            }
                        }
                    }

                categoryNameTextView.text = wish.category!![0].name?.capitalize()
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
                if (wishesType == DETAILS)
                    setWishItemsAdapter(true) //Expanded wish in details screen
                else {
                    if (wish.items!!.size <= MAX_VISIBLE_WISH_ITEMS) {
                        seeMoreTextSwitcher.visibility = View.GONE
                        arrowImageView.visibility = View.GONE
                    } else {//Expandable wish
                        expandableViewHolders[position] = this@WishViewHolder
                        setTextSwitcherFactory()
                        applySeeMoreText()
                        seeMoreLayout.setOnClickListener {
                            if (!isExpanded) {
                                expandWish(position)
                                //Collapse all other expandable wishes except for the current one expanding
                                for (viewHolderPosition in expandableViewHolders.keys) {
                                    if (viewHolderPosition != position && expandableViewHolders[viewHolderPosition]!!.isExpanded)
                                        expandableViewHolders[viewHolderPosition]!!.collapseWish()
                                }
                            } else {
                                val intent = Intent(it.context, WishDetailsActivity::class.java)
                                intent.putExtra(WISH_DETAILS_INTENT, wish)
                                it.context.startActivity(intent)
                            }
                        }
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
                seeMoreTextSwitcher.setText(
                    seeMoreTextSwitcher.context.resources.getQuantityString(
                        R.plurals.see_more_text,
                        extraWishItemsNumber,
                        extraWishItemsNumber
                    )
                )
            }
        }

        private fun expandWish(position: Int) {
            scrollListener(position)
            isExpanded = true
            setWishItemsAdapter(isExpanded)
            binding.arrowImageView.startAnimation(get90DegreesAnimation())
            binding.seeMoreTextSwitcher.setText(binding.seeMoreTextSwitcher.context.getString(R.string.go_to_details))
        }

        private fun collapseWish() {
            isExpanded = false
            setWishItemsAdapter(isExpanded)
            binding.arrowImageView.clearAnimation()
            applySeeMoreText()
        }
    }
}