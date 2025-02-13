package com.example.concertio.ui.main

import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_DRAGGING

class ReviewsScrollListener(
    private val onListEndReached: () -> Unit
) : OnScrollListener() {
    private var oldYCoordinate = 0
    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
        super.onScrollStateChanged(recyclerView, newState)

        if (newState == SCROLL_STATE_DRAGGING) {
            if (!recyclerView.canScrollVertically(1)) {
                onListEndReached()
            }

            oldYCoordinate = recyclerView.scrollY
        }
    }
}