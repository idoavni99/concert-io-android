package com.example.concertio.data.users

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert

@Dao
interface UsersDao {
    @Query("SELECT * FROM users WHERE uid LIKE :uid LIMIT 1")
    suspend fun getUserByUid(uid: String): UserModel?

    @Query("SELECT uid FROM users WHERE uid IN (:uids)")
    suspend fun getExistingUserIds(uids: List<String>): List<String>

    @Upsert
    suspend fun upsertAll(vararg users: UserModel)

    @Query("DELETE FROM users")
    suspend fun deleteAll()

    @Query("SELECT * FROM users WHERE uid = :uid")
    fun getMyUserObservable(uid: String): LiveData<UserModel?>
}