package com.pspgames.library.custom

import android.content.Context
import android.graphics.Bitmap
import coil.size.Size
import coil.transform.Transformation
import com.hoko.blur.HokoBlur


class BlurTransformation @JvmOverloads constructor(
    private val context: Context,
    private val radius: Float = DEFAULT_RADIUS,
    private val sampling: Float = DEFAULT_SAMPLING
) : Transformation {
    private companion object {
        private const val DEFAULT_RADIUS = 10f
        private const val DEFAULT_SAMPLING = 1f
    }
    init {
        require(radius in 0.0..25.0) { "radius must be in [0, 25]." }
        require(sampling > 0) { "sampling must be > 0." }
    }

    @Suppress("NullableToStringCall")
    override val cacheKey = "${BlurTransformation::class.java.name}-$radius-$sampling"

    override suspend fun transform(bitmap: Bitmap, size: Size): Bitmap {
//        return HokoBlur.with(context)
//            .scheme(HokoBlur.SCHEME_NATIVE) //different implementation, RenderScript、OpenGL、Native(default) and Java
//            .mode(HokoBlur.MODE_STACK) //blur algorithms，Gaussian、Stack(default) and Box
//            .radius(10) //blur radius，max=25，default=5
//            .sampleFactor(2.0f) //scale factor，if factor=2，the width and height of a bitmap will be scale to 1/2 sizes，default=5
//            .forceCopy(false) //If scale factor=1.0f，the origin bitmap will be modified. You could set forceCopy=true to avoid it. default=false
//            .needUpscale(true) //After blurring，the bitmap will be upscaled to origin sizes，default=true
//            .translateX(150)//add x axis offset when blurring
//            .translateY(150)//add y axis offset when blurring
//            .processor() //build a blur processor
//            .blur(bitmap)
        return HokoBlur.with(context).blur(bitmap)
    }


}
//class BlurTransformation @JvmOverloads constructor(
//    private val radius: Int = DEFAULT_RADIUS,
//    private val sampling: Int = DEFAULT_SAMPLING
//) : Transformation {
//
//    init {
//        require(radius in 0..40) { "radius must be in [0, 25]." }
//        require(sampling > 0) { "sampling must be > 0." }
//    }
//
//    @Suppress("NullableToStringCall")
//    override val cacheKey = "${BlurTransformation::class.java.name}-$radius-$sampling"
//
//    override suspend fun transform(input: Bitmap, size: Size): Bitmap {
//        val width: Int = input.width
//        val height: Int = input.height
//        val scaledWidth = width / sampling
//        val scaledHeight = height / sampling
//        var bitmap = createBitmap(scaledWidth, scaledHeight, Bitmap.Config.ARGB_8888)
//        val canvas = Canvas(bitmap!!)
//        canvas.scale(1 / sampling.toFloat(), 1 / sampling.toFloat())
//        val paint = Paint()
//        paint.flags = Paint.FILTER_BITMAP_FLAG
//        canvas.drawBitmap(input, 0f, 0f, paint)
//        bitmap = blur(bitmap, radius, true)
//        return bitmap
//    }
//
//    private companion object {
//        private const val DEFAULT_RADIUS = 10
//        private const val DEFAULT_SAMPLING = 1
//    }
//
//    private   fun blur(sentBitmap: Bitmap, radius: Int, canReuseInBitmap: Boolean): Bitmap? {
//        val bitmap: Bitmap = if (canReuseInBitmap) {
//            sentBitmap
//        } else {
//            sentBitmap.copy(sentBitmap.config, true)
//        }
//        if (radius < 1) {
//            return null
//        }
//        val w = bitmap.width
//        val h = bitmap.height
//        val pix = IntArray(w * h)
//        bitmap.getPixels(pix, 0, w, 0, 0, w, h)
//        val wm = w - 1
//        val hm = h - 1
//        val wh = w * h
//        val div = radius + radius + 1
//        val r = IntArray(wh)
//        val g = IntArray(wh)
//        val b = IntArray(wh)
//        var rsum: Int
//        var gsum: Int
//        var bsum: Int
//        var x: Int
//        var y: Int
//        var i: Int
//        var p: Int
//        var yp: Int
//        var yi: Int
//        var yw: Int
//        val vmin = IntArray(Math.max(w, h))
//        var divsum = div + 1 shr 1
//        divsum *= divsum
//        val dv = IntArray(256 * divsum)
//        i = 0
//        while (i < 256 * divsum) {
//            dv[i] = i / divsum
//            i++
//        }
//        yi = 0
//        yw = yi
//        val stack = Array(div) {
//            IntArray(
//                3
//            )
//        }
//        var stackpointer: Int
//        var stackstart: Int
//        var sir: IntArray
//        var rbs: Int
//        val r1 = radius + 1
//        var routsum: Int
//        var goutsum: Int
//        var boutsum: Int
//        var rinsum: Int
//        var ginsum: Int
//        var binsum: Int
//        y = 0
//        while (y < h) {
//            bsum = 0
//            gsum = bsum
//            rsum = gsum
//            boutsum = rsum
//            goutsum = boutsum
//            routsum = goutsum
//            binsum = routsum
//            ginsum = binsum
//            rinsum = ginsum
//            i = -radius
//            while (i <= radius) {
//                p = pix[yi + Math.min(wm, Math.max(i, 0))]
//                sir = stack[i + radius]
//                sir[0] = p and 0xff0000 shr 16
//                sir[1] = p and 0x00ff00 shr 8
//                sir[2] = p and 0x0000ff
//                rbs = r1 - Math.abs(i)
//                rsum += sir[0] * rbs
//                gsum += sir[1] * rbs
//                bsum += sir[2] * rbs
//                if (i > 0) {
//                    rinsum += sir[0]
//                    ginsum += sir[1]
//                    binsum += sir[2]
//                } else {
//                    routsum += sir[0]
//                    goutsum += sir[1]
//                    boutsum += sir[2]
//                }
//                i++
//            }
//            stackpointer = radius
//            x = 0
//            while (x < w) {
//                r[yi] = dv[rsum]
//                g[yi] = dv[gsum]
//                b[yi] = dv[bsum]
//                rsum -= routsum
//                gsum -= goutsum
//                bsum -= boutsum
//                stackstart = stackpointer - radius + div
//                sir = stack[stackstart % div]
//                routsum -= sir[0]
//                goutsum -= sir[1]
//                boutsum -= sir[2]
//                if (y == 0) {
//                    vmin[x] = Math.min(x + radius + 1, wm)
//                }
//                p = pix[yw + vmin[x]]
//                sir[0] = p and 0xff0000 shr 16
//                sir[1] = p and 0x00ff00 shr 8
//                sir[2] = p and 0x0000ff
//                rinsum += sir[0]
//                ginsum += sir[1]
//                binsum += sir[2]
//                rsum += rinsum
//                gsum += ginsum
//                bsum += binsum
//                stackpointer = (stackpointer + 1) % div
//                sir = stack[stackpointer % div]
//                routsum += sir[0]
//                goutsum += sir[1]
//                boutsum += sir[2]
//                rinsum -= sir[0]
//                ginsum -= sir[1]
//                binsum -= sir[2]
//                yi++
//                x++
//            }
//            yw += w
//            y++
//        }
//        x = 0
//        while (x < w) {
//            bsum = 0
//            gsum = bsum
//            rsum = gsum
//            boutsum = rsum
//            goutsum = boutsum
//            routsum = goutsum
//            binsum = routsum
//            ginsum = binsum
//            rinsum = ginsum
//            yp = -radius * w
//            i = -radius
//            while (i <= radius) {
//                yi = Math.max(0, yp) + x
//                sir = stack[i + radius]
//                sir[0] = r[yi]
//                sir[1] = g[yi]
//                sir[2] = b[yi]
//                rbs = r1 - Math.abs(i)
//                rsum += r[yi] * rbs
//                gsum += g[yi] * rbs
//                bsum += b[yi] * rbs
//                if (i > 0) {
//                    rinsum += sir[0]
//                    ginsum += sir[1]
//                    binsum += sir[2]
//                } else {
//                    routsum += sir[0]
//                    goutsum += sir[1]
//                    boutsum += sir[2]
//                }
//                if (i < hm) {
//                    yp += w
//                }
//                i++
//            }
//            yi = x
//            stackpointer = radius
//            y = 0
//            while (y < h) {
//
//                // Preserve alpha channel: ( 0xff000000 & pix[yi] )
//                pix[yi] =
//                    -0x1000000 and pix[yi] or (dv[rsum] shl 16) or (dv[gsum] shl 8) or dv[bsum]
//                rsum -= routsum
//                gsum -= goutsum
//                bsum -= boutsum
//                stackstart = stackpointer - radius + div
//                sir = stack[stackstart % div]
//                routsum -= sir[0]
//                goutsum -= sir[1]
//                boutsum -= sir[2]
//                if (x == 0) {
//                    vmin[y] = Math.min(y + r1, hm) * w
//                }
//                p = x + vmin[y]
//                sir[0] = r[p]
//                sir[1] = g[p]
//                sir[2] = b[p]
//                rinsum += sir[0]
//                ginsum += sir[1]
//                binsum += sir[2]
//                rsum += rinsum
//                gsum += ginsum
//                bsum += binsum
//                stackpointer = (stackpointer + 1) % div
//                sir = stack[stackpointer]
//                routsum += sir[0]
//                goutsum += sir[1]
//                boutsum += sir[2]
//                rinsum -= sir[0]
//                ginsum -= sir[1]
//                binsum -= sir[2]
//                yi += w
//                y++
//            }
//            x++
//        }
//        bitmap.setPixels(pix, 0, w, 0, 0, w, h)
//        return bitmap
//    }
//}