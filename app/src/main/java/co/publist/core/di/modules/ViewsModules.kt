package co.publist.core.di.modules

import co.publist.features.categories.CategoriesFragment
import co.publist.features.intro.IntroActivity
import co.publist.features.login.LoginActivity
import co.publist.features.splash.SplashActivity
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

}
