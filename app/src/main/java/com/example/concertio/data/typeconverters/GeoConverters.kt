package com.example.concertio.data.typeconverters

import androidx.room.TypeConverter
import com.google.android.gms.maps.model.LatLng

class GeoConverters {
    @TypeConverter
    fun fromLatLng(value: LatLng): String {
        return "${value.latitude},${value.longitude}"
    }

    @TypeConverter
    fun toLatLng(value: String): LatLng {
        val parts = value.split(",")
        return LatLng(parts[0].toDouble(), parts[1].toDouble())
    }
}