package com.publist.core.di.modules

import com.publist.features.categories.CategoriesFragment
import com.publist.features.createwish.CreateWishActivity
import com.publist.features.editprofile.EditProfileActivity
import com.publist.features.home.HomeActivity
import com.publist.features.intro.IntroActivity
import com.publist.features.login.LoginActivity
import com.publist.features.myfavorites.MyFavoritesFragment
import com.publist.features.mylists.MyListsFragment
import com.publist.features.onboarding.OnBoardingActivity
import com.publist.features.profile.ProfileActivity
import com.publist.features.splash.SplashActivity
import com.publist.features.wishdetails.WishDetailsActivity
import com.publist.features.wishes.WishesFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Suppress("unused")
@Module
abstract class ViewsModules {

    @ContributesAndroidInjector
    abstract fun contributeSplashActivity(): SplashActivity

    @ContributesAndroidInjector
    abstract fun contributeLoginActivity(): LoginActivity

    @ContributesAndroidInjector
    abstract fun contributeIntroActivity(): IntroActivity

    @ContributesAndroidInjector
    abstract fun contributeCategoriesFragment(): CategoriesFragment

    @ContributesAndroidInjector
    abstract fun contributeEditProfileActivity(): EditProfileActivity

    @ContributesAndroidInjector
    abstract fun contributeHomeActivity(): HomeActivity

    @ContributesAndroidInjector
    abstract fun contributeWishesActivity(): WishesFragment

    @ContributesAndroidInjector
    abstract fun contributeCreateWishActivity(): CreateWishActivity

    @ContributesAndroidInjector
    abstract fun contributeProfileActivity(): ProfileActivity

    @ContributesAndroidInjector
    abstract fun contributeMyFavoritesFragment(): MyFavoritesFragment

    @ContributesAndroidInjector
    abstract fun contributeMyListsFragment(): MyListsFragment

    @ContributesAndroidInjector
    abstract fun contributeWishDetailsActivity(): WishDetailsActivity

    @ContributesAndroidInjector
    abstract fun contributeOnBoardingActivity(): OnBoardingActivity

}
