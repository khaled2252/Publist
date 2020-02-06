package co.publist.features.wishes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import co.publist.R
import co.publist.core.platform.BaseFragment
import co.publist.core.platform.ViewModelFactory
import co.publist.features.wishes.data.WishView
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import kotlinx.android.synthetic.main.fragment_wishes.*
import javax.inject.Inject


class WishesFragment : BaseFragment<WishesViewModel>() {

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
        setAdapter()
        setObservers()
    }

    private fun setAdapter() {

        wishesRecyclerView.layoutManager = LinearLayoutManager(this.context)

        val options: FirestoreRecyclerOptions<WishView> =
            FirestoreRecyclerOptions.Builder<WishView>()
                .setQuery(viewModel.getWishesQuery(), WishView::class.java)
                .build()

        val adapter = WishesAdapter(options)

        adapter.setHasStableIds(true) //To avoid recycling view holders while scrolling thus removing selected colors
        adapter.startListening() //To fetch data from firestore
        wishesRecyclerView.adapter = adapter
    }

    private fun setObservers() {

    }

}
