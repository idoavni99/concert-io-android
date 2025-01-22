package com.example.concertio.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.concertio.data.reviews.ReviewModel
import com.example.concertio.data.reviews.ReviewsDao
import com.example.concertio.data.users.UserModel
import com.example.concertio.data.users.UsersDao

@Database(
    entities = [ReviewModel::class, UserModel::class], version = 1, exportSchema = true,
    autoMigrations = []
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun reviewsDao(): ReviewsDao
    abstract fun usersDao(): UsersDao
}