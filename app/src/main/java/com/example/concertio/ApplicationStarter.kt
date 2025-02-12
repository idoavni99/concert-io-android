package com.example.concertio

import android.app.Application
import com.example.concertio.places.PlacesClientHolder
import com.example.concertio.room.DatabaseHolder
import com.example.concertio.storage.FileCacheManager

class ApplicationStarter : Application() {
    override fun onCreate() {
        DatabaseHolder.initDatabase(this)
        PlacesClientHolder.init(this)
        FileCacheManager.init(this)
        super.onCreate()
    }
}