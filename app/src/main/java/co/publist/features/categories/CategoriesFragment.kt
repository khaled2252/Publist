package co.publist.features.categories

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import co.publist.R
import co.publist.core.data.models.Category
import co.publist.core.platform.BaseFragment
import co.publist.core.platform.ViewModelFactory
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.material.button.MaterialButton
import kotlinx.android.synthetic.main.fragment_categories.*
import javax.inject.Inject


class CategoriesFragment : BaseFragment<CategoriesViewModel>() {

    @Inject
    lateinit var viewModel: CategoriesViewModel

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    override fun getBaseViewModel() = viewModel

    override fun getBaseViewModelFactory() = viewModelFactory

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_categories, container, false)
    }

    private lateinit var lastClickedButton: MaterialButton

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setObservers()
    }

    private fun setAdapter(selectedCategoriesList : ArrayList<String>) {

        categoriesRecyclerView.layoutManager = FlexboxLayoutManager(this.context)

        val options: FirestoreRecyclerOptions<Category> =
            FirestoreRecyclerOptions.Builder<Category>()
                .setQuery(viewModel.getCategoriesQuery(), Category::class.java)
                .build()

        val adapter = CategoriesAdapter(options, selectedCategoriesList) { id, button ->
            lastClickedButton = button
            viewModel.addCategory(id)
        }

        adapter.setHasStableIds(true) //To avoid recycling view holders while scrolling thus removing selected colors
        adapter.startListening() //To fetch data from firestore
        categoriesRecyclerView.adapter = adapter
    }

    private fun setObservers() {
        viewModel.previouslySelectedCategoriesList.observe(viewLifecycleOwner, Observer {list ->
            setAdapter(list)
        })

        viewModel.selectedCategory.observe(viewLifecycleOwner, Observer { isAdding ->
            if (isAdding) {
                lastClickedButton.setBackgroundColor(
                    ContextCompat.getColor(
                        lastClickedButton.context,
                        R.color.outerSpace
                    )
                )
                lastClickedButton.setTextColor(
                    ContextCompat.getColor(
                        lastClickedButton.context,
                        R.color.gray
                    )
                )
            } else {
                lastClickedButton.setBackgroundColor(
                    ContextCompat.getColor(
                        lastClickedButton.context,
                        R.color.gray
                    )
                )
                lastClickedButton.setTextColor(
                    ContextCompat.getColor(
                        lastClickedButton.context,
                        R.color.outerSpace
                    )
                )
            }
        }

        )

        viewModel.reachedMaximumSelection.observe(viewLifecycleOwner, Observer {
            Toast.makeText(this.context, getString(R.string.maximum_categories), Toast.LENGTH_SHORT)
                .show()
        })
    }

}
