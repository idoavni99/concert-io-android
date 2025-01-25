package com.example.concertio.ui.main

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.concertio.data.reviews.ReviewsRepository
import com.example.concertio.data.users.UsersRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UserProfileViewModel : ViewModel() {
    private val usersRepository = UsersRepository.getInstance()
    private val reviewsRepository = ReviewsRepository.getInstance()

    fun observeMyProfile() =
        usersRepository.getMyUserObservable()

    fun signOut() = viewModelScope.launch {
        reviewsRepository.deleteAll()
        usersRepository.deleteAllUsers()
        FirebaseAuth.getInstance().signOut()
    }

    fun updateProfile(
        name: String,
        photoUri: Uri,
        onCompleteUi: () -> Unit
    ) =
        viewModelScope.launch(Dispatchers.Main) {
            FirebaseAuth.getInstance().currentUser?.let {
                if (photoUri.scheme != "https") {
                    val profilePictureUri = usersRepository.uploadUserProfilePictureToFirebase(
                        photoUri,
                        it.uid
                    )
                    usersRepository.updateUserDetails(name, profilePictureUri!!)
                } else {
                    usersRepository.updateUserDetails(name, photoUri)
                }
                onCompleteUi()
            }
        }

    fun updateAuth(
        email: String?,
        password: String?,
        onCompleteUi: () -> Unit
    ) = viewModelScope.launch {
        if (email == null && password == null) return@launch
        usersRepository.updateUserAuth(email, password)
        onCompleteUi()
    }
}