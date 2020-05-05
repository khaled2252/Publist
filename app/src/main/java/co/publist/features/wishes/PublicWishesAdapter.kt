package co.publist.features.wishes

import android.content.Intent
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
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
import co.publist.core.utils.Utils.Constants.GENERAL_TYPE
import co.publist.core.utils.Utils.Constants.LOADING_MORE
import co.publist.core.utils.Utils.Constants.MAX_VISIBLE_WISH_ITEMS
import co.publist.core.utils.Utils.Constants.WISH_DETAILS_INTENT
import co.publist.core.utils.Utils.get90DegreesAnimation
import co.publist.core.utils.Utils.getUnfavoriteAnimation
import co.publist.databinding.ItemWishBinding
import co.publist.features.wishdetails.WishDetailsActivity
import org.ocpsoft.prettytime.PrettyTime
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.set

class PublicWishesAdapter(
    val wishesType: Int,
    val user: User?,
    val favoriteListener: (wish: WishAdapterItem, isFavoriting: Boolean) -> Unit,
    val detailsListener: (wish: WishAdapterItem) -> Unit,
    val completeListener: (itemId: String, wishId: String, isCreator: Boolean, isDone: Boolean) -> Unit,
    val likeListener: (itemId: String, wishId: String, isLiked: Boolean) -> Unit,
    val seenCountListener: (wishId: String) -> Unit,
    val scrollListener: (position: Int) -> Unit
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val wishList = ArrayList<WishAdapterItem?>()
    private lateinit var loadMoreView: View
    var loadMoreViewHeight = 0
    val expandableViewHolders = mutableMapOf<Int, WishViewHolder>()
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

            if (wish!!.items!!.size <= MAX_VISIBLE_WISH_ITEMS) //To avoid recycling expandable items to keep their state
                holder.setIsRecyclable(false)

            if (wish.wishId != null)//Fixme checking because of iOS bug where some wishes are without wishId
                seenCountListener(wish.wishId!!)

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
                        expandableViewHolders[position] = this@WishViewHolder
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
                }, viewHolderSpaceClickListener = {
                    val intent = Intent(
                        binding.wishItemsRecyclerView.context,
                        WishDetailsActivity::class.java
                    )
                    intent.putExtra(WISH_DETAILS_INTENT, holderWish)
                    binding.wishItemsRecyclerView.context.startActivity(intent)
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
                startAnimation(AnimationUtils.loadAnimation(context, R.anim.favorite_activate))
                wish.isFavorite = isFavoriting
                //Update remotely
                favoriteListener(wish, isFavoriting)
            } else {
                if (wish.items?.values?.any { it.done == true }!!) {
                    val builder = AlertDialog.Builder(this.context!!)

                    builder.setTitle(context.getString(R.string.remove_wish_title))
                    builder.setMessage(context.getString(R.string.remove_wish_message))
                    builder.setPositiveButton(this.context.getString(R.string.yes)) { _, _ ->
                        startAnimation(getUnfavoriteAnimation(this))
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
                    startAnimation(getUnfavoriteAnimation(this))
                    wish.isFavorite = isFavoriting
                    favoriteListener(wish, isFavoriting)
                }

            }

        }
    }

}
