package com.lhd.view.materialseekbar

import android.graphics.*
import android.os.Build
import android.util.Log
import android.view.View
import androidx.annotation.FontRes
import androidx.core.content.res.ResourcesCompat
import java.math.BigDecimal


fun RectF.set(l: Number, t: Number, r: Number, b: Number) {
    set(l.toFloat(), t.toFloat(), r.toFloat(), b.toFloat())
}

fun Rect.set(l: Number, t: Number, r: Number, b: Number) {
    set(l.toInt(), t.toInt(), r.toInt(), b.toInt())
}

fun RectF.setCenter(cX: Number, cY: Number, sizeX: Number, sizeY: Number) {
    val centerX = cX.toFloat()
    val centerY = cY.toFloat()
    val width = sizeX.toFloat()
    val height = sizeY.toFloat()
    set(centerX - width / 2f, centerY - height / 2f, centerX + width / 2f, centerY + height / 2f)
}

fun RectF.setCenterX(cX: Number, sizeX: Number) {
    val centerX = cX.toFloat()
    val width = sizeX.toFloat()
    set(centerX - width / 2f, top, centerX + width / 2f, bottom)
}

fun Rect.setCenter(cX: Number, cY: Number, sizeX: Number, sizeY: Number) {
    val centerX = cX.toInt()
    val centerY = cY.toInt()
    val width = sizeX.toInt()
    val height = sizeY.toInt()
    set(centerX - width / 2f, centerY - height / 2, centerX + width / 2, centerY + height / 2)
}

fun View.getTypeFaceById(@FontRes fontId: Int): Typeface? {
    if (context == null)
        return null
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        context.resources.getFont(fontId)
    } else
        ResourcesCompat.getFont(context, fontId)
}

var isLogEnable = true
val TAG_LOG = "VIEW_LOG"
fun eLog(message: String) {
    TAG_LOG.eLog(message)
}

fun String.eLog(message: String) {
    if (isLogEnable)
        Log.e(this, message)
}

fun Float.abs(): Float {
    return kotlin.math.abs(this)
}

fun Int.abs(): Int {
    return kotlin.math.abs(this)
}

fun View.dpToPixel(dp: Float): Float {
    return dp * resources.displayMetrics.density
}

fun Bitmap.scaleBitmap(width: Number, height: Number): Bitmap {
    val background = Bitmap.createBitmap(width.toInt(), height.toInt(), Bitmap.Config.ARGB_8888)

    val originalWidth: Float = getWidth().toFloat()
    val originalHeight: Float = getHeight().toFloat()

    val canvas = Canvas(background)

    val scale = width.toInt() / originalWidth

    val xTranslation = 0.0f
    val yTranslation = (height.toInt() - originalHeight * scale) / 2.0f

    val transformation = Matrix()
    transformation.postTranslate(xTranslation, yTranslation)
    transformation.preScale(scale, scale)

    val paint = Paint()
    paint.isFilterBitmap = true

    canvas.drawBitmap(this, transformation, paint)

    return background
}

fun Float.scale(numberDigitsAfterComma: Int): Float {
    return BigDecimal(this.toDouble()).setScale(numberDigitsAfterComma, BigDecimal.ROUND_HALF_EVEN)
        .toFloat()
}
