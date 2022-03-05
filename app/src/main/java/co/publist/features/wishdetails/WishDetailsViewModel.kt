package com.publist.features.wishdetails

import com.publist.core.common.data.repositories.user.UserRepository
import com.publist.core.common.data.repositories.wish.WishesRepository
import com.publist.core.platform.BaseViewModel
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