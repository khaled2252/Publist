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


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.publist.core.di.helper.ViewModelKey
import com.publist.core.platform.ViewModelFactory
import com.publist.features.categories.CategoriesViewModel
import com.publist.features.createwish.CreateWishViewModel
import com.publist.features.editprofile.EditProfileViewModel
import com.publist.features.home.HomeViewModel
import com.publist.features.intro.IntroViewModel
import com.publist.features.myfavorites.MyFavoritesViewModel
import com.publist.features.mylists.MyListsViewModel
import com.publist.features.onboarding.OnBoardingViewModel
import com.publist.features.profile.ProfileViewModel
import com.publist.features.splash.SplashViewModel
import com.publist.features.terms.TermsViewModel
import com.publist.features.wishes.WishesViewModel
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

    @Binds
    @IntoMap
    @ViewModelKey(OnBoardingViewModel::class)
    abstract fun bindOnBoardingViewModel(onBoardingViewModel: OnBoardingViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(TermsViewModel::class)
    abstract fun bindTermsViewModel(termsViewModel: TermsViewModel): ViewModel

}
