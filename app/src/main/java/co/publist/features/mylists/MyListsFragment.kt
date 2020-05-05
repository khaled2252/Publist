package co.publist.features.mylists

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.lifecycle.Observer
import co.publist.R
import co.publist.core.platform.BaseFragment
import co.publist.core.platform.ViewModelFactory
import co.publist.core.utils.Utils.Constants.EDIT_WISH_INTENT
import co.publist.core.utils.Utils.Constants.LISTS
import co.publist.features.createwish.CreateWishActivity
import co.publist.features.wishes.WishesFragment
import kotlinx.android.synthetic.main.fragment_my_lists.*
import javax.inject.Inject

class MyListsFragment : BaseFragment<MyListsViewModel>() {
    @Inject
    lateinit var viewModel: MyListsViewModel

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    override fun getBaseViewModel() = viewModel

    override fun getBaseViewModelFactory() = viewModelFactory

    lateinit var wishesFragment: WishesFragment

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_my_lists, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        wishesFragment =
            childFragmentManager.findFragmentById(R.id.wishesFragment) as WishesFragment
        setListeners()
        setObservers()
        wishesFragment.viewModel.loadWishes(LISTS)
    }

    private fun setObservers() {
        wishesFragment.viewModel.wishDeletedLiveData.observe(viewLifecycleOwner, Observer {
            wishesFragment.clearLoadedData()
            wishesFragment.viewModel.loadWishes(LISTS)
            Toast.makeText(this.context, getString(R.string.delete_wish), Toast.LENGTH_SHORT).show()
        })

        wishesFragment.viewModel.editWishLiveData.observe(viewLifecycleOwner, Observer { wish ->
            val intent = Intent(this.context, CreateWishActivity::class.java)
            intent.putExtra(EDIT_WISH_INTENT, wish)
            startActivity(intent)
        })
    }

    private fun setListeners() {
        addListBtn.setOnHoverListener { v, event ->

            true
        }
        addListBtn.setOnClickListener {
            addListBtn.startAnimation(
                AnimationUtils.loadAnimation(
                    addListBtn.context,
                    R.anim.pulsate_view
                )
            )
            startActivity(Intent(addListBtn.context, CreateWishActivity::class.java))
        }
    }
}