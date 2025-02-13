package com.example.concertio.data.reviews

import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.GeoPoint

data class RemoteSourceReview(
    val artist: String? = null,
    val location_name: String? = null,
    val location_coordinate: GeoPoint? = null,
    val review: String? = null,
    val reviewer_uid: String? = null,
    val media_type: String? = null,
    val media_uri: String? = null,
    val id: String? = null,
    val updated_at: Long = System.currentTimeMillis(),
    val stars: Float = 4F
) {
    fun toReviewModel(): ReviewModel {
        return ReviewModel(
            id = id ?: "",
            artist = artist,
            location = location_name,
            locationCoordinate = location_coordinate?.let { LatLng(it.latitude, it.longitude) },
            review = review ?: "",
            reviewerUid = reviewer_uid ?: "",
            stars = stars,
            mediaType = media_type,
            mediaUri = media_uri,
            updatedAt = updated_at
        )
    }
}