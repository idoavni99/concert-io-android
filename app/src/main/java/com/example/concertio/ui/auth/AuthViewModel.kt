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
    private val authService = AuthService.getInstance()
    private val usersRepository = UsersRepository.getInstance()

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
        viewModelScope.launch(Dispatchers.Main) {
            authService.signInWithEmailAndPassword(email, password)?.let {
                authService.saveUserToCache(
                    it, it.photoUrl?.toString()
                        ?: usersRepository.getUserByUid(it.uid)?.profilePicture
                )
                onFinishUi()
            } ?: run { onErrorUi("Sign in failed") }
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
        viewModelScope.launch(Dispatchers.Main) {
            authService.signUp(email, password, name, profilePictureUri)?.let {
                signInWithEmailPassword(email, password, onFinishUi, onErrorUi)
            } ?: run { onErrorUi("Sign up failed") }
        }
    }

    fun signInWithIdToken(
        idOption: GetGoogleIdOption,
        credentialManager: CredentialManager,
        context: Context,
        onFinishUi: () -> Unit,
        onErrorUi: (message: String) -> Unit

    ) {
        viewModelScope.launch(Dispatchers.Main) {
            authService.signInWithIdToken(idOption, credentialManager, context)?.let {
                onFinishUi()
            } ?: run { onErrorUi("Sign in with google failed") }
        }
    }
}