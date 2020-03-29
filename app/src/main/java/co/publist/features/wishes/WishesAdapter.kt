package co.publist.features.wishes

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import co.publist.R
import co.publist.core.common.data.models.Mapper
import co.publist.core.common.data.models.wish.WishAdapterItem
import co.publist.core.utils.DataBindingAdapters
import co.publist.core.utils.Utils.Constants.DETAILS
import co.publist.core.utils.Utils.Constants.WISH_DETAILS_INTENT
import co.publist.databinding.ItemWishBinding
import co.publist.features.wishdetails.WishDetailsActivity
import org.ocpsoft.prettytime.PrettyTime
import java.util.*
import kotlin.collections.ArrayList

class WishesAdapter(
    val list: ArrayList<WishAdapterItem>,
    val wishesType: Int,
    val favoriteListener: (wish: WishAdapterItem, isFavoriting: Boolean) -> Unit,
    val detailsListener: (wish: WishAdapterItem) -> Unit,
    val completeListener: (itemId: String, wish: WishAdapterItem, isDone: Boolean) -> Unit
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
        holder.bind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class WishViewHolder(private val binding: ItemWishBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(
            wish: WishAdapterItem
        ) {
            if (wishesType == DETAILS)
                binding.seeMoreLayout.visibility = View.GONE
            else {
                binding.root.setOnClickListener {
                    val intent = Intent(it.context, WishDetailsActivity::class.java)
                    intent.putExtra(WISH_DETAILS_INTENT, Mapper.mapToWish(wish))
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
                    if (wish.isFavorite) {
                        setImageResource(R.drawable.ic_heart_active)
                        setOnClickListener {
                            favoriteWish(wish , false)
                        }
                    } else {
                        setOnClickListener {
                            favoriteWish(wish , true)
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
            DataBindingAdapters.loadProfilePicture(
                binding.profilePictureImageView,
                wish.creator?.imagePath
            )
            binding.userNameTextView.text = wish.creator?.name

            //Load wish data
            DataBindingAdapters.loadWishImage(binding.wishImageView, wish.wishPhotoURL)
            val wishItemsAdapter = WishItemsAdapter(
                wish,
                wishesType,
                binding.seeMoreTextView,
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
                    if(!wish.isCreator && !wish.isFavorite && isDone)
                        binding.wishActionImageView.favoriteWish(wish,true)
                    completeListener(itemId, wish, isDone)
                })
            wishItemsAdapterArrayList.add(wishItemsAdapter)
            wishItemsAdapter.setHasStableIds(true)
            binding.wishItemsRecyclerView.adapter = wishItemsAdapter
        }

        private fun ImageView.favoriteWish(wish: WishAdapterItem , isFavoriting: Boolean) {
            //Update Ui then remotely
            if (isFavoriting)
            {setImageResource(R.drawable.ic_heart_active)
                wish.isFavorite = isFavoriting
                favoriteListener(wish, isFavoriting)
                notifyDataSetChanged()
                }
            else {
                if(wish.items?.values?.any {it.done==true}!!)
                {
                    val builder = AlertDialog.Builder(this.context!!)

                    builder.setTitle(context.getString(R.string.remove_wish_title))
                    builder.setMessage(context.getString(R.string.remove_wish_message))
                    builder.setPositiveButton(this.context.getString(R.string.yes)) { _, _ ->
                        setImageResource(R.drawable.ic_heart)
                        wish.isFavorite = isFavoriting
                        favoriteListener(wish, isFavoriting)
                        for (item in wish.items!!.values)
                        {
                            item.done = false
                            item.completeCount = 0
                        }
                        notifyDataSetChanged()
                    }
                    builder.setNegativeButton(this.context.getString(R.string.cancel)) { _, _ ->
                    }
                    builder.create().show()
                }
                else
                {
                    setImageResource(R.drawable.ic_heart)
                    wish.isFavorite = isFavoriting
                    favoriteListener(wish, isFavoriting)
                    notifyDataSetChanged()
                }

            }

        }
    }
}