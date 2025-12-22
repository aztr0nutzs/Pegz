package com.neon.peggame.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.neon.peggame.game.PegBoard
import com.neon.peggame.game.PegPieceType
import kotlin.math.hypot

/**
 * Renders the peg board on top of the PEGZ reference art.
 *
 * We compute hole coordinates procedurally so the layout stays consistent on different screens.
 * If you change the reference art, tweak [BoardLayout] values.
 */
@Composable
fun PegBoardView(
    modifier: Modifier = Modifier,
    background: ImageBitmap,
    board: PegBoard,
    selectedIndex: Int?,
    pieceBitmaps: Map<PegPieceType, ImageBitmap>,
    onTapHole: (Int) -> Unit,
    maxHeight: Dp = 720.dp
) {
    val layout = remember { BoardLayout() }
    val density = LocalDensity.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = maxHeight)
    ) {
        Image(
            bitmap = background,
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxWidth()
        )

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = maxHeight)
                .pointerInput(board, selectedIndex) {
                    detectTapGestures { tap ->
                        val hole = layout.findClosestHole(tap, size.width.toFloat(), size.height.toFloat())
                        if (hole != null) onTapHole(hole)
                    }
                }
        ) {
            val w = size.width
            val h = size.height

            val holeCenters = layout.holeCenters(w, h)
            val r = layout.holeRadiusPx(w, h)

            // Mask over the baked-in static pieces so our runtime pieces are what you see.
            for (i in holeCenters.indices) {
                val c = holeCenters[i]
                // dark ooze shadow
                drawCircle(
                    color = androidx.compose.ui.graphics.Color(0xAA001008),
                    radius = r * 1.02f,
                    center = c
                )
                // inner hole ring
                drawCircle(
                    color = androidx.compose.ui.graphics.Color(0xFF00331A),
                    radius = r * 0.86f,
                    center = c
                )
            }

            // Selection highlight
            if (selectedIndex != null && selectedIndex in holeCenters.indices) {
                val c = holeCenters[selectedIndex]
                drawCircle(
                    color = androidx.compose.ui.graphics.Color(0xAA00FF66),
                    radius = r * 1.20f,
                    center = c
                )
            }

            // Draw pieces
            board.cells.forEachIndexed { idx, cell ->
                val piece = cell.piece ?: return@forEachIndexed
                val center = holeCenters[idx]
                val bmp = pieceBitmaps[piece.type] ?: return@forEachIndexed

                val dst = androidx.compose.ui.geometry.Rect(
                    left = center.x - r,
                    top = center.y - r,
                    right = center.x + r,
                    bottom = center.y + r
                )

                drawImage(
                    image = bmp,
                    dstSize = androidx.compose.ui.unit.IntSize(dst.width.toInt(), dst.height.toInt()),
                    dstOffset = androidx.compose.ui.unit.IntOffset(dst.left.toInt(), dst.top.toInt())
                )
            }
        }
    }
}

/**
 * Calibrated to pegz2.jpg aesthetic.
 * Coordinates are normalized (0..1) relative to the image as displayed with ContentScale.Fit.
 */
private class BoardLayout(
    private val xCenter: Float = 0.50f,
    private val yTop: Float = 0.29f,
    private val dx: Float = 0.11f,
    private val dy: Float = 0.082f,
    private val radiusRelToWidth: Float = 0.045f
) {
    fun holeCenters(w: Float, h: Float): List<Offset> {
        val out = ArrayList<Offset>(15)
        for (r in 0..4) {
            val y = (yTop + r * dy) * h
            val rowWidth = r * dx
            val xStart = (xCenter - rowWidth / 2f) * w
            for (c in 0..r) {
                val x = xStart + c * dx * w
                out.add(Offset(x, y))
            }
        }
        return out
    }

    fun holeRadiusPx(w: Float, h: Float): Float {
        // Using width keeps sizing stable across devices (portrait reference art).
        return radiusRelToWidth * w
    }

    fun findClosestHole(tap: Offset, w: Float, h: Float): Int? {
        val centers = holeCenters(w, h)
        val r = holeRadiusPx(w, h) * 1.25f
        var bestIdx: Int? = null
        var bestDist = Float.MAX_VALUE
        for (i in centers.indices) {
            val c = centers[i]
            val d = hypot((tap.x - c.x).toDouble(), (tap.y - c.y).toDouble()).toFloat()
            if (d < bestDist) {
                bestDist = d
                bestIdx = i
            }
        }
        return if (bestIdx != null && bestDist <= r) bestIdx else null
    }
}
