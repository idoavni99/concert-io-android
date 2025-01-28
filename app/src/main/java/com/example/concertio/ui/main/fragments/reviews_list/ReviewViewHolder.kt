package com.example.concertio.ui.main.fragments.reviews_list

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
import com.example.concertio.extensions.loadProfilePicture
import com.google.android.gms.maps.model.LatLng

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
            onLocationClicked: ((location: LatLng, name: String) -> Unit)
        ) {
            holder.reviewerUid.text = currentReview.reviewer.name
            holder.location.text = currentReview.review.location
            holder.artist.text = currentReview.review.artist
            holder.text.text = currentReview.review.review
            currentReview.reviewer.profilePicture?.let {
                holder.profileImage.loadProfilePicture(
                    holder.itemView.context,
                    Uri.parse(it),
                    R.drawable.empty_profile_picture
                )
            }
            currentReview.review.mediaUri?.let { uri ->
                currentReview.review.mediaType?.let { type ->
                    initMedia(
                        holder.itemView.context,
                        holder.image,
                        holder.video,
                        Uri.parse(uri),
                        type
                    )
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