package co.publist.features.categories

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import co.publist.R
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import kotlinx.android.synthetic.main.item_category.view.*

class CategoriesAdapter(
    options: FirestoreRecyclerOptions<Category>
) :
    FirestoreRecyclerAdapter<Category, CategoriesAdapter.CategoryViewHolder>(options) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_category, parent, false)
        return CategoryViewHolder(view)
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
        }
    }


}