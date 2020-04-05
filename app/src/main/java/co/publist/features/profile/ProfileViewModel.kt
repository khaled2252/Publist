package co.publist.features.profile

import androidx.lifecycle.MutableLiveData
import co.publist.core.common.data.models.Mapper
import co.publist.core.common.data.models.User
import co.publist.core.common.data.models.wish.WishAdapterItem
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
    val editWishLiveData = MutableLiveData<WishAdapterItem>()

    lateinit var selectedWish: WishAdapterItem

    init {
        userLiveData.postValue(userRepository.getUser())
    }

    fun deleteSelectedWish() {
        //Merge operator runs both calls in parallel (independent calls)
        subscribe(wishesRepository.deleteWishFromWishes(Mapper.mapToWish(selectedWish))
            .mergeWith(wishesRepository.deleteWishFromMyLists(Mapper.mapToWish(selectedWish)))
            , Action {
                wishDeletedLiveData.postValue(true)
            })

    }

    fun editSelectedWish() {
        editWishLiveData.postValue(selectedWish)
    }
}