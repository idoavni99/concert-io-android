package com.example.concertio.ui.main

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.concertio.data.reviews.ReviewModel
import com.example.concertio.data.reviews.ReviewWithReviewer
import com.example.concertio.data.reviews.ReviewsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReviewsViewModel : ViewModel() {
    private val repository = ReviewsRepository.getInstance()

    fun deleteReviewById(id: String, onDeletedUi: () -> Unit = {}) {
        viewModelScope.launch(Dispatchers.Main) {
            repository.deleteReviewById(id)
            onDeletedUi()
        }
    }

    fun getReviews(getOnlyMyReviews: Boolean = false): LiveData<List<ReviewWithReviewer>> {
        return this.repository.getReviewsList(50, 0, getOnlyMyReviews)
    }

    fun invalidateReviews() {
        viewModelScope.launch {
            repository.loadReviewsFromRemoteSource(50, 0)
        }
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