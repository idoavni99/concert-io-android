package com.example.concertio.ui.auth

import android.content.Context
import android.net.Uri
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.example.concertio.data.users.UserModel
import com.example.concertio.data.users.UsersRepository
import com.example.concertio.room.DatabaseHolder
import com.example.concertio.storage.FileCacheManager
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class AuthService {
    private val usersRepository = UsersRepository.getInstance()

    suspend fun signOut() = withContext(Dispatchers.IO) {
        FirebaseAuth.getInstance().signOut()
        FileCacheManager.clearCacheAsync()
        DatabaseHolder.flushDB()
    }

    suspend fun signUp(
        email: String,
        password: String,
        name: String,
        profilePictureUri: Uri?
    ) = withContext(Dispatchers.IO) {
        try {
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .await().apply {
                    user?.apply {
                        val uploadedPictureUri =
                            if (profilePictureUri != null) usersRepository.uploadUserProfilePictureToFirebase(
                                profilePictureUri,
                                this.uid
                            ) else null
                        user?.updateProfile(
                            UserProfileChangeRequest.Builder().apply {
                                displayName = name
                                photoUri = uploadedPictureUri
                            }.build()
                        )
                    }
                }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun signInWithEmailAndPassword(email: String, password: String) = withContext(
        Dispatchers.IO
    ) {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .await().user
    }

    suspend fun signInWithIdToken(
        idOption: GetGoogleIdOption,
        credentialManager: CredentialManager,
        context: Context
    ) = withContext(Dispatchers.IO) {
        try {
            val credential = credentialManager.getCredential(
                request = GetCredentialRequest.Builder().addCredentialOption(idOption)
                    .build(),
                context = context
            ).credential
            when (credential) {
                is CustomCredential -> {
                    val idToken = GoogleIdTokenCredential.createFrom(credential.data)
                    FirebaseAuth.getInstance().signInWithCredential(
                        GoogleAuthProvider.getCredential(
                            idToken.idToken,
                            null
                        )
                    ).await().user
                }

                else -> null
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun saveUserToCache(user: FirebaseUser, profilePictureUrl: String? = null) =
        withContext(Dispatchers.IO) {
            usersRepository.upsertUser(
                UserModel(
                    uid = user.uid,
                    name = user.displayName ?: "",
                    email = user.email,
                    profilePicture = profilePictureUrl
                )
            )
        }

    companion object {
        private val instance = AuthService()
        fun getInstance() = instance
    }
}