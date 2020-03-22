package co.publist.features.profile

import androidx.lifecycle.MutableLiveData
import co.publist.core.common.data.models.User
import co.publist.core.common.data.models.wish.Wish
import co.publist.core.common.data.repositories.user.UserRepositoryInterface
import co.publist.core.common.data.repositories.wish.WishesRepositoryInterface
import co.publist.core.platform.BaseViewModel
import io.reactivex.functions.Action
import javax.inject.Inject

class ProfileViewModel @Inject constructor(
    userRepository: UserRepositoryInterface,
    private val wishesRepository: WishesRepositoryInterface

    ) : BaseViewModel() {
    var userLiveData = MutableLiveData<User>()
    val wishDeletedLiveData = MutableLiveData<Boolean>()
    val editWishLiveData = MutableLiveData<Wish>()

    lateinit var selectedWish : Wish

    init {
        userLiveData.postValue(userRepository.getUser())
    }

    fun deleteSelectedWish() {
        //Merge operator runs both calls in parallel (independent calls)
        subscribe(wishesRepository.deleteWishFromWishes(selectedWish).mergeWith(wishesRepository.deleteWishFromMyLists(selectedWish))
            , Action {
                wishDeletedLiveData.postValue(true)
            })

    }

    fun editSelectedWish() {
        editWishLiveData.postValue(selectedWish)
    }
}