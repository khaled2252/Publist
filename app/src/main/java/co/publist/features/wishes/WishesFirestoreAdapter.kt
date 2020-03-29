package co.publist.features.wishes

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import co.publist.R
import co.publist.core.common.data.models.Mapper
import co.publist.core.common.data.models.wish.Wish
import co.publist.core.utils.DataBindingAdapters.loadProfilePicture
import co.publist.core.utils.DataBindingAdapters.loadWishImage
import co.publist.core.utils.Utils.Constants.DETAILS
import co.publist.core.utils.Utils.Constants.LISTS
import co.publist.core.utils.Utils.Constants.WISH_DETAILS_INTENT
import co.publist.databinding.ItemWishBinding
import co.publist.features.wishdetails.WishDetailsActivity
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import org.ocpsoft.prettytime.PrettyTime
import java.util.*
import kotlin.collections.ArrayList


class WishesFirestoreAdapter(
    options: FirestoreRecyclerOptions<Wish>,
    val wishesType: Int,
    val displayPlaceHolder: (Boolean) -> Unit,
    val unFavoriteListener: (wish: Wish) -> Unit,
    val detailsListener: (wish: Wish) -> Unit,
    val completeListener: (itemId: String, wish: Wish, isDone: Boolean) -> Unit
) :
    FirestoreRecyclerAdapter<Wish, WishesFirestoreAdapter.WishViewHolder>(options) {
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

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onBindViewHolder(holder: WishViewHolder, position: Int, wish: Wish) {
        holder.bind(wish)
    }

    inner class WishViewHolder(private val binding: ItemWishBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(
            wish: Wish
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

            binding.wishActionImageView.apply {
                if (wishesType == LISTS) {
                    setImageResource(R.drawable.ic_dots)
                    setOnClickListener {
                        detailsListener(wish)
                    }
                } else {
                    setImageResource(R.drawable.ic_heart_active)
                    setOnClickListener {
                        if(wish.items?.values?.any {it.done==true}!!)
                        {
                            val builder = AlertDialog.Builder(this.context!!)

                            builder.setTitle(context.getString(R.string.remove_wish_title))
                            builder.setMessage(context.getString(R.string.remove_wish_message))
                            builder.setPositiveButton(this.context.getString(R.string.yes)) { _, _ ->

                                unFavoriteListener(wish)

                            }
                            builder.setNegativeButton(this.context.getString(R.string.cancel)) { _, _ ->
                            }
                            builder.create().show()
                        }
                        else
                            unFavoriteListener(wish)
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

            //Load wish data
            loadWishImage(binding.wishImageView, wish.wishPhotoURL)
            val wishItemsAdapter = WishItemsAdapter(
                Mapper.mapToWishAdapterItem(wish),
                LISTS,
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
                    completeListener(itemId, wish, isDone)
                })
            wishItemsAdapterArrayList.add(wishItemsAdapter)
            wishItemsAdapter.setHasStableIds(true)
            binding.wishItemsRecyclerView.adapter = wishItemsAdapter
        }

    }
}