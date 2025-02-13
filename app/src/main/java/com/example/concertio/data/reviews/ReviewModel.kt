package com.example.concertio.data.reviews

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.example.concertio.data.ValidationResult
import com.example.concertio.data.users.UserModel
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.GeoPoint

@Entity(
    tableName = "reviews",
    foreignKeys = [ForeignKey(
        entity = UserModel::class,
        parentColumns = ["uid"],
        childColumns = ["reviewer_uid"]
    )]
)
data class ReviewModel(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "location") val location: String? = null,
    @ColumnInfo(name = "reviewer_uid") val reviewerUid: String,
    @ColumnInfo(name = "artist") val artist: String? = null,
    @ColumnInfo(name = "review") val review: String,
    @ColumnInfo(
        name = "updated_at",
        defaultValue = "0",
    ) val updatedAt: Long = System.currentTimeMillis(),
    @ColumnInfo(
        name = "stars",
        defaultValue = "4"
    ) val stars: Float?,
    @ColumnInfo(
        name = "media_type",
    ) val mediaType: String? = null,
    @ColumnInfo(
        name = "media_uri"
    ) val mediaUri: String? = null,
    @ColumnInfo(
        name = "location_coordinate",
        defaultValue = "NULL"
    ) val locationCoordinate: LatLng? = null
) {
    fun validate(): ValidationResult {
        try {
            require(review.isNotEmpty()) { "Review cannot be empty" }
            require(stars != null && stars in 0F..5F) { "Stars must be between 0 and 5" }
            require(location?.isNotEmpty() == true || artist?.isNotEmpty() == true) { "Must have location or artist" }
            return ValidationResult()
        } catch (e: IllegalArgumentException) {
            return ValidationResult(e)
        }
    }

    fun toRemoteSource(): RemoteSourceReview {
        return RemoteSourceReview(
            artist = artist,
            location_name = location,
            location_coordinate = locationCoordinate?.let { GeoPoint(it.latitude, it.longitude) },
            review = review,
            reviewer_uid = reviewerUid,
            id = id,
            media_type = mediaType,
            media_uri = mediaUri,
            stars = stars ?: 0F,
            updated_at = updatedAt,
        )
    }
}


