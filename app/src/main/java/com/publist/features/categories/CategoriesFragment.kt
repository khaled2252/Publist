package com.publist.features.categories

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import com.publist.R
import com.publist.core.common.data.models.category.CategoryAdapterItem
import com.publist.core.platform.BaseFragment
import com.publist.core.platform.ViewModelFactory
import com.publist.core.utils.Utils.Constants.MAXIMUM_SELECTED_CATEGORIES
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

    private lateinit var adapter: CategoriesAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setObservers()
        setAdapter(arrayListOf())
    }

    private fun setAdapter(categoriesList: ArrayList<CategoryAdapterItem>) {
        adapter = CategoriesAdapter(categoriesList) { CategoryAdapterItem ->
            viewModel.addCategory(CategoryAdapterItem)

        }
        categoriesRecyclerView.adapter = adapter
    }

    private fun setObservers() {
        viewModel.categoriesListLiveData.observe(viewLifecycleOwner, Observer { newList ->
            adapter.updateList(newList)
        })

        viewModel.reachedMaximumSelectionLiveData.observe(
            viewLifecycleOwner,
            Observer { isCreatingWish ->
                if (!isCreatingWish)
                    Toast.makeText(
                        this.context,
                        getString(R.string.maximum_categories).format(MAXIMUM_SELECTED_CATEGORIES),
                        Toast.LENGTH_SHORT
                    )
                        .show()
                else Toast.makeText(
                    this.context,
                    getString(R.string.maximum_categories_create_wish),
                    Toast.LENGTH_SHORT
                )
                    .show()
//            val toast =
//                Toast.makeText(this.context, getString(R.string.maximum_categories_create_wish), Toast.LENGTH_SHORT)
//            toast.setGravity(Gravity.BOTTOM, 0, 400)
//            toast.show()
            })
    }

}
