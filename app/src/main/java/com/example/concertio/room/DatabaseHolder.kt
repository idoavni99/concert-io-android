package com.example.concertio.room

import android.content.Context
import androidx.room.Room

object DatabaseHolder {
    private var appDatabase: AppDatabase? = null

    fun initDatabase(context: Context) {
        appDatabase = Room.databaseBuilder(context, AppDatabase::class.java, "concert-io-db")
            .build()
    }

    fun getDatabase(): AppDatabase {
        return this.appDatabase!!
    }
}