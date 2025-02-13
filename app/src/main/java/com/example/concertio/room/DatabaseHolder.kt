package com.example.concertio.room

import android.content.Context
import androidx.room.Room
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object DatabaseHolder {
    private lateinit var appDatabase: AppDatabase

    fun initDatabase(context: Context) {
        appDatabase = Room.databaseBuilder(context, AppDatabase::class.java, "concert-io-db")
            .build()
    }

    fun getDatabase(): AppDatabase {
        return this.appDatabase
    }

    suspend fun flushDB() = withContext(Dispatchers.IO) {
        appDatabase.clearAllTables()
    }
}