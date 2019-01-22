package com.zippyid.zippydroid.extension

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.Image
import java.io.ByteArrayOutputStream
import java.util.*
import android.util.Log

//TODO separate resizing and encoding logic
fun Image.toEncodedResizedPng(imageOrientation: Int): String {
    val buffer = planes[0].buffer
    val bytes = ByteArray(buffer.remaining())
    buffer.get(bytes)
    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return ""

    Log.d("IMAGE", "Original size: w:${bitmap.width}, h:${bitmap.height}")

    val matrix = Matrix()

    matrix.postRotate(imageOrientation.toFloat())

    val imageRatio = bitmap.width.toFloat() / bitmap.height

    val resizedBitmap = Bitmap.createScaledBitmap(bitmap, (600 * imageRatio).toInt(), 600, false)
    Log.d("IMAGE", "Resized size: w:${resizedBitmap.width}, h:${resizedBitmap.height}")
    val rotatedBitmap = Bitmap.createBitmap(resizedBitmap, 0, 0, resizedBitmap.width, resizedBitmap.height, matrix, true)

    resizedBitmap.recycle()
    bitmap.recycle()

    val bos = ByteArrayOutputStream()
    rotatedBitmap.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos)
    val bitmapData = bos.toByteArray()

    //TODO fix this call for older phones
    val base64 = Base64.getEncoder().encodeToString(bitmapData)

    base64.replace("+", "%2b")

    return base64
}