package com.example.concertio.extensions

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.MenuItem
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.concertio.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

fun ImageView.loadProfilePicture(context: Context, uri: Uri) {
    Glide.with(context)
        .load(uri)
        .circleCrop()
        .placeholder(
            R.drawable.empty_profile_picture
        )
        .into(this)
}

fun ImageView.loadReviewImage(context: Context, uri: Uri) {
    Glide.with(context)
        .load(uri)
        .placeholder(
            R.drawable.baseline_insert_photo_24
        )
        .into(this)
}

fun MenuItem.loadImage(
    context: Context,
    uri: Uri,
    placeholderRes: Int,
    scope: CoroutineScope
) {
    loadImageIntoDrawable(context, uri, placeholderRes) {
        scope.launch(Dispatchers.Main) {
            icon = it
        }
    }
}

fun loadImageIntoDrawable(
    context: Context,
    uri: Uri,
    placeholderRes: Int,
    onDrawableLoaded: (drawable: Drawable) -> Unit
) {
    Glide.with(context)
        .asBitmap()
        .load(uri)
        .placeholder(placeholderRes)
        .circleCrop()
        .listener(object : RequestListener<Bitmap> {
            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: Target<Bitmap>,
                isFirstResource: Boolean
            ): Boolean {
                return false
            }

            override fun onResourceReady(
                resource: Bitmap,
                model: Any,
                target: Target<Bitmap>?,
                dataSource: DataSource,
                isFirstResource: Boolean
            ): Boolean {
                onDrawableLoaded(BitmapDrawable(context.resources, resource))
                return false
            }

        }).submit()
}