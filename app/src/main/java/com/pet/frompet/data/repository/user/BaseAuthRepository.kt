package com.pet.frompet.data.repository.user

import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseUser

interface BaseAuthRepository {

    suspend fun signInWithEmailPassword(email:String , password:String): FirebaseUser?

    suspend fun signUpWithEmailPassword(email: String , password: String): FirebaseUser?

    fun signOut() : FirebaseUser?

    fun getCurrentUser() : FirebaseUser?

    suspend fun deleteAccount(): Boolean

    suspend fun sendResetPassword(email : String) : Boolean
    suspend fun isAlreadyLoggedIn() : Boolean

    suspend fun signInGoogle(idToken: String) : FirebaseUser?

    suspend fun signInWithCredential(credential: AuthCredential): FirebaseUser?
}