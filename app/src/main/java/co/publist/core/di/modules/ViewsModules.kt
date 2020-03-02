package co.publist.core.di.modules

import co.publist.features.categories.CategoriesFragment
import co.publist.features.createwish.CreateWishActivity
import co.publist.features.editprofile.EditProfileActivity
import co.publist.features.home.HomeActivity
import co.publist.features.intro.IntroActivity
import co.publist.features.login.LoginActivity
import co.publist.features.profile.ProfileActivity
import co.publist.features.profile.myfavorites.MyFavoritesFragment
import co.publist.features.profile.mylists.MyListsFragment
import co.publist.features.splash.SplashActivity
import co.publist.features.wishes.WishesFragment
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

}
