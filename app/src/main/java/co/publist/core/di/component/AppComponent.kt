package co.publist.core.di.component

import android.app.Application
import android.content.Context
import co.publist.core.PubListApp
import co.publist.core.di.modules.AppModule
import co.publist.core.di.modules.Modules
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjectionModule
import javax.inject.Singleton

@Singleton
@Component(modules = [AndroidInjectionModule::class, AppModule::class, Modules::class])
interface AppComponent {

    @Component.Builder
    interface Builder {

        @BindsInstance
        fun application(application: Application): Builder

        @BindsInstance
        fun context(context: Context): Builder

        fun build(): AppComponent
    }

    fun inject(pubListApp: PubListApp)
}