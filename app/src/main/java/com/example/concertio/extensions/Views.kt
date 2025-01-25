package com.example.concertio.extensions

import android.content.Context
import android.net.Uri
import android.widget.ImageView
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
            uri,
            R.drawable.baseline_insert_photo_24
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
