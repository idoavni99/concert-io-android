package com.example.concertio.data.reviews

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import com.example.concertio.data.users.UsersRepository
import com.example.concertio.places.PlacesClientHolder
import com.example.concertio.room.DatabaseHolder
import com.example.concertio.storage.CloudStorageHolder
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.kotlin.awaitFetchPlace
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ReviewsRepository {
    private val reviewsDao = DatabaseHolder.getDatabase().reviewsDao()
    private val usersRepository = UsersRepository.getInstance()
    private val firestoreHandle = Firebase.firestore.collection("reviews")

    suspend fun saveReview(review: ReviewModel) = withContext(Dispatchers.IO) {
        val remoteSourceReview = review.toRemoteSource()
        firestoreHandle.document(review.id).set(remoteSourceReview).await()
        reviewsDao.upsertAll(review)
    }

    suspend fun deleteReviewById(id: String) = withContext(Dispatchers.IO) {
        firestoreHandle.document(id).delete().await()
        reviewsDao.deleteById(id)
    }

    fun getReviewById(id: String): LiveData<ReviewWithReviewer?> {
        return reviewsDao.findById(id)
    }

    fun getReviewsList(
        limit: Int,
        getMyReviews: Boolean = false
    ): LiveData<List<ReviewWithReviewer>> {
        return if (getMyReviews) reviewsDao.getAllMyReviews(
            limit,
            usersRepository.getMyUid()
        ) else reviewsDao.getAllReviews(limit)
    }


    suspend fun loadReviewFromRemoteSource(id: String) =
        withContext(Dispatchers.IO) {
            try {
                val review =
                    firestoreHandle.document(id).get().await()
                        .toObject(RemoteSourceReview::class.java)
                        ?.toReviewModel()
                if (review != null) {
                    usersRepository.cacheUserIfNotExisting(review.reviewerUid)
                    reviewsDao.upsertAll(review)
                }
            } catch (e: Exception) {
                Log.e("ReviewsRepository", e.toString())
            }
            return@withContext reviewsDao.findById(id)
        }

    suspend fun startReviewsCursor(limit: Int) = this.advanceCursor(
        firestoreHandle.orderBy("updated_at"),
        limit
    ) ?: firestoreHandle.orderBy("updated_at").limit(limit.toLong())

    suspend fun advanceCursor(query: Query, limit: Int) = withContext(Dispatchers.IO) {
        val reviewsResult = query.limit(limit.toLong()).get().await()
        val lastVisibleReview = reviewsResult.documents.getOrNull(reviewsResult.documents.lastIndex)
        lastVisibleReview?.let {
            saveReviewsFromRemoteSource(reviewsResult)
            query.startAfter(lastVisibleReview).limit(limit.toLong())
        }
    }

    private suspend fun saveReviewsFromRemoteSource(result: QuerySnapshot): Unit =
        withContext(Dispatchers.IO) {
            try {
                val reviews = result.toObjects(RemoteSourceReview::class.java)
                    .map { it.toReviewModel() }
                if (reviews.isNotEmpty()) {
                    usersRepository.cacheUsersIfNotExisting(reviews.map { it.reviewerUid })
                    reviewsDao.upsertAll(*reviews.toTypedArray())
                }
            } catch (e: Exception) {
                Log.e("ReviewsRepository", e.toString(), e)
            }
        }

    suspend fun uploadReviewMedia(reviewId: String, uri: Uri): Uri? = withContext(Dispatchers.IO) {
        try {
            CloudStorageHolder.reviewFiles.child(reviewId).putFile(uri).await()
            CloudStorageHolder.reviewFiles.child(reviewId).downloadUrl.await()
        } catch (e: Exception) {
            Log.e("ReviewsRepository", e.toString(), e)
            null
        }

    }

    suspend fun getCoordinateByPlaceId(placeId: String): LatLng? = withContext(Dispatchers.IO) {
        PlacesClientHolder.getInstance()
            .awaitFetchPlace(placeId, listOf(Place.Field.LOCATION)).place.location
    }

    suspend fun searchReviews(query: String) = withContext(Dispatchers.IO) {
        val resultsFromFirestore = firestoreHandle.orderBy("artist").orderBy("location").where(
            Filter.or(
                Filter.lessThanOrEqualTo("artist", query),
                Filter.lessThanOrEqualTo("location", query),
                Filter.greaterThanOrEqualTo("artist", query),
                Filter.greaterThanOrEqualTo("location", query),
            )
        ).get().await()
        saveReviewsFromRemoteSource(resultsFromFirestore)
        reviewsDao.searchReviews("%${query}%")
    }

    suspend fun removeAllFromDB() = withContext(Dispatchers.IO) {
        reviewsDao.deleteAll()
    }

    companion object {
        private val instance = ReviewsRepository()
        fun getInstance() = instance
    }
}