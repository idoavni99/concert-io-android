package com.example.concertio.ui.auth

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    private fun register(onFinishUi: () -> Unit) {
        viewModelScope.launch(Dispatchers.Main) {
            FirebaseAuth.getInstance().currentUser?.let {
                onFinishUi()
            }
        }
    }

    fun signInWithIdToken(
        idOption: GetGoogleIdOption,
        credentialManager: CredentialManager,
        context: Context,
        onFinishUi: () -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
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
        }
    }
}