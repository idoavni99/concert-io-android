package com.example.concertio.extensions

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.ImageView
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.concertio.R

fun initMedia(
    context: Context,
    imageView: ImageView?,
    videoView: PlayerView?,
    uri: Uri,
    mediaType: String
) {
    if (mediaType == "image") {
        videoView?.isVisible = false
        imageView?.isVisible = true
        imageView?.loadReviewImage(
            context,
            uri
        )
    } else {
        videoView?.apply {
            player =
                ExoPlayer.Builder(context).build()
            player?.setMediaItem(MediaItem.fromUri(uri))
            player?.repeatMode = ExoPlayer.REPEAT_MODE_ONE
            player?.seekTo(0, 0L)
            player?.prepare()
            imageView?.isVisible = false
            isVisible = true
        }
    }
}

fun Context.hasPermission(permission: String): Boolean {
    return ActivityCompat.checkSelfPermission(
        this,
        permission
    ) != PackageManager.PERMISSION_GRANTED
}