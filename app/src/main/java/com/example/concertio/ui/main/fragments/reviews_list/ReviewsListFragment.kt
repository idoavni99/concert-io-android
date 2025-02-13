package com.example.concertio.ui.main.fragments.reviews_list

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import com.example.concertio.R
import com.example.concertio.ui.main.listadapter.ReviewType
import com.example.concertio.ui.main.listadapter.ReviewsAdapter
import com.example.concertio.ui.main.ReviewsViewModel
import com.google.android.material.search.SearchBar
import com.google.android.material.search.SearchView


class ReviewsListFragment : Fragment() {
    private lateinit var reviewsList: RecyclerView
    private lateinit var searchResults: RecyclerView;
    private lateinit var searchBar: SearchBar
    private lateinit var searchView: SearchView
    private val viewModel: ReviewsViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_reviews_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        reviewsList = view.findViewById(R.id.reviews_list)
        searchResults = view.findViewById(R.id.reviews_search_results)
        searchView = view.findViewById(R.id.search_view)
        searchBar = view.findViewById(R.id.search_bar)
        initReviewsList(view, reviewsList)
        initReviewsList(view, searchResults)
        initReviewsSearch()
        initReviewsList(view)
        viewModel.getReviews().observe(viewLifecycleOwner, {
            if (it.isEmpty()) viewModel.invalidateReviews()
            (reviewsList.adapter as? ReviewsAdapter)?.updateReviews(it)
        })
    }

    override fun onDestroyView() {
        view?.findViewById<RecyclerView>(R.id.myReviewsList)?.run {
            (adapter as? ReviewsAdapter)?.onViewHidden(this)
        }
        super.onDestroyView()
    }

    private fun initReviewsSearch() {
        searchView.setupWithSearchBar(searchBar)
        searchView.editText.doOnTextChanged { text, _, _, _ ->
            text?.let {
                viewModel.cancelRunningSearch()
                if (text.isNotEmpty()) {
                    viewModel.searchReviews(it.toString()) { results ->
                        this@ReviewsListFragment.view?.findViewById<View>(R.id.no_results_text)?.isVisible =
                            results.isEmpty()
                        (searchResults.adapter as? ReviewsAdapter)?.updateReviews(results)
                    }
                } else {
                    this@ReviewsListFragment.view?.findViewById<View>(R.id.no_results_text)?.isVisible =
                        true
                    (searchResults.adapter as? ReviewsAdapter)?.updateReviews(emptyList())
                }
            }
        }
    }

    private fun initReviewsList(view: View, recycler: RecyclerView = reviewsList) {
        recycler.run {
            layoutManager = LinearLayoutManager(view.context)
            adapter =
                ReviewsAdapter(reviewType = ReviewType.REVIEW, onLocationClicked = { coord, name ->
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
                }, scope = lifecycleScope)
            addItemDecoration(
                DividerItemDecoration(
                    context,
                    LinearLayoutManager.VERTICAL
                )
            )
            addOnScrollListener(
                object : OnScrollListener() {
                    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                        super.onScrollStateChanged(recyclerView, newState)

                        if (!this@run.canScrollVertically(1)) {
                            viewModel.onListEnd()
                        }
                    }
                }
            )
        }
    }
}