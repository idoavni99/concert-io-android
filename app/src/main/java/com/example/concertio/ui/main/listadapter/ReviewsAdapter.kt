package com.example.concertio.ui.main.listadapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.concertio.R
import com.example.concertio.data.reviews.ReviewModel
import com.example.concertio.data.reviews.ReviewWithReviewer
import com.example.concertio.ui.main.fragments.reviews_list.ReviewViewHolder
import com.example.concertio.ui.main.fragments.user_profile.UserReviewViewHolder

enum class ReviewType {
    USER,
    REVIEW
}

class ReviewsAdapter(
    private val onEdit: ((ReviewModel) -> Unit)? = null,
    private val onDelete: ((ReviewModel) -> Unit)? = null,
    private val reviewType: ReviewType
) :
    RecyclerView.Adapter<ViewHolder>() {
    private val reviewDiffer = AsyncListDiffer(this, ReviewsComparator())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when (reviewType) {
            ReviewType.USER -> {
                val itemView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.user_review_list_item, parent, false)
                UserReviewViewHolder(itemView)
            }

            ReviewType.REVIEW -> {
                val itemView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.review_list_item, parent, false)
                ReviewViewHolder(itemView)
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentReview = reviewDiffer.currentList[position]
        when (holder) {
            is ReviewViewHolder -> ReviewViewHolder.bind(holder, currentReview)
            is UserReviewViewHolder -> UserReviewViewHolder.bind(
                holder,
                currentReview,
                onDeleteClick = onDelete,
                onEditClick = onEdit
            )
        }
    }

    override fun onViewDetachedFromWindow(holder: ViewHolder) {
        when (holder) {
            is ReviewViewHolder -> holder.video.player?.pause()
            is UserReviewViewHolder -> holder.video.player?.pause()
        }
        super.onViewDetachedFromWindow(holder)
    }

    fun updateReviews(newReviews: List<ReviewWithReviewer>) {
        reviewDiffer.submitList(newReviews)
    }

    fun onViewHidden(recyclerView: RecyclerView) {
        this.reviewDiffer.currentList.forEachIndexed({ index, review ->
            when (val holder = recyclerView.findViewHolderForAdapterPosition(index)) {
                is ReviewViewHolder -> holder.video.player?.release()
                is UserReviewViewHolder -> holder.video.player?.release()
            }
        })
    }

    override fun getItemCount(): Int {
        return reviewDiffer.currentList.size
    }
}