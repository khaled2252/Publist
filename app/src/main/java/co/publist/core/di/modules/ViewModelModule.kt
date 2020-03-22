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


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import co.publist.core.di.helper.ViewModelKey
import co.publist.core.platform.ViewModelFactory
import co.publist.features.categories.CategoriesViewModel
import co.publist.features.createwish.CreateWishViewModel
import co.publist.features.editprofile.EditProfileViewModel
import co.publist.features.home.HomeViewModel
import co.publist.features.intro.IntroViewModel
import co.publist.features.profile.ProfileViewModel
import co.publist.features.myfavorites.MyFavoritesViewModel
import co.publist.features.mylists.MyListsViewModel
import co.publist.features.splash.SplashViewModel
import co.publist.features.wishes.WishesViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Suppress("unused")
@Module
abstract class ViewModelModule {
    @Binds
    abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory

    @Binds
    @IntoMap
    @ViewModelKey(SplashViewModel::class)
    abstract fun bindSplashViewModel(splashViewModel: SplashViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(IntroViewModel::class)
    abstract fun bindIntroViewModel(introViewModel: IntroViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CategoriesViewModel::class)
    abstract fun bindCategoriesViewModel(categoriesViewModel: CategoriesViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(EditProfileViewModel::class)
    abstract fun bindEditProfileViewModel(editProfileViewModel: EditProfileViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(HomeViewModel::class)
    abstract fun bindHomeViewModel(homeViewModel: HomeViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(WishesViewModel::class)
    abstract fun bindWishesViewModel(wishesViewModel: WishesViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CreateWishViewModel::class)
    abstract fun bindCreateWishViewModel(createWishViewModel: CreateWishViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ProfileViewModel::class)
    abstract fun bindProfileViewModel(profileViewModel: ProfileViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MyFavoritesViewModel::class)
    abstract fun bindMyFavoritesViewModel(myFavoritesViewModel: MyFavoritesViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MyListsViewModel::class)
    abstract fun bindListsViewModel(myListsViewModel: MyListsViewModel): ViewModel

}
