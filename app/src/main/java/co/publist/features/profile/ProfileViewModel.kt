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
    lateinit var selectedWish : Wish

    init {
        userLiveData.postValue(userRepository.getUser())
    }

    fun deleteSelectedWish() {
        subscribe(wishesRepository.deleteWishFromMyLists(selectedWish).doOnComplete {
            wishesRepository.deleteWishFromWishes(selectedWish)
        }, Action {
            wishDeletedLiveData.postValue(true)
        })

    }
}