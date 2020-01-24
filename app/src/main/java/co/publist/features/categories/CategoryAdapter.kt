package co.publist.features.categories

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import co.publist.R
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import kotlinx.android.synthetic.main.item_category.view.*

class CategoryAdapter(
    var data: List<Category>,
    options: FirebaseRecyclerOptions<Category>
) :
    FirebaseRecyclerAdapter<Category, CategoryAdapter.CategoryViewHolder>(options) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_category, parent, false)
        return CategoryViewHolder(view)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int, category: Category) {
        holder.bind(category, position)
    }

    inner class CategoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(
            category: Category,
            position: Int
        ) {
            itemView.btnCategoryName.text = category.en

        }
    }


}