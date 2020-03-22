package co.publist.features.wishes

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import co.publist.R
import co.publist.core.common.data.models.wish.Wish
import co.publist.core.utils.DataBindingAdapters.loadProfilePicture
import co.publist.core.utils.DataBindingAdapters.loadWishImage
import co.publist.core.utils.Utils.Constants.LISTS
import co.publist.databinding.ItemWishBinding
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import org.ocpsoft.prettytime.PrettyTime
import java.util.*
import kotlin.collections.ArrayList


class WishesFirestoreAdapter(
    options: FirestoreRecyclerOptions<Wish>,
    val type: Int,
    val displayPlaceHolder: (Boolean) -> Unit,
    val unFavoriteListener: (wish: Wish) -> Unit,
    val detailsListener: (wish: Wish) -> Unit
) :
    FirestoreRecyclerAdapter<Wish, WishesFirestoreAdapter.WishViewHolder>(options) {
    val todosAdapterArrayList = ArrayList<TodosAdapter>()
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
            binding.wishActionImageView.apply {
                if (type == LISTS) {
                    setImageResource(R.drawable.ic_dots)
                    setOnClickListener {
                        detailsListener(wish)
                    }
                } else {
                    setImageResource(R.drawable.ic_heart_active)
                    setOnClickListener {
                        //todo confirmation dialog if user has completed item
                        unFavoriteListener(wish)
                    }
                }
            }
            binding.categoryNameTextView.text = wish.category!![0].name
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
            val todosAdapter = TodosAdapter(
                ArrayList(wish.items!!.values),
                binding.moreTextView,
                binding.arrowImageView,
                todosAdapterArrayList.size
            ) {
                //Collapse all other lists except for the current one expanding
                for (adapterIndex in 0 until todosAdapterArrayList.size) {
                    if (adapterIndex != it)
                        todosAdapterArrayList[adapterIndex].collapseExtraTodosPostLoading()
                }
            }
            todosAdapterArrayList.add(todosAdapter)
            todosAdapter.setHasStableIds(true)
            binding.todoListRecyclerView.adapter = todosAdapter
            binding.todoListRecyclerView.post {
                if (wish.items!!.size > 3)
                    todosAdapter.collapseExtraTodosPostLoading()
            }

        }
    }
}