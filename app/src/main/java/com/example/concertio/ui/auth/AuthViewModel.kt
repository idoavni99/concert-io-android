package com.example.concertio.ui.auth

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.concertio.data.users.UserModel
import com.example.concertio.data.users.UsersRepository
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class AuthViewModel : ViewModel() {
    private val usersRepository = UsersRepository.getInstance()
    private fun register(onFinishUi: () -> Unit) {
        viewModelScope.launch(Dispatchers.Main) {
            FirebaseAuth.getInstance().currentUser?.let {
                usersRepository.upsertUser(
                    UserModel(
                        uid = it.uid,
                        name = it.displayName ?: "",
                        email = it.email,
                        profilePicture = it.photoUrl?.toString()
                            ?: usersRepository.getUserByUid(it.uid)?.profilePicture
                    )
                )
                onFinishUi()
            }
        }
    }

    fun isUserLoggedIn(): Boolean {
        return FirebaseAuth.getInstance().currentUser != null
    }

    fun checkEmail(email: String, onExistsUi: () -> Unit, onUserDoesNotExistUi: () -> Unit) =
        viewModelScope.launch(Dispatchers.Main) {
            if (usersRepository.isUserExisting(email)) {
                onExistsUi()
            } else {
                onUserDoesNotExistUi()
            }
        }

    fun signInWithEmailPassword(
        email: String,
        password: String,
        onFinishUi: () -> Unit,
        onErrorUi: (message: String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                    .await().user?.let {
                        register(onFinishUi)
                    }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onErrorUi("Sign in failed")
                }
            }
        }
    }

    fun signUpWithEmailPassword(
        email: String,
        password: String,
        name: String,
        profilePictureUri: Uri? = null,
        onFinishUi: () -> Unit,
        onErrorUi: (message: String) -> Unit

    ) {
        viewModelScope.launch(Dispatchers.IO) {
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
                signInWithEmailPassword(email, password, onFinishUi, onErrorUi)
            } catch (e: Exception) {
                onErrorUi("Sign Up Failed")
            }
        }
    }

    fun signInWithIdToken(
        idOption: GetGoogleIdOption,
        credentialManager: CredentialManager,
        context: Context,
        onFinishUi: () -> Unit,
        onErrorUi: (message: String) -> Unit

    ) {
        viewModelScope.launch(Dispatchers.IO) {
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
                        ).await()
                        register {
                            onFinishUi()
                        }
                    }
                }
            } catch (e: Exception) {
onErrorUi("Sign In with Google failed")
            }
        }
    }
}