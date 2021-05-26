package xyz.techmush.birdhouse_peeper.util

import android.graphics.BitmapFactory
import android.widget.ImageView

fun setImageFromPath(imageView: ImageView, imagePath: String) {
    // Get the dimensions of the View
    val targetW: Int = imageView.width
    val targetH: Int = imageView.height

    val bmOptions = BitmapFactory.Options().apply {
        // Get the dimensions of the bitmap
        inJustDecodeBounds = true

        BitmapFactory.decodeFile(imagePath, this)

        val photoW: Int = outWidth
        val photoH: Int = outHeight

        // Determine how much to scale down the image
        val scaleFactor: Int = Math.max(1, Math.min(photoW / targetW, photoH / targetH))

        // Decode the image file into a Bitmap sized to fill the View
        inJustDecodeBounds = false
        inSampleSize = scaleFactor
        inPurgeable = true
    }
    BitmapFactory.decodeFile(imagePath, bmOptions)?.also { bitmap ->
        imageView.setImageBitmap(bitmap)
    }
}
