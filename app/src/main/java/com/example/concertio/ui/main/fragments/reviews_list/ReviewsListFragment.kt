package com.example.concertio.ui.main.fragments.reviews_list

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.concertio.R
import com.example.concertio.ui.main.listadapter.ReviewType
import com.example.concertio.ui.main.listadapter.ReviewsAdapter
import com.example.concertio.ui.main.ReviewsViewModel


class ReviewsListFragment : Fragment() {
    private lateinit var reviewsList: RecyclerView
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
        initStudentsList(view)
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

    private fun initStudentsList(view: View) {
        reviewsList.run {
            layoutManager = LinearLayoutManager(view.context)
            adapter = ReviewsAdapter(reviewType = ReviewType.REVIEW)
            addItemDecoration(
                DividerItemDecoration(
                    context,
                    LinearLayoutManager.VERTICAL
                )
            )
        }
    }
}