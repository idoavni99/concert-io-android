package com.example.concertio.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.concertio.data.users.UserModel
import com.example.concertio.data.users.UsersDao

@Database(
    entities = [UserModel::class], version = 1, exportSchema = true,
    autoMigrations = []
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun usersDao(): UsersDao
}