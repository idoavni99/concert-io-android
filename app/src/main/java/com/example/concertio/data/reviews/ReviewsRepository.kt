package com.example.concertio.data.reviews

import android.net.Uri
import androidx.lifecycle.LiveData
import com.example.concertio.data.users.UsersRepository
import com.example.concertio.room.DatabaseHolder
import com.example.concertio.storage.CloudStorageHolder
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

    suspend fun deleteAll() = withContext(Dispatchers.IO) {
        reviewsDao.deleteAll()
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
            val review =
                firestoreHandle.document(id).get().await().toObject(RemoteSourceReview::class.java)
                    ?.toReviewModel()
            if (review != null) {
                usersRepository.cacheUserIfNotExisting(review.reviewerUid)
                reviewsDao.upsertAll(review)
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

    private suspend fun saveReviewsFromRemoteSource(result: QuerySnapshot) = withContext(Dispatchers.IO) {
        val reviews = result.toObjects(RemoteSourceReview::class.java)
            .map { it.toReviewModel() }
        if (reviews.isNotEmpty()) {
            usersRepository.cacheUsersIfNotExisting(reviews.map { it.reviewerUid })
            reviewsDao.upsertAll(*reviews.toTypedArray())
        }
    }

    suspend fun uploadReviewMedia(reviewId: String, uri: Uri) = withContext(Dispatchers.IO) {
        CloudStorageHolder.reviewFiles.child(reviewId).putFile(uri).await()
        CloudStorageHolder.reviewFiles.child(reviewId).downloadUrl.await()
    }

    companion object {
        private val instance = ReviewsRepository()
        fun getInstance() = instance
    }
}