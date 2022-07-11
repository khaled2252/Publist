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

package com.publist.core.di.modules


import com.publist.core.common.data.repositories.user.UserRepository
import com.publist.core.common.data.repositories.user.UserRepositoryInterface
import com.publist.core.common.data.repositories.wish.WishesRepository
import com.publist.core.common.data.repositories.wish.WishesRepositoryInterface
import com.publist.features.categories.data.CategoriesRepository
import com.publist.features.categories.data.CategoriesRepositoryInterface
import com.publist.features.home.data.HomeRepository
import com.publist.features.home.data.HomeRepositoryInterface
import com.publist.features.login.data.LoginRepository
import com.publist.features.login.data.LoginRepositoryInterface
import com.publist.features.myfavorites.data.MyFavoritesRepository
import com.publist.features.myfavorites.data.MyFavoritesRepositoryInterface
import com.publist.features.mylists.data.MyListsRepository
import com.publist.features.mylists.data.MyListsRepositoryInterface
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

    @Binds
    @Singleton
    abstract fun bindMyListsRepository(myListsRepository: MyListsRepository): MyListsRepositoryInterface

    @Binds
    @Singleton
    abstract fun bindMyFavoritesRepository(myFavoritesRepository: MyFavoritesRepository): MyFavoritesRepositoryInterface

}
