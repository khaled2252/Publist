package co.publist.features.login

import co.publist.core.platform.BaseViewModel
import co.publist.features.login.data.LoginRepository
import co.publist.features.login.data.LoginRepositoryInterface
import javax.inject.Inject

class LoginViewModel @Inject constructor(private val loginRepository: LoginRepository): BaseViewModel(){

    fun gets()
    {
        loginRepository.printa()
    }
}