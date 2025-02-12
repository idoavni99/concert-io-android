package com.example.concertio.ui.main.fragments.save_review

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.media3.ui.PlayerView
import androidx.navigation.findNavController
import com.example.concertio.R
import com.example.concertio.data.PlaceData
import com.example.concertio.ui.main.ReviewsViewModel
import com.example.concertio.data.reviews.ReviewModel
import com.example.concertio.extensions.FileUploadingFragment
import com.example.concertio.extensions.initMedia
import com.example.concertio.extensions.showProgress
import com.example.concertio.extensions.stopProgress
import com.example.concertio.ui.main.fragments.save_review.adapter.PlacesAdapter
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationServices.getFusedLocationProviderClient
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID

open class AddReviewFragment : FileUploadingFragment() {
    var mediaType: String? = null
    var mediaUri: Uri? = null
    private var chooseMediaButtonIcon: Drawable? = null
    protected val viewModel: ReviewsViewModel by activityViewModels()
    protected val reviewImage by lazy { view?.findViewById<ImageView>(R.id.review_image) }
    protected val reviewVideo by lazy { view?.findViewById<PlayerView>(R.id.review_video) }
    protected val chooseMediaButton by lazy { view?.findViewById<MaterialButton>(R.id.choose_media) }
    protected val artistTextView by lazy { view?.findViewById<EditText>(R.id.review_artist) }
    protected val locationTextView by lazy { view?.findViewById<AutoCompleteTextView>(R.id.review_location) }
    protected val reviewText by lazy { view?.findViewById<EditText>(R.id.save_review_text) }
    protected val reviewStars by lazy { view?.findViewById<RatingBar>(R.id.save_review_stars) }
    protected val saveButton by lazy { view?.findViewById<MaterialButton>(R.id.upload_review_button) }
    protected var reviewerUid: String = FirebaseAuth.getInstance().currentUser!!.uid
    protected var selectedLocationId: String? = null

    private val selectMediaLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let {
                    mediaUri = it
                    mediaType =
                        context?.run {
                            contentResolver.getType(it)?.split("/")?.get(0)?.let { type ->
                                initMedia(this, reviewImage, reviewVideo, it, type)
                                type
                            }
                        }
                }
            }

            chooseMediaButton?.stopProgress(this.chooseMediaButtonIcon)
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_review, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupView(view)
        super.onViewCreated(view, savedInstanceState)
        activity?.let {
            setupLocationAutocomplete(it)
        }
    }

    protected open fun setupView(view: View) {
        setupActions(view)
    }

    private fun setupLocationAutocomplete(activity: Activity) {
        if (ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                0
            )
        }
        locationTextView?.apply {
            setAdapter(PlacesAdapter(context, lifecycleScope))
            setOnItemClickListener { adapterView, view, i, l ->
                val placeData = adapterView.getItemAtPosition(i) as PlaceData
                selectedLocationId = placeData.placeId
                locationTextView?.setText(placeData.name)
            }
        }
    }

    private fun setupActions(view: View) {
        saveButton?.setOnClickListener {
            val oldIcon = saveButton?.showProgress()
            val reviewData = getReviewFromInputs()
            viewModel.saveReview(reviewData, selectedLocationId, mediaUri,
                onCompleteUi = {
                    this.onReviewSaved(view)
                },
                onErrorUi = {
                    saveButton?.stopProgress(
                        oldIcon
                    )
                    Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                }
            )
        }

        chooseMediaButton?.setOnClickListener {
            this.chooseMediaButtonIcon = chooseMediaButton?.showProgress()
            requestFileAccess()
        }
    }

    override fun onFileAccessGranted() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "*/*"
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/*", "video/*"))
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        selectMediaLauncher.launch(intent)
    }

    open fun onReviewSaved(view: View) {
        view.findNavController()
            .navigate(AddReviewFragmentDirections.actionAddReviewFragmentToReviewsListFragment())
    }

    open fun getReviewFromInputs() = ReviewModel(
        location = locationTextView?.text.toString(),
        artist = artistTextView?.text.toString(),
        review = reviewText?.text.toString(),
        reviewerUid = reviewerUid,
        id = UUID.randomUUID().toString(),
        stars = reviewStars?.rating,
        mediaType = mediaType,
    )
}