package com.zippyid.zippydroid.extension

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.Image
import android.util.Log
import java.io.ByteArrayOutputStream

fun Image.toBitmap(): Bitmap? {
    val buffer = planes[0].buffer
    val bytes = ByteArray(buffer.remaining())
    buffer.get(bytes)
    return BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: null
}

fun Bitmap.resizeAndRotate(imageOrientation: Int): Bitmap? {
    Log.d("IMAGE", "Original size: w:$width, h:$height")

    val matrix = Matrix()

    val imageRatio = width.toFloat() / height

    val resizedBitmap = Bitmap.createScaledBitmap(this, (600 * imageRatio).toInt(), 600, false)
    Log.d("IMAGE", "Resized size: w:${resizedBitmap.width}, h:${resizedBitmap.height}")

    recycle()

    return resizedBitmap
}

fun Bitmap.toEncodedPng(): String {
    val bos = ByteArrayOutputStream()
    compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos)
    val bitmapData = bos.toByteArray()

    val base64 = android.util.Base64.encodeToString(bitmapData, android.util.Base64.DEFAULT)

    base64.replace("+", "%2b")

    return "data:image/png;base64,$base64"
}