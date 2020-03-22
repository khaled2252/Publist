package co.publist.features.profile

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import co.publist.features.myfavorites.MyFavoritesFragment
import co.publist.features.mylists.MyListsFragment

class ProfilePagerAdapter(manager: FragmentManager, lifecycle: Lifecycle) :
    FragmentStateAdapter(manager, lifecycle) {

    private val fragmentCount = 2

    override fun getItemCount(): Int {
        return fragmentCount
    }

    override fun createFragment(position: Int): Fragment {
        when (position) {
            0 -> return MyFavoritesFragment()
        }
        return MyListsFragment()

    }

}
