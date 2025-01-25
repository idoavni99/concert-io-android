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

    suspend fun updateUserDetails(name: String, photoUri: Uri) = withContext(Dispatchers.IO) {
        FirebaseAuth.getInstance().currentUser?.run {
            updateProfile(
                UserProfileChangeRequest.Builder().apply {
                    displayName = name
                    setPhotoUri(photoUri)
                }.build()
            ).await()
            upsertUser(
                UserModel(
                    uid = uid,
                    name = name,
                    profilePicture = photoUri.toString(),
                    email = email
                )
            )
        }
    }

    suspend fun updateUserAuth(email: String?, password: String?) = withContext(Dispatchers.IO) {
        email?.let {
            FirebaseAuth.getInstance().currentUser?.verifyBeforeUpdateEmail(email)?.await()
        }
        password?.let {
            FirebaseAuth.getInstance().currentUser?.updatePassword(password)?.await()
        }
    }

    suspend fun deleteAllUsers() = withContext(Dispatchers.IO) {
        usersDao.deleteAll()
    }

    suspend fun cacheUserIfNotExisting(uid: String) = withContext(Dispatchers.IO) {
        val cachedResult = usersDao.getUserByUid(uid)
        if (cachedResult == null) {
            this@UsersRepository.getUserFromRemoteSource(uid)
        }
    }

    suspend fun cacheUsersIfNotExisting(uids: List<String>) =
        withContext(Dispatchers.IO) {
            val existingUserIds = usersDao.getExistingUserIds(uids)
            val nonExistingUserIds = uids.filter { !existingUserIds.contains(it) }
            this@UsersRepository.getUsersFromRemoteSource(nonExistingUserIds)
        }

    suspend fun getUserByUid(uid: String) = withContext(Dispatchers.IO) {
        usersDao.getUserByUid(uid)
    }

    suspend fun isUserExisting(email: String) = withContext(Dispatchers.IO) {
        firestoreHandle.whereEqualTo("email", email).get().await().size() > 0
    }

    suspend fun getUserFromRemoteSource(uid: String): UserModel? =
        withContext(Dispatchers.IO) {
            val user =
                firestoreHandle.document(uid).get().await().toObject(RemoteSourceUser::class.java)
                    ?.toUserModel()
            if (user != null) {
                usersDao.upsertAll(user)
            }
            return@withContext user
        }

    private suspend fun getUsersFromRemoteSource(
        uids: List<String>
    ): List<UserModel> =
        withContext(Dispatchers.IO) {
            val usersQuery =
                if (uids.isNotEmpty()) firestoreHandle.whereIn("uid", uids) else firestoreHandle
            val users = usersQuery.get().await().toObjects(RemoteSourceUser::class.java)
                .map { it.toUserModel() }
            if (users.isNotEmpty()) {
                usersDao.upsertAll(*users.toTypedArray())
            }
            return@withContext users
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