package co.publist.features.wishdetails

import co.publist.core.common.data.repositories.wish.WishesRepository
import co.publist.core.platform.BaseViewModel
import io.reactivex.functions.Action
import javax.inject.Inject

class WishDetailsViewModel @Inject constructor(
    private val wishesRepository: WishesRepository
) : BaseViewModel(){
    fun incrementOrganicSeen(wishId : String){
        subscribe(wishesRepository.incrementOrganicSeen(wishId), Action {

        })
    }

}