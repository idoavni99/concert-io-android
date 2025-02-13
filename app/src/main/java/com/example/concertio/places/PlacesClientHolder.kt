package com.example.concertio.places

import android.content.Context
import com.example.concertio.BuildConfig
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient

object PlacesClientHolder {
    private lateinit var client: PlacesClient
    fun init(applicationContext: Context) {
        if (!::client.isInitialized) {
            val apiKey = BuildConfig.placesApiKey
            Places.initializeWithNewPlacesApiEnabled(applicationContext, apiKey)
            client = Places.createClient(applicationContext)
        }
    }

    fun getInstance() = client
}