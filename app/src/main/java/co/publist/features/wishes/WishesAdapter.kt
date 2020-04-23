package co.publist.features.wishes

import android.content.Intent
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import co.publist.R
import co.publist.core.common.data.models.User
import co.publist.core.common.data.models.wish.WishAdapterItem
import co.publist.core.utils.DataBindingAdapters
import co.publist.core.utils.Utils
import co.publist.core.utils.Utils.Constants.DETAILS
import co.publist.core.utils.Utils.Constants.MAX_VISIBLE_WISH_ITEMS
import co.publist.core.utils.Utils.Constants.NOT_EXPANDABLE
import co.publist.core.utils.Utils.Constants.WISH_DETAILS_INTENT
import co.publist.core.utils.Utils.get90DegreesAnimation
import co.publist.databinding.ItemWishBinding
import co.publist.features.wishdetails.WishDetailsActivity
import org.ocpsoft.prettytime.PrettyTime
import java.util.*
import kotlin.collections.set

class WishesAdapter(
    val list: ArrayList<WishAdapterItem>,
    val wishesType: Int,
    val user: User?,
    val favoriteListener: (wish: WishAdapterItem, isFavoriting: Boolean) -> Unit,
    val detailsListener: (wish: WishAdapterItem) -> Unit,
    val completeListener: (itemId: String, wishId: String, isCreator: Boolean, isDone: Boolean) -> Unit,
    val likeListener: (itemId: String, wishId: String, isLiked: Boolean) -> Unit,
    val seenCountListener: (wishId: String) -> Unit,
    val scrollListener: (position: Int) -> Unit
) :
    RecyclerView.Adapter<WishesAdapter.WishViewHolder>() {
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

    override fun onBindViewHolder(holder: WishViewHolder, position: Int) {
        val wish = list[position]

        if (wish.wishId != null)//Fixme checking because of iOS bug where some wishes are without wishId
            seenCountListener(wish.wishId!!)

        if (!expandableViewHolders.containsKey(position)) //To avoid binding expandable items
            holder.bind(wish, position)

    }

    override fun getItemCount(): Int {
        return list.size
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

                if (wish.isCreator)
                    wishActionImageView.apply {
                        setImageResource(R.drawable.ic_dots)
                        setOnClickListener {
                            detailsListener(wish)
                        }
                    }
                else
                    wishActionImageView.apply {
                        if (wish.isFavorite)
                            setImageResource(R.drawable.ic_heart_active)
                        else
                            setImageResource(R.drawable.ic_heart)

                        setOnClickListener {
                            if (user == null)
                                Utils.showLoginPromptForGuest(it.context)
                            else
                                favoriteWish(
                                    wish,
                                    position,
                                    isFavoriting = !wish.isFavorite
                                ) //Taking the opposite of current state
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
                wish.items = wish.items?.toList()?.sortedBy { it.second.orderId }
                    ?.toMap()
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
                    //completed item's wish is added to favorites according to business
                    if (!holderWish.isCreator && !holderWish.isFavorite && isDone)
                        binding.wishActionImageView.favoriteWish(holderWish, adapterPosition, true)

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
