package com.example.concertio.ui.main

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.example.concertio.data.reviews.ReviewModel
import com.example.concertio.data.reviews.ReviewWithReviewer
import com.example.concertio.data.reviews.ReviewsRepository
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

const val REVIEWS_FETCH_LIMIT = 50

class ReviewsViewModel : ViewModel() {
    private val repository = ReviewsRepository.getInstance()
    private var isLoadingReviews = false
    private lateinit var reviewsCursor: Query
    private val page = MutableLiveData(1)

    fun deleteReviewById(id: String, onDeletedUi: () -> Unit = {}) {
        viewModelScope.launch(Dispatchers.Main) {
            repository.deleteReviewById(id)
            onDeletedUi()
        }
    }

    fun getReviews(getOnlyMyReviews: Boolean = false): LiveData<List<ReviewWithReviewer>> {
        invalidateReviews()
        return this.page.switchMap {
            repository.getReviewsList(it * REVIEWS_FETCH_LIMIT, getOnlyMyReviews)
        }
    }

    fun invalidateReviews() {
        if (!isLoadingReviews) {
            viewModelScope.launch {
                isLoadingReviews = true
                if (::reviewsCursor.isInitialized) {
                    repository.advanceCursor(reviewsCursor, REVIEWS_FETCH_LIMIT)?.let {
                        reviewsCursor = it
                        page.value = page.value?.plus(1)
                    }
                } else {
                    reviewsCursor = repository.startReviewsCursor(REVIEWS_FETCH_LIMIT)
                }
                isLoadingReviews = false
            }
        }
    }

    fun onListEnd() {
        invalidateReviews()
    }

    fun invalidateReviewById(id: String) {
        viewModelScope.launch {
            repository.loadReviewFromRemoteSource(id)
        }
    }

    fun getReviewById(id: String): LiveData<ReviewWithReviewer?> {
        return this.repository.getReviewById(id)
    }

    fun saveReview(
        review: ReviewModel,
        mediaUri: Uri?,
        onCompleteUi: () -> Unit = {},
        onErrorUi: (message: String?) -> Unit = {}
    ) {
        review.validate().let {
            viewModelScope.launch(Dispatchers.Main) {
                if (it.success) {
                    val uri =
                        if (mediaUri?.scheme == "content") repository.uploadReviewMedia(
                            review.id,
                            mediaUri
                        ) else mediaUri
                    repository.saveReview(
                        review.copy(
                            mediaUri = uri.toString()
                        )
                    )
                    onCompleteUi()
                } else {
                    onErrorUi(it.message)
                }
            }
        }
    }
}