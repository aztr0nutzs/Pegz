package com.neon.peggame.ui.art

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.neon.peggame.R
import com.neon.peggame.game.PegPieceType

/**
 * Extracts the "Pegz pieces" directly from the provided PEGZ art so the in-game
 * pieces stay visually identical to the reference images.
 *
 * We crop small regions out of pegz2.jpg (the gameplay art) and reuse those
 * bitmaps as draggable/clickable pieces.
 *
 * If you ever update pegz2.jpg, you can adjust the crop rectangles here.
 */
object PegzArt {

    // Crop rectangles on pegz2.jpg at its native resolution (832x1248).
    // Format: left, top, width, height
    private val CROP_BY_TYPE: Map<PegPieceType, IntArray> = mapOf(
        PegPieceType.PINK to intArrayOf(391, 468, 52, 52),
        PegPieceType.BLUE to intArrayOf(78, 407, 44, 44),
        PegPieceType.GREEN to intArrayOf(682, 364, 58, 58),
        PegPieceType.TEAL to intArrayOf(467, 707, 54, 54),
        PegPieceType.SKULL to intArrayOf(300, 459, 40, 40),
    )

    fun loadBackground(context: Context, @DrawableRes resId: Int): Bitmap =
        BitmapFactory.decodeResource(context.resources, resId)

    fun loadPieceBitmap(context: Context, type: PegPieceType): Bitmap {
        val base = loadBackground(context, R.drawable.pegz2)
        val crop = CROP_BY_TYPE[type] ?: error("Missing crop for $type")
        val (l, t, w, h) = crop
        val safeW = w.coerceAtMost(base.width - l)
        val safeH = h.coerceAtMost(base.height - t)
        return Bitmap.createBitmap(base, l, t, safeW, safeH)
    }
}

@Composable
fun rememberPegPieceBitmaps(context: Context): Map<PegPieceType, ImageBitmap> {
    return remember {
        PegPieceType.entries.associateWith { type ->
            PegzArt.loadPieceBitmap(context, type).asImageBitmap()
        }
    }
}
