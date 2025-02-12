package com.example.concertio.extensions

import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.CircularProgressIndicatorSpec
import com.google.android.material.progressindicator.IndeterminateDrawable

fun MaterialButton.showProgress(@ColorInt tintColor: Int = this.iconTint.defaultColor): Drawable {
    val spec = CircularProgressIndicatorSpec(
        context, null, 0,
        com.google.android.material.R.style.Widget_Material3_CircularProgressIndicator_Small
    )

    spec.indicatorColors = intArrayOf(tintColor)

    val progressIndicatorDrawable =
        IndeterminateDrawable.createCircularDrawable(context, spec)

    val oldIcon = this.icon
    this.icon = progressIndicatorDrawable
    this.isClickable = false
    return oldIcon
}

fun MaterialButton.stopProgress(icon: Drawable? = null) {
    this.icon = icon
    this.isClickable = true
}
