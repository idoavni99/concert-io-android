package com.example.concertio

import android.app.Application
import com.example.concertio.room.DatabaseHolder

class ApplicationStarter : Application() {
    override fun onCreate() {
        DatabaseHolder.initDatabase(this)
        super.onCreate()
    }
}