package com.uet.nvmnghia.yacv.glide

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.TransformationUtils
import java.nio.charset.Charset
import java.security.MessageDigest


/**
 * Mostly a copypasta of [CenterCrop].
 */
class TopCrop : BitmapTransformation() {

    companion object {
        private const val ID = "com.uet.nvmnghia.yacv.glide"
        private val ID_BYTES = ID.toByteArray(Charset.forName("UTF-8"))
        private val DEFAULT_PAINT = Paint(TransformationUtils.PAINT_FLAGS)

    }

    /**
     * This transformation takes no argument, so no special handling of cache key is needed.
     */
    override fun updateDiskCacheKey(messageDigest: MessageDigest) {
        messageDigest.update(ID_BYTES)
    }

    override fun equals(other: Any?): Boolean {
        return other is TopCrop
    }

    override fun hashCode(): Int {
        return ID.hashCode()
    }

    /**
     * A potentially expensive operation to crop the given Bitmap so that it fills the given
     * dimensions. This operation is significantly less expensive in terms of memory if a mutable
     * Bitmap with the given dimensions is passed in as well.
     *
     * @param pool The BitmapPool to obtain a bitmap from.
     * @param toTransform The Bitmap to transform.
     * @param outWidth The width in pixels of the final Bitmap.
     * @param outHeight The height in pixels of the final Bitmap.
     * @return The resized Bitmap (will be recycled if recycled is not null).
     */
    override fun transform(
        pool: BitmapPool,
        toTransform: Bitmap,
        outWidth: Int,
        outHeight: Int,
    ): Bitmap {
        if (toTransform.width == outWidth && toTransform.height == outHeight) {
            return toTransform
        }

        val scale: Float; val dx: Float; val dy: Float
        val m = Matrix()

        val scaleX = outWidth / toTransform.width.toFloat()
        val scaleY = outHeight / toTransform.height.toFloat()
        if (scaleY > scaleX) {
            scale = scaleY
            dx = (outWidth - toTransform.width * scale) * 0.5f
            dy = 0f
        } else {
            scale = scaleX
            dx = 0f
            dy = 0f
        }

        m.setScale(scale, scale)
        m.postTranslate(dx, dy)

        val result: Bitmap = pool.get(outWidth, outHeight,
            toTransform.config ?: Bitmap.Config.ARGB_8888)
        TransformationUtils.setAlpha(toTransform, result)

        // Copypasta: applyMatrix
        val canvas = Canvas(result)
        canvas.drawBitmap(toTransform, m, DEFAULT_PAINT)
        canvas.setBitmap(null)

        return result
    }

}