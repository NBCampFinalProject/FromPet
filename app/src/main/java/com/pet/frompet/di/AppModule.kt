package com.pet.frompet.di

import android.app.Application
import com.pet.frompet.R
import com.pet.frompet.data.repository.user.AuthRepository
import com.pet.frompet.data.repository.user.BaseAuthRepository
import com.pet.frompet.data.repository.firebase.BaseAuthenticator
import com.pet.frompet.data.repository.firebase.FirebaseAuthenticator
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.gun0912.tedpermission.provider.TedPermissionProvider.context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Singleton
    @Provides
    fun provideAuthenticator(): BaseAuthenticator {
        return FirebaseAuthenticator()
    }
    @Singleton
    @Provides
    fun provideRepository(
        authenticator: BaseAuthenticator,
        firestore: FirebaseFirestore
    ): BaseAuthRepository {
        return AuthRepository(authenticator, firestore)
    }

    @Singleton
    @Provides
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Singleton
    @Provides
    fun provideMemberInfoViewModel(): FirebaseFirestore {
        val firestore = FirebaseFirestore.getInstance()
        val settings = firestore.firestoreSettings
        firestore.firestoreSettings = settings
        return firestore
    }

    @Singleton
    @Provides
    fun provideGoogleSignInOptions(): GoogleSignInOptions {
        return GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.web_client_id))
            .requestEmail()
            .build()
    }

    @Singleton
    @Provides
    fun provideGoogleSignInClient(application: Application, googleSignInOptions: GoogleSignInOptions): GoogleSignInClient {
        return GoogleSignIn.getClient(application, googleSignInOptions)
    }

}