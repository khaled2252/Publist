package co.publist.features.categories

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import co.publist.R
import co.publist.core.platform.BaseFragment
import co.publist.core.platform.ViewModelFactory
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initView()
    }

    private fun initView() {

        val query = FirebaseFirestore.getInstance()
            .collection("categories")

        val options: FirestoreRecyclerOptions<Category> = FirestoreRecyclerOptions.Builder<Category>()
            .setQuery(query, Category::class.java)
            .build()

        val layoutManager = FlexboxLayoutManager(this.context)

            categoriesRecyclerView.layoutManager = layoutManager
        val adapter = CategoriesAdapter(options,this) { id, adding ->
            if (adding)
                viewModel.addSelectedCategory(id)
            else
                viewModel.removeSelectedCategory(id)

        }
        adapter.setHasStableIds(true) //To avoid recycling view holders while scrolling thus removing selected colors
        adapter.startListening()
        categoriesRecyclerView.adapter = adapter

    }

}
