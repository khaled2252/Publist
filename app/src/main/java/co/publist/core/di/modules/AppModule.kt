package co.publist.core.di.modules;

import android.content.Context
import co.publist.R
import co.publist.core.common.data.local.LocalDataSource
import co.publist.core.common.data.local.LocalDataSourceImpl
import co.publist.core.utils.Utils.Constants.ALGOLIA_API_KEY
import co.publist.core.utils.Utils.Constants.ALGOLIA_APP_ID
import com.algolia.search.saas.Client
import com.facebook.CallbackManager
import com.facebook.internal.CallbackManagerImpl
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module(includes = [ViewModelModule::class, ViewsModules::class, RepositoriesModule::class])
class AppModule {

    @Singleton
    @Provides
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Singleton
    @Provides
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    @Singleton
    @Provides
    fun provideFirebaseStorage(): FirebaseStorage {
        return FirebaseStorage.getInstance()
    }

    @Singleton
    @Provides
    fun provideGoogleSignInClient(context : Context): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(context,gso)
    }

    @Singleton
    @Provides
    fun provideCallbackManager(): CallbackManager {
        return CallbackManagerImpl()
    }

    @Singleton
    @Provides
    fun provideLoginManager(): LoginManager {
        return LoginManager.getInstance()
    }

    @Singleton
    @Provides
    fun provideLocalDataSource(context: Context): LocalDataSource {
        return LocalDataSourceImpl(context)
    }

    @Singleton
    @Provides
    fun provideFirebaseFunctions(): FirebaseFunctions {
        return FirebaseFunctions.getInstance()
    }

    @Singleton
    @Provides
    fun provideAlgoliaClient(): Client {
        return Client(ALGOLIA_APP_ID,ALGOLIA_API_KEY)
    }


}
