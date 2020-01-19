package co.publist.core.di.modules;

import dagger.Module;

@Module(includes = [ViewModelModule::class, ViewsModules::class, RepositoriesModule::class])
class AppModule {

}
