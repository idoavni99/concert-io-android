package com.example.concertio.storage

import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

object CloudStorageHolder {
    private val cloudStorage = Firebase.storage
    val reviewFiles = cloudStorage.reference.child("review_files")
    val profilePictures = cloudStorage.reference.child("profile_pictures")
}