package com.example.concertio.ui.main

import android.location.Location
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.example.concertio.data.PlaceData
import com.example.concertio.data.reviews.ReviewModel
import com.example.concertio.data.reviews.ReviewWithReviewer
import com.example.concertio.data.reviews.ReviewsRepository
import com.example.concertio.places.PlacesClientHolder
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.Task
import com.google.android.libraries.places.api.model.CircularBounds
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

const val REVIEWS_FETCH_LIMIT = 50

class ReviewsViewModel : ViewModel() {
    private val repository = ReviewsRepository.getInstance()
    private var isLoadingReviews = false
    private var prevSearchJob: Job? = null
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

    fun searchReviews(searchQuery: String, onReviewsFound: (List<ReviewWithReviewer>) -> Unit) {
        prevSearchJob = viewModelScope.launch(Dispatchers.Main) {
            val results = repository.searchReviews(searchQuery)
            onReviewsFound(results)
        }
    }

    fun cancelRunningSearch() {
        prevSearchJob?.cancel("Search canceled")
        prevSearchJob = null
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
        placeId: String?,
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
                    val locationCoordinate =
                        if (placeId != null) repository.getCoordinateByPlaceId(placeId) else null
                    repository.saveReview(
                        review.copy(
                            mediaUri = uri.toString(),
                            locationCoordinate = locationCoordinate
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