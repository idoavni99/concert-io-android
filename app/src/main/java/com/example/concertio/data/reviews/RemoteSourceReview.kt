package com.example.concertio.data.reviews

import android.net.Uri
import com.google.firebase.firestore.GeoPoint
import java.sql.Timestamp

data class RemoteSourceReview(
    val artist: String? = null,
    val location_name: String? = null,
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
            review = review ?: "",
            reviewerUid = reviewer_uid ?: "",
            stars = stars,
            mediaType = media_type,
            mediaUri = media_uri,
            updatedAt = updated_at
        )
    }
}