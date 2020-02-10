package co.publist.features.wishes

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import co.publist.core.utils.Utils.loadProfilePicture
import co.publist.core.utils.Utils.loadWishImage
import co.publist.databinding.ItemWishBinding
import co.publist.features.wishes.data.Wish
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import org.ocpsoft.prettytime.PrettyTime
import java.util.*


class WishesAdapter(
    options: FirestoreRecyclerOptions<Wish>
) :
    FirestoreRecyclerAdapter<Wish, WishesAdapter.WishViewHolder>(options) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WishViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemWishBinding.inflate(inflater)
        binding.executePendingBindings()
        return WishViewHolder(binding)
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
            binding.categoryNameTextView.text = wish.category?.get(0)?.name
            val prettyTime = PrettyTime(Locale.getDefault())
            val date = wish.date?.toDate()
            val timeAgo = ". " + prettyTime.format(date)
            binding.timeTextView.text = timeAgo
            binding.titleTextView.text = wish.title
            loadProfilePicture(binding.profilePictureImageView, wish.creator?.imagePath)
            binding.userNameTextView.text = wish.creator?.name
            loadWishImage(binding.wishImageView, wish.wishPhotoURL)
            val todoAdapter =  TodosAdapter(wish.items!!,binding.moreTextView)
            binding.moreTextView.text = """${(wish.items!!.size - 3)} More Check Points"""

            binding.todoListRecyclerView.post {
                if(wish.items!!.size>3)
                todoAdapter.removeExtra()
            }
            binding.todoListRecyclerView.adapter =todoAdapter

        }
    }
}