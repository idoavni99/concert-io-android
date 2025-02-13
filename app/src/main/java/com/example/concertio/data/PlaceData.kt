package com.example.concertio.data

data class PlaceData(val name: String, val placeId: String) {
    override fun toString(): String {
        return name;
    }
}
