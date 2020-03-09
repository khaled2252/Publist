package co.publist.features.wishes

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import co.publist.R
import co.publist.core.common.data.models.wish.WishAdapterItem
import co.publist.core.utils.DataBindingAdapters
import co.publist.databinding.ItemWishBinding
import org.ocpsoft.prettytime.PrettyTime
import java.util.*
import kotlin.collections.ArrayList

class WishesAdapter(
    val list: ArrayList<WishAdapterItem>,
    val listener: (wish: WishAdapterItem, isFavoriting: Boolean) -> Unit
) :
    RecyclerView.Adapter<WishesAdapter.WishViewHolder>() {
    val todosAdapterArrayList = ArrayList<TodosAdapter>()
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
            if (wish.isCreator)
                binding.wishActionImageView.apply {
                    setImageResource(R.drawable.ic_dots)
                    setOnClickListener {
                        //todo open edit wish dialog
                    }
                }
            else
                binding.wishActionImageView.apply {
                    if (wish.isFavorite) {
                        setImageResource(R.drawable.ic_heart_active)
                        setOnClickListener {
                            setImageResource(R.drawable.ic_heart)
                            wish.isFavorite = false
                            listener(wish, false) //unFavorite
                            notifyDataSetChanged()
                        }
                    } else {
                        setOnClickListener {
                            setImageResource(R.drawable.ic_heart_active)
                            wish.isFavorite = true
                            listener(wish, true) //favorite
                            notifyDataSetChanged()
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
            DataBindingAdapters.loadProfilePicture(
                binding.profilePictureImageView,
                wish.creator?.imagePath
            )
            binding.userNameTextView.text = wish.creator?.name

            //Load wish data
            DataBindingAdapters.loadWishImage(binding.wishImageView, wish.wishPhotoURL)
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