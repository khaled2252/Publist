package co.publist.features.wishes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import co.publist.R
import co.publist.core.common.data.models.wish.Wish
import co.publist.core.platform.BaseFragment
import co.publist.core.platform.ViewModelFactory
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.fragment_wishes.*
import javax.inject.Inject


class WishesFragment(private val isPublic : Boolean) : BaseFragment<WishesViewModel>() {

    @Inject
    lateinit var viewModel: WishesViewModel

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    override fun getBaseViewModel() = viewModel

    override fun getBaseViewModelFactory() = viewModelFactory

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_wishes, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.onCreated(isPublic)
        setObservers()
    }

    private fun setObservers() {
        viewModel.wishesQueryLiveData.observe(viewLifecycleOwner , Observer {query ->
            setAdapter(query)
        })
    }

    private fun setAdapter(query : Query) {
        val options: FirestoreRecyclerOptions<Wish> =
            FirestoreRecyclerOptions.Builder<Wish>()
                .setQuery(query, Wish::class.java)
                .build()

        val adapter = WishesAdapter(options)

        adapter.setHasStableIds(true) //To avoid recycling view holders while scrolling thus removing selected colors
        adapter.startListening() //To fetch data from firestore
        wishesRecyclerView.adapter = adapter
    }

}
