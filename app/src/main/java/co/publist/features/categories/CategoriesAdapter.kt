package co.publist.features.categories

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import co.publist.R
import co.publist.core.common.data.models.Category
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.button.MaterialButton
import kotlinx.android.synthetic.main.item_category.view.*


class CategoriesAdapter(
    options: FirestoreRecyclerOptions<Category>,
    val previouslySelectedCategoriesList : ArrayList<String>,
    val listener: (documentId : String?, buttonId : MaterialButton) ->Unit
) :
    FirestoreRecyclerAdapter<Category, CategoriesAdapter.CategoryViewHolder>(options) {
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
    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int, category: Category) {
        holder.bind(category, position)
    }

    inner class CategoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(
            category: Category,
            position: Int
        ) {
            itemView.btnCategoryName.text = category.name
            val documentId = snapshots.getSnapshot(position).id

            //Highlight previously selected categories
            if(previouslySelectedCategoriesList.contains(documentId))
            {
                itemView.btnCategoryName.setBackgroundColor(
                    ContextCompat.getColor(
                        itemView.btnCategoryName.context,
                        R.color.outerSpace
                    )
                )
                itemView.btnCategoryName.setTextColor(
                    ContextCompat.getColor(
                        itemView.btnCategoryName.context,
                        R.color.gray
                    )
                )
            }

            itemView.btnCategoryName.setOnClickListener {
                listener(documentId,itemView.btnCategoryName)
            }
        }
    }
}