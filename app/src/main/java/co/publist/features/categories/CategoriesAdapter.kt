package co.publist.features.categories

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import co.publist.R
import co.publist.core.common.data.models.category.CategoryAdapterItem
import co.publist.core.utils.Utils.getField
import kotlinx.android.synthetic.main.item_category.view.*
import java.util.*


class CategoriesAdapter(
    var list: ArrayList<CategoryAdapterItem>,
    val selectingListener: (category: CategoryAdapterItem) -> Unit
) :
    RecyclerView.Adapter<CategoriesAdapter.CategoryViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_category, parent, false)
        return CategoryViewHolder(view)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(list[position])
    }

    fun updateList(newList: ArrayList<CategoryAdapterItem>) {
        list = newList
        notifyDataSetChanged()
    }

    inner class CategoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(
            category: CategoryAdapterItem
        ) {
            val currentDeviceLanguage = Locale.getDefault().language
            //Use reflection to access localization property of current device language
            itemView.btnCategory.text =
                category.localizations?.getField<String>(currentDeviceLanguage)?.capitalize()

            //Highlight selected categories
            if (category.isSelected) {
                itemView.btnCategory.setBackgroundColor(
                    ContextCompat.getColor(
                        itemView.btnCategory.context,
                        R.color.outerSpace
                    )
                )
                itemView.btnCategory.setTextColor(
                    ContextCompat.getColor(
                        itemView.btnCategory.context,
                        R.color.gray
                    )
                )
            }

            //Default color for non selected categories
            else {
                itemView.btnCategory.setBackgroundColor(
                    ContextCompat.getColor(
                        itemView.btnCategory.context,
                        R.color.gray
                    )
                )
                itemView.btnCategory.setTextColor(
                    ContextCompat.getColor(
                        itemView.btnCategory.context,
                        R.color.outerSpace
                    )
                )
            }

            itemView.btnCategory.setOnClickListener {
                selectingListener(category)
            }
        }
    }
}