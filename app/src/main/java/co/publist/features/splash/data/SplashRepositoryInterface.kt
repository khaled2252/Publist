package co.publist.features.splash.data

import co.publist.core.data.models.User

interface SplashRepositoryInterface {
    fun getUser() : User?
}