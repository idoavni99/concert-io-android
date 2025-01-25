package com.example.concertio.ui.main.listadapter

import androidx.recyclerview.widget.DiffUtil
import com.example.concertio.data.reviews.ReviewWithReviewer

class ReviewsComparator : DiffUtil.ItemCallback<ReviewWithReviewer>() {
    override fun areItemsTheSame(
        oldItem: ReviewWithReviewer,
        newItem: ReviewWithReviewer
    ) = oldItem.review.id == newItem.review.id

    override fun areContentsTheSame(
        oldItem: ReviewWithReviewer,
        newItem: ReviewWithReviewer
    ) = oldItem.review == newItem.review

}