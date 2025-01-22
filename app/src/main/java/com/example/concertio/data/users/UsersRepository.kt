package com.example.concertio.data.users

import android.net.Uri
import com.example.concertio.room.DatabaseHolder
import com.example.concertio.storage.CloudStorageHolder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext


class UsersRepository {
    private val usersDao = DatabaseHolder.getDatabase().usersDao()
    private val firestoreHandle = Firebase.firestore.collection("users")

    fun getMyUid() = FirebaseAuth.getInstance().currentUser!!.uid

    fun getMyUserObservable() = usersDao.getMyUserObservable(getMyUid())

    suspend fun upsertUser(user: UserModel) = withContext(Dispatchers.IO) {
        firestoreHandle.document(user.uid).set(user.toRemoteSourceUser()).await()
        usersDao.upsertAll(user)
    }

    suspend fun getUserByUid(uid: String) = withContext(Dispatchers.IO) {
        usersDao.getUserByUid(uid)
    }

    suspend fun isUserExisting(email: String) = withContext(Dispatchers.IO) {
        firestoreHandle.whereEqualTo("email", email).get().await().size() > 0
    }

    suspend fun uploadUserProfilePictureToFirebase(uri: Uri, uid: String) =
        withContext(Dispatchers.IO) {
            try {
                CloudStorageHolder.profilePictures.child("$uid-profile.png")
                    .putFile(uri).await()
                val photoUri =
                    CloudStorageHolder.profilePictures.child("$uid-profile.png").downloadUrl.await()
                FirebaseAuth.getInstance().currentUser?.updateProfile(
                    UserProfileChangeRequest.Builder().setPhotoUri(photoUri).build()
                )?.await()
                photoUri
            } catch (error: Error) {
                null
            }
        }

    companion object {
        private val instance = UsersRepository()
        fun getInstance() = instance
    }
}