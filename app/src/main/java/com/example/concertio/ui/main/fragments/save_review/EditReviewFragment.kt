package com.example.concertio.ui.main.fragments.save_review

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.example.concertio.R
import com.example.concertio.data.reviews.ReviewWithReviewer
import com.example.concertio.extensions.initMedia
import com.example.concertio.storage.FileCacheManager
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch
import java.net.URL

class EditReviewFragment : AddReviewFragment() {
    private val args by navArgs<EditReviewFragmentArgs>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_edit_review, container, false)
    }

    override fun setupView(view: View) {
        super.setupView(view)
        view.findViewById<MaterialButton>(R.id.upload_review_button)?.apply {
            text = resources.getText(R.string.save_changes)
            icon = null
        }
        viewModel.getReviewById(args.reviewId).observe(viewLifecycleOwner, ::setupInputFields)
    }

    private fun setupInputFields(reviewObject: ReviewWithReviewer?) {
        reviewObject?.review?.run {
            artistTextView?.setText(artist)
            locationTextView?.setText(location)
            reviewText?.setText(review)
            reviewStars?.rating = stars ?: 0F
            mediaUri?.let { url ->
                lifecycleScope.launch {
                    mediaType?.let { type ->
                        val uri = FileCacheManager.getFileLocalUri(URL(url))
                        this@EditReviewFragment.mediaUri = uri
                        this@EditReviewFragment.mediaType = mediaType
                        initMedia(requireContext(), reviewImage, reviewVideo, uri, type)
                    }
                }
            }
        } ?: {
            viewModel.invalidateReviewById(args.reviewId)
        }
    }

    override fun onReviewSaved(view: View) {
        view.findNavController()
            .navigate(EditReviewFragmentDirections.actionEditReviewFragmentToUserProfileFragment())
    }

    override fun getReviewFromInputs() = super.getReviewFromInputs().copy(id = args.reviewId)
}