package com.example.concertio.ui.main.fragments.reviews_list

import android.view.View
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.media3.ui.PlayerView
import androidx.recyclerview.widget.RecyclerView
import com.example.concertio.R
import com.example.concertio.data.reviews.ReviewWithReviewer
import com.example.concertio.extensions.initMedia
import com.example.concertio.extensions.loadProfilePicture
import com.google.android.gms.maps.model.LatLng
import com.example.concertio.storage.FileCacheManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.net.URL

class ReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val reviewerUid: TextView = itemView.findViewById(R.id.reviewer)
    val location: TextView = itemView.findViewById(R.id.review_location)
    val artist: TextView = itemView.findViewById(R.id.review_artist)
    val text: TextView = itemView.findViewById(R.id.review_text)
    val image: ImageView = itemView.findViewById(R.id.review_image)
    val video: PlayerView = itemView.findViewById(R.id.review_video)
    val profileImage: ImageView = itemView.findViewById(R.id.reviewer_image)
    val stars: RatingBar = itemView.findViewById(R.id.review_stars)

    companion object {
        fun bind(
            holder: ReviewViewHolder,
            currentReview: ReviewWithReviewer,
            onLocationClicked: ((location: LatLng, name: String) -> Unit),
            scope: CoroutineScope
        ) {
            holder.reviewerUid.text = currentReview.reviewer.name
            holder.location.text = currentReview.review.location
            holder.artist.text = currentReview.review.artist ?: "Unknown"
            holder.text.text = currentReview.review.review
            scope.launch {
                currentReview.reviewer.profilePicture?.let {
                    holder.profileImage.loadProfilePicture(
                        holder.itemView.context,
                        FileCacheManager.getFileLocalUri(URL(it)),
                    )
                }
                currentReview.review.mediaUri?.let { url ->
                    currentReview.review.mediaType?.let { type ->
                        initMedia(
                            holder.itemView.context,
                            holder.image,
                            holder.video,
                            FileCacheManager.getFileLocalUri(URL(url)),
                            type
                        )
                    }
                }
            }
            holder.stars.rating = currentReview.review.stars ?: 0F

            currentReview.review.locationCoordinate?.let { coordinate ->
                holder.location.setOnClickListener {
                    onLocationClicked(coordinate, currentReview.review.location ?: "")
                }
            }
        }
    }
}