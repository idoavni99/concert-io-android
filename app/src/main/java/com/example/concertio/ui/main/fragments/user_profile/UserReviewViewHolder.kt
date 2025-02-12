package com.example.concertio.ui.main.fragments.user_profile

import android.net.Uri
import android.view.View
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import android.widget.VideoView
import androidx.media3.ui.PlayerView
import androidx.recyclerview.widget.RecyclerView
import com.example.concertio.R
import com.example.concertio.data.reviews.ReviewModel
import com.example.concertio.data.reviews.ReviewWithReviewer
import com.example.concertio.extensions.initMedia
import com.example.concertio.extensions.showProgress
import com.example.concertio.extensions.stopProgress
import com.google.android.gms.maps.model.LatLng
import com.example.concertio.storage.FileCacheManager
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.net.URL

class UserReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val reviewLocation: TextView = itemView.findViewById(R.id.review_location)
    val reviewArtist: TextView = itemView.findViewById(R.id.review_artist)
    val reviewText: TextView = itemView.findViewById(R.id.review_text)
    val stars: RatingBar = itemView.findViewById(R.id.review_stars)
    val image: ImageView = itemView.findViewById(R.id.review_image)
    val video: PlayerView = itemView.findViewById(R.id.review_video)
    val editButton: MaterialButton = itemView.findViewById(R.id.edit_review_button)
    val deleteButton: MaterialButton = itemView.findViewById(R.id.delete_review_button)

    companion object {
        fun bind(
            holder: UserReviewViewHolder,
            currentReview: ReviewWithReviewer,
            onLocationClicked: ((location: LatLng, name: String) -> Unit),
            scope: CoroutineScope,
            onEditClick: ((review: ReviewModel) -> Unit)?,
            onDeleteClick: ((review: ReviewModel) -> Unit)?
        ) {
            holder.reviewLocation.text = currentReview.review.location
            holder.reviewArtist.text = currentReview.review.artist ?: "Unknown"
            holder.reviewText.text = currentReview.review.review
            holder.stars.rating = currentReview.review.stars ?: 0F

            scope.launch {
                currentReview.review.mediaUri?.let { uri ->
                    currentReview.review.mediaType?.let { type ->
                        initMedia(
                            holder.itemView.context,
                            holder.image,
                            holder.video,
                            FileCacheManager.getFileLocalUri(URL(uri)),
                            type
                        )
                    }
                }
            }

            holder.editButton.setOnClickListener {
                onEditClick?.invoke(currentReview.review)
            }

            holder.deleteButton.setOnClickListener {
                holder.deleteButton.showProgress()
                onDeleteClick?.invoke(currentReview.review)
            }

            currentReview.review.locationCoordinate?.let { coordinate ->
                holder.reviewLocation.setOnClickListener {
                    onLocationClicked(coordinate, currentReview.review.location ?: "")
                }
            }
        }
    }
}