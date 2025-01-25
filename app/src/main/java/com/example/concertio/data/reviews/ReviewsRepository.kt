package com.example.concertio.data.reviews

import android.net.Uri
import androidx.lifecycle.LiveData
import com.example.concertio.data.users.UsersRepository
import com.example.concertio.room.DatabaseHolder
import com.example.concertio.storage.CloudStorageHolder
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
        offset: Int,
        getMyReviews: Boolean = false
    ): LiveData<List<ReviewWithReviewer>> {
        return if (getMyReviews) reviewsDao.getAllMyReviewsPaginated(
            limit,
            offset,
            usersRepository.getMyUid()
        ) else reviewsDao.getAllReviewsPaginated(limit, offset)
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

    suspend fun loadReviewsFromRemoteSource(limit: Int, offset: Int) =
        withContext(Dispatchers.IO) {
            val reviews = firestoreHandle.orderBy("review").startAt(offset).limit(limit.toLong())
                .get().await().toObjects(RemoteSourceReview::class.java)
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