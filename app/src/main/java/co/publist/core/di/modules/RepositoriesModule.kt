/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package co.publist.core.di.modules


import co.publist.core.common.data.repositories.user.UserRepository
import co.publist.core.common.data.repositories.user.UserRepositoryInterface
import co.publist.core.common.data.repositories.wish.WishesRepository
import co.publist.core.common.data.repositories.wish.WishesRepositoryInterface
import co.publist.features.categories.data.CategoriesRepository
import co.publist.features.categories.data.CategoriesRepositoryInterface
import co.publist.features.home.data.HomeRepository
import co.publist.features.home.data.HomeRepositoryInterface
import co.publist.features.login.data.LoginRepository
import co.publist.features.login.data.LoginRepositoryInterface
import dagger.Binds
import dagger.Module
import javax.inject.Singleton

@Suppress("unused")
@Module
abstract class RepositoriesModule {

    @Binds
    @Singleton
    abstract fun bindLoginRepository(loginRepository: LoginRepository): LoginRepositoryInterface

    @Binds
    @Singleton
    abstract fun bindCategoriesRepository(categoriesRepository: CategoriesRepository): CategoriesRepositoryInterface

    @Binds
    @Singleton
    abstract fun bindUserRepository(userRepository: UserRepository): UserRepositoryInterface

    @Binds
    @Singleton
    abstract fun bindHomeRepository(homeRepository: HomeRepository): HomeRepositoryInterface

    @Binds
    @Singleton
    abstract fun bindWishesRepository(wishesRepository: WishesRepository): WishesRepositoryInterface

}
