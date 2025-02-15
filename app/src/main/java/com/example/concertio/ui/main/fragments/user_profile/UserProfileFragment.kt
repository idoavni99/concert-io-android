package com.example.concertio.ui.main.fragments.user_profile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toolbar
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import com.example.concertio.R
import com.example.concertio.extensions.loadProfilePicture
import com.example.concertio.storage.FileCacheManager
import com.example.concertio.ui.auth.AuthActivity
import com.example.concertio.ui.main.ReviewsScrollListener
import com.example.concertio.ui.main.ReviewsViewModel
import com.example.concertio.ui.main.listadapter.ReviewType
import com.example.concertio.ui.main.listadapter.ReviewsAdapter
import com.example.concertio.ui.main.UserProfileViewModel
import kotlinx.coroutines.launch
import java.net.URL

class UserProfileFragment : Fragment() {
    private val userProfileViewModel: UserProfileViewModel by activityViewModels()
    private val reviewsViewModel: ReviewsViewModel by viewModels()
    private lateinit var reviewsList: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_user_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.findViewById<Toolbar>(R.id.user_profile_toolbar)?.apply {
            inflateMenu(R.menu.user_profile_toolbar_menu)
            overflowIcon?.setTint(view.resources.getColor(R.color.black))
        }
        setupFields(view)
        setupMyReviewsList(view)
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroyView() {
        reviewsList.run {
            (adapter as? ReviewsAdapter)?.onViewHidden(this)
        }
        super.onDestroyView()
    }

    private fun setupFields(view: View) {
        userProfileViewModel.observeMyProfile().observe(viewLifecycleOwner) { user ->
            if (user == null) {
                activity?.run {
                    startActivity(Intent(this, AuthActivity::class.java))
                    finish()
                }
            } else {
                user.run {
                    view.findViewById<Toolbar>(R.id.user_profile_toolbar)?.apply {
                        findViewById<TextView>(R.id.user_profile_name)?.text = name
                        profilePicture?.let { url ->
                            findViewById<ImageView>(R.id.user_profile_picture)?.let {
                                lifecycleScope.launch {
                                    it.loadProfilePicture(
                                        this@UserProfileFragment.requireContext(),
                                        FileCacheManager.getFileLocalUri(URL(url))
                                    )
                                }
                            }
                        }
                        menu.findItem(R.id.signOut)?.setOnMenuItemClickListener {
                            userProfileViewModel.signOut()
                            true
                        }

                        menu.findItem(R.id.editProfile).setOnMenuItemClickListener {
                            findNavController().navigate(UserProfileFragmentDirections.actionUserProfileFragmentToSettingsFragment())
                            true
                        }
                    }
                }
            }
        }
    }

    private fun setupMyReviewsList(view: View) {
        reviewsList = view.findViewById(R.id.myReviewsList)
        reviewsList.run {
            adapter = ReviewsAdapter(
                reviewType = ReviewType.USER,
                onLocationClicked = { coord, name ->
                    activity?.run {
                        val gmmIntentUri = Uri.parse(
                            "geo:${coord.latitude},${coord.longitude}?q=${
                                Uri.encode(name)
                            }"
                        )
                        Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
                            setPackage("com.google.android.apps.maps")
                            resolveActivity(packageManager)?.let {
                                startActivity(this)
                            }
                        }
                    }
                },
                scope = lifecycleScope,
                onDelete = {
                    reviewsViewModel.deleteReviewById(it.id)
                },
                onEdit = {
                    findNavController().navigate(
                        UserProfileFragmentDirections.actionUserProfileFragmentToEditReviewFragment(
                            it.id
                        )
                    )
                })
            layoutManager = LinearLayoutManager(context)

            addItemDecoration(
                DividerItemDecoration(
                    context,
                    LinearLayoutManager.VERTICAL
                )
            )

            addOnScrollListener(
                ReviewsScrollListener(
                    { reviewsViewModel.invalidateReviews() }
                )
            )
        }
        reviewsViewModel.getReviews(true).observe(viewLifecycleOwner) {
            if (it.isEmpty())
                reviewsViewModel.invalidateReviews()
            (reviewsList.adapter as? ReviewsAdapter)?.updateReviews(it)
        }
    }
}