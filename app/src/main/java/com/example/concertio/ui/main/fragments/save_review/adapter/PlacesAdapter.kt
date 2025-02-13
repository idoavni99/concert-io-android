package com.example.concertio.ui.main.fragments.save_review.adapter

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.widget.ArrayAdapter
import android.widget.Filter
import com.example.concertio.data.PlaceData
import com.example.concertio.extensions.hasPermission
import com.example.concertio.places.PlacesClientHolder
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.CircularBounds
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class PlacesAdapter(
    context: Context,
    private val scope: CoroutineScope
) :
    ArrayAdapter<PlaceData>(context, android.R.layout.simple_selectable_list_item) {
    private val placesClient = PlacesClientHolder.getInstance()
    private val results: MutableList<PlaceData> = mutableListOf()

    override fun getItem(position: Int): PlaceData {
        return results[position]
    }

    override fun getCount(): Int {
        return results.size
    }

    @SuppressLint("MissingPermission")
    private suspend fun queryPredictions(text: String) = withContext(Dispatchers.IO) {
        val lastLocation =
            if (context.hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION)) LocationServices.getFusedLocationProviderClient(
                context
            ).lastLocation.await() else null
        val result = placesClient.findAutocompletePredictions(
            FindAutocompletePredictionsRequest.builder().apply {
                query = text
                locationBias = if (lastLocation != null) CircularBounds.newInstance(
                    LatLng(
                        lastLocation.latitude,
                        lastLocation.longitude
                    ), 2000.0
                ) else null
                typesFilter = listOf("establishment")
            }.build()
        ).await().autocompletePredictions.map {
            PlaceData(
                it.getPrimaryText(null).toString(),
                it.placeId
            )
        }
        results.clear()
        results.addAll(result)
        scope.launch(Dispatchers.Main) {
            notifyDataSetChanged()
        }
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filterResults = FilterResults()
                scope.launch {
                    queryPredictions(constraint.toString())
                    filterResults.values = results
                    filterResults.count = results.size
                }
                return filterResults
            }

            override fun publishResults(p0: CharSequence?, p1: FilterResults?) {
                if (results.isNotEmpty()) {
                    notifyDataSetChanged()
                } else {
                    notifyDataSetInvalidated()
                }
            }
        }
    }
}