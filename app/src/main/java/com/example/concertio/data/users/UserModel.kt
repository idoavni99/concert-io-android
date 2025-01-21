package com.example.concertio.data.users

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.concertio.storage.CloudStorageHolder
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@Entity(tableName = "users")
data class UserModel(
    @PrimaryKey val uid: String = "",
    @ColumnInfo("name") val name: String,
    @ColumnInfo("email") val email: String?,
    @ColumnInfo("profilePicture") val profilePicture: String? = null
) {
    fun toRemoteSourceUser(): RemoteSourceUser {
        return RemoteSourceUser(
            uid = uid,
            name = name,
            email = email,
            profilePicture = profilePicture
        )
    }
}
