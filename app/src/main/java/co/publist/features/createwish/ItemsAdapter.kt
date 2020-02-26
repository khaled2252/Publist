package co.publist.features.createwish

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import co.publist.R
import kotlinx.android.synthetic.main.item_create_wish.view.*
import java.util.*
import kotlin.collections.ArrayList


class ItemsAdapter (private val listChangedListener: () -> Unit):
    RecyclerView.Adapter<ItemsAdapter.ItemViewHolder>() {
    private val list = ArrayList<String>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_create_wish, parent, false)
        return ItemViewHolder(itemView)
    }

    fun moveItem(fromPosition: Int, toPosition: Int) {
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                Collections.swap(list, i, i + 1)
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                Collections.swap(list, i, i - 1)
            }
        }
        notifyItemMoved(fromPosition, toPosition)
    }

    fun removeItem(position: Int) {
        list.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, list.size)
        listChangedListener()
    }

    fun addItem(item : String)
    {
        list.add(item)
        notifyDataSetChanged()
        listChangedListener()
    }

    fun getList(): java.util.ArrayList<String> {
        return list
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(list[position],position)
    }

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(
            todo: String
        ,position: Int
        ) {
            itemView.textView.text = todo
            itemView.deleteItemImageView.setOnClickListener {
                removeItem(position)
            }

        }
    }

}