package com.example.concertio

import android.app.Application
import com.example.concertio.places.PlacesClientHolder
import com.example.concertio.room.DatabaseHolder

class ApplicationStarter : Application() {
    override fun onCreate() {
        DatabaseHolder.initDatabase(this)
        PlacesClientHolder.init(this)
        super.onCreate()
    }
}