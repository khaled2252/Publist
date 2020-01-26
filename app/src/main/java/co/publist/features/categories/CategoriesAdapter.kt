package co.publist.features.categories

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import co.publist.R
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import kotlinx.android.synthetic.main.item_category.view.*

class CategoriesAdapter(
    options: FirestoreRecyclerOptions<Category>,
    val categoriesFragment : CategoriesFragment,
    val listener: ( id : String,adding : Boolean) ->Unit
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
            category.id = snapshots.getSnapshot(position).id
            itemView.btnCategoryName.text = category.name
            itemView.btnCategoryName.addOnCheckedChangeListener { button, isChecked ->
                if (!isChecked) {
                    button.setBackgroundColor(
                        ContextCompat.getColor(
                            itemView.context,
                            R.color.gray
                        )
                    )
                    button.setTextColor(
                        ContextCompat.getColor(
                            itemView.context,
                            R.color.outerSpace
                        )
                    )
                    //unchecked
                    listener(category.id!!, false)
                } else {
                    if (categoriesFragment.viewModel.selectedCategories.size <5) {
                        button.setBackgroundColor(
                            ContextCompat.getColor(
                                itemView.context,
                                R.color.outerSpace
                            )
                        )
                        button.setTextColor(ContextCompat.getColor(itemView.context, R.color.gray))
                        //checked
                        listener(category.id!!, true)

                    } else
                        Toast.makeText(
                            itemView.context, "You can select at most 5 categories",
                            Toast.LENGTH_SHORT
                        ).show()
                }
            }
        }
    }
}