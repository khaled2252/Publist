package co.publist.features.wishes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.lifecycle.Observer
import co.publist.R
import co.publist.core.common.data.models.Mapper
import co.publist.core.common.data.models.wish.Wish
import co.publist.core.common.data.models.wish.WishAdapterItem
import co.publist.core.platform.BaseFragment
import co.publist.core.platform.ViewModelFactory
import co.publist.core.utils.Utils.Constants.PUBLIC
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.fragment_wishes.*
import javax.inject.Inject


class WishesFragment : BaseFragment<WishesViewModel>() {

    @Inject
    lateinit var viewModel: WishesViewModel

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    override fun getBaseViewModel() = viewModel

    override fun getBaseViewModelFactory() = viewModelFactory

    private var wishesType = -1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_wishes, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        refreshLayout.isRefreshing = true
        setObservers()
    }

    private fun setObservers() {
        viewModel.wishesType.observe(viewLifecycleOwner, Observer { type ->
            wishesType = type
            setListeners()

        })
        viewModel.wishesQueryLiveData.observe(viewLifecycleOwner, Observer { pair ->
            val query = pair.first
            val type = pair.second
            setAdapter(query, type)
        })

        viewModel.wishesListLiveData.observe(viewLifecycleOwner, Observer { list ->
            setAdapter(list)
            refreshLayout.isRefreshing = false
        })
    }

    private fun setListeners() {
        if (wishesType == PUBLIC) {
            refreshLayout.isEnabled = true
            refreshLayout.setOnRefreshListener {
                viewModel.loadData(PUBLIC)
            }
        } else
            refreshLayout.isEnabled = false
    }

    private fun setAdapter(query: Query, type: Int) {
        val options: FirestoreRecyclerOptions<Wish> =
            FirestoreRecyclerOptions.Builder<Wish>()
                .setQuery(query, Wish::class.java)
                .build()

        val adapter = WishesFirestoreAdapter(options, type, displayPlaceHolder = {
            val view = this.parentFragment?.view?.findViewById<LinearLayout>(R.id.placeHolderView)
            view?.visibility = View.VISIBLE
        }) { wish ->
            viewModel.modifyFavorite(wish, false)
        }

        adapter.startListening()
        wishesRecyclerView.adapter = adapter
    }

    private fun setAdapter(list: ArrayList<WishAdapterItem>) {
        refreshLayout.isRefreshing = false
        if (list.isNotEmpty()) {
            val adapter = WishesAdapter(list) { wish, isFavoriting ->
                viewModel.modifyFavorite(Mapper.mapToWish(wish), isFavoriting)
            }
            wishesRecyclerView.adapter = adapter
        } else {
            //todo display placeholder
        }

    }

}
