package co.publist.features.wishdetails

import co.publist.core.common.data.repositories.user.UserRepository
import co.publist.core.common.data.repositories.wish.WishesRepository
import co.publist.core.platform.BaseViewModel
import io.reactivex.functions.Action
import javax.inject.Inject

class WishDetailsViewModel @Inject constructor(
    private val wishesRepository: WishesRepository,
    private val userRepository: UserRepository
) : BaseViewModel() {
    fun incrementOrganicSeen(wishId: String) {
        if (userRepository.getUser() != null)
            subscribe(wishesRepository.incrementOrganicSeen(wishId), Action {
            })
    }

}