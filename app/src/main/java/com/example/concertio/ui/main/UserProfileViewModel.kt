package com.example.concertio.ui.main

import androidx.lifecycle.ViewModel
import com.example.concertio.data.users.UsersRepository

class UserProfileViewModel : ViewModel() {
    private val usersRepository = UsersRepository.getInstance()

    fun observeMyProfile() =
        usersRepository.getMyUserObservable()
}