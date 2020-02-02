package co.publist.features.editprofile

import androidx.lifecycle.MutableLiveData
import co.publist.core.data.local.LocalDataSource
import co.publist.core.data.models.User
import co.publist.core.platform.BaseViewModel
import co.publist.features.categories.data.CategoriesRepositoryInterface
import co.publist.features.editprofile.data.EditProfileRepositoryInterface
import io.reactivex.functions.Consumer
import javax.inject.Inject


class EditProfileViewModel @Inject constructor(
    private val editProfileRepository: EditProfileRepositoryInterface,
    private val categoriesRepository: CategoriesRepositoryInterface,
    private val localDataSource: LocalDataSource
) : BaseViewModel() {

    var userLiveData = MutableLiveData<User>()
    var selectedCategoriesList = MutableLiveData<ArrayList<String>>()

    fun onCreated() {
        val user = editProfileRepository.getUserInformation()
        if (user != null) {
            userLiveData.postValue(user)
            getUserSelectedCategories(user)
        }
    }

    private fun getUserSelectedCategories(user: User) {
        subscribe(categoriesRepository.getUserCategories(user.id), Consumer { list ->
            user.myCategories = list
            localDataSource.getSharedPreferences().updateUser(user)
        })
    }

}