package co.publist.features.createwish

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.RecyclerView
import co.publist.R
import co.publist.core.common.data.models.wish.WishItem
import kotlinx.android.synthetic.main.item_create_wish.view.*
import java.util.*
import kotlin.collections.ArrayList


class ItemsAdapter(private val listChangedListener: (ArrayList<Pair<String, WishItem>>) -> Unit) :
    RecyclerView.Adapter<ItemsAdapter.ItemViewHolder>() {
    private var wishItemsMapList = ArrayList<Pair<String, WishItem>>()
    var isInEditingMode = false
    var deletedItemFromEditingPosition = -1
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.item_create_wish, parent, false)
        return ItemViewHolder(itemView)
    }

    fun moveItem(fromPosition: Int, toPosition: Int) {
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                Collections.swap(wishItemsMapList, i, i + 1)
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                Collections.swap(wishItemsMapList, i, i - 1)
            }
        }
        notifyItemMoved(fromPosition, toPosition)
        listChangedListener(wishItemsMapList)
    }

    fun removeItem(position: Int) {
        wishItemsMapList.removeAt(position)
        notifyItemRemoved(position)
        notifyDataSetChanged()
        listChangedListener(wishItemsMapList)
    }

    fun addItem(name: String) {
        val id = UUID.randomUUID().toString().toUpperCase(Locale.getDefault())
        wishItemsMapList.add(Pair(id, WishItem(name)))
        notifyItemInserted(wishItemsMapList.lastIndex)
        listChangedListener(wishItemsMapList)
    }

    fun populateOldList(oldList: ArrayList<Map.Entry<String, WishItem>>) {
        for (entry in oldList)
            wishItemsMapList.add(Pair(entry.key, entry.value))
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return wishItemsMapList.size
    }

    override fun getItemId(position: Int): Long {
        return wishItemsMapList[position].first.hashCode().toLong()
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(wishItemsMapList[position].second.name!!)
    }

    fun toggleEditingMode() {
        isInEditingMode = !isInEditingMode
        if (deletedItemFromEditingPosition == -1)
            notifyDataSetChanged()
        else
            removeItem(deletedItemFromEditingPosition)
    }

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var editedText = ""
        val oldBackground = itemView.wishItemEditText.background
        fun bind(
            wishItemText: String
        ) {
            //Note that binding will be called when editing mode is toggled thus checking for editing of the text
            when {
                editedText.isEmpty() -> itemView.wishItemEditText.setText(wishItemText) //First binding
                else -> { //Text edited
                    itemView.wishItemEditText.setText(editedText)
                    wishItemsMapList[adapterPosition].second.name = editedText
                    listChangedListener(wishItemsMapList)
                }
            }

            itemView.wishItemEditText.doAfterTextChanged { text ->
                if (text!!.isEmpty() && adapterPosition != -1) //Save current position of item if user deleted whole text
                    deletedItemFromEditingPosition = adapterPosition
                else {
                    editedText = text.toString()
                    deletedItemFromEditingPosition = -1
                }
            }

            itemView.preItemLayout.setOnClickListener {
                if (isInEditingMode)
                    removeItem(adapterPosition)
            }
            itemView.setupEditingLayout()
        }

        private fun View.setupEditingLayout() {
            if (isInEditingMode) {
                preItemImageView.setImageResource(R.drawable.ic_cross)
                reOrderItemImageView.visibility = View.VISIBLE
                wishItemEditText.isFocusableInTouchMode = true
                wishItemEditText.isEnabled = true
                wishItemEditText.isCursorVisible = true
                wishItemEditText.background = oldBackground
            } else {
                preItemImageView.setImageResource(R.drawable.rectangle_shape)
                reOrderItemImageView.visibility = View.INVISIBLE
                wishItemEditText.isFocusable = false
                wishItemEditText.isEnabled = false
                wishItemEditText.isCursorVisible = false
                wishItemEditText.setBackgroundColor(Color.TRANSPARENT)
            }
        }

    }

}