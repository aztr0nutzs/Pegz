// ui/PegBoard.kt (UPDATED - DEBUG COORD OVERLAY)

package com.neon.peggame.ui

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector2D
import androidx.compose.animation.core.InfiniteRepeatableSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.neon.peggame.model.INVALID
import com.neon.peggame.model.PEG
import com.neon.peggame.model.Position
import com.neon.peggame.viewmodel.GameUiState
import com.neon.peggame.viewmodel.GameViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.min
import kotlin.random.Random

private const val NUM_ROWS = 5
private const val ROW_HEIGHT_FACTOR = 0.866
private const val PEG_RADIUS_FACTOR = 0.08f
private const val JUMP_ANIM_DURATION_MS = 200

private data class JumpAnimationState(
    val startOffset: Offset,
    val targetOffset: Offset,
    val progress: Animatable<Offset, AnimationVector2D>,
    val jumpedPosition: Position?
)

private data class Particle(
    var offset: Offset,
    val velocityX: Float,
    val velocityY: Float,
    var lifetime: Float,
    val color: Color
)

@Composable
fun PegBoard(
    uiState: GameUiState,
    viewModel: GameViewModel,
    onPositionTap: (Position) -> Unit,
    modifier: Modifier = Modifier,
) {
    val neonColor = CyberpunkTheme.colors.peg
    val selectedColor = CyberpunkTheme.colors.pegSelected
    val holeColor = CyberpunkTheme.colors.hole
    val frameColor = CyberpunkTheme.colors.boardFrame
    val jumpHighlightColor = CyberpunkTheme.colors.secondary

    // Dev / QA overlay: show (row,col) coordinates on each valid hole.
    val debugCoords by viewModel.debugCoordsEnabled.collectAsState(initial = false)

    val coordPaint = remember {
        Paint().apply {
            isAntiAlias = true
            color = android.graphics.Color.argb(215, 240, 245, 255)
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        }
    }

    val glowScale = remember { Animatable(0.8f) }
    LaunchedEffect(Unit) {
        glowScale.animateTo(
            targetValue = 1.0f,
            animationSpec = InfiniteRepeatableSpec(
                animation = tween(1500, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            )
        )
    }

    var jumpState by remember { mutableStateOf<JumpAnimationState?>(null) }
    val particles = remember { mutableStateListOf<Particle>() }

    LaunchedEffect(Unit) {
        while (true) {
            delay(16)
            particles.removeAll { it.lifetime <= 0f }
            particles.forEach { p ->
                p.offset = Offset(p.offset.x + p.velocityX, p.offset.y + p.velocityY)
                p.lifetime -= 0.03f
            }
        }
    }

    BoxWithConstraints(modifier.fillMaxSize()) {
        val minSize = min(constraints.maxWidth, constraints.maxHeight).toFloat()
        val boardCenterX = constraints.maxWidth / 2f
        val boardTopY = 40.dp.toPx()
        val rowHeight = (minSize * 0.82f) / (NUM_ROWS - 1) * ROW_HEIGHT_FACTOR
        val pegRadius = minSize * PEG_RADIUS_FACTOR

        val coords = remember(minSize, uiState.board) {
            calculateCoords(uiState.board, boardCenterX, boardTopY, rowHeight, minSize)
        }

        fun getCoords(r: Int, c: Int): Offset = coords[r][c] ?: Offset.Zero

        // Hook to existing jump logic if your ViewModel triggers lastMoveTrigger
        LaunchedEffect(uiState.lastMoveTrigger) {
            if (uiState.lastMoveTrigger <= 0L) return@LaunchedEffect

            val move = viewModel.consumeLastMove() ?: return@LaunchedEffect
            val start = getCoords(move.from.row, move.from.col)
            val target = getCoords(move.to.row, move.to.col)

            val anim = Animatable(start, Offset.VectorConverter)
            jumpState = JumpAnimationState(
                startOffset = start,
                targetOffset = target,
                progress = anim,
                jumpedPosition = move.jumped
            )

            launch {
                anim.animateTo(target, tween(JUMP_ANIM_DURATION_MS, easing = LinearEasing))
                spawnParticles(
                    center = getCoords(move.jumped.row, move.jumped.col),
                    pegRadius = pegRadius,
                    color = neonColor,
                    out = particles
                )
                jumpState = null
            }
        }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        var tapped: Position? = null
                        loop@ for (r in 0 until NUM_ROWS) {
                            for (c in 0 until NUM_ROWS) {
                                if (uiState.board[r][c] == INVALID) continue
                                val center = getCoords(r, c)
                                if ((offset - center).getDistance() <= pegRadius) {
                                    tapped = Position(r, c)
                                    break@loop
                                }
                            }
                        }
                        tapped?.let(onPositionTap)
                    }
                }
        ) {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF05080D), Color(0xFF010105)),
                    startY = 0f,
                    endY = size.height
                )
            )
            drawScanlines(this)

            // Triangle frame
            drawBoardFrame(this, frameColor, getCoords(0, 2), getCoords(4, 0), getCoords(4, 4))

            // Holes + pegs
            for (r in 0 until NUM_ROWS) {
                for (c in 0 until NUM_ROWS) {
                    if (uiState.board[r][c] == INVALID) continue

                    val center = getCoords(r, c)
                    val pos = Position(r, c)

                    val isJumpingFrom = jumpState?.startOffset == center
                    val isJumped = jumpState?.jumpedPosition == pos

                    // Always draw hole; skip static peg if animating/jumped
                    drawHole(this, center, pegRadius, holeColor)

                    if (debugCoords) {
                        coordPaint.textSize = (pegRadius * 0.65f).coerceAtLeast(14f)
                        drawContext.canvas.nativeCanvas.drawText(
                            "($r,$c)",
                            center.x,
                            center.y + (pegRadius * 0.22f),
                            coordPaint
                        )
                    }

                    if (isJumpingFrom || isJumped) continue

                    val isSelected = uiState.selectedPos == pos
                    val isJumpTarget = uiState.validJumps.contains(pos)

                    if (uiState.board[r][c] == PEG) {
                        val pegColor = if (isSelected) selectedColor else neonColor
                        val currentGlowScale = if (isSelected) 1.2f else 1.0f + glowScale.value * 0.1f
                        drawNeonPeg(this, center, pegRadius * currentGlowScale, pegColor)
                    }

                    if (isJumpTarget) {
                        drawCircle(
                            color = jumpHighlightColor.copy(alpha = 0.5f),
                            radius = pegRadius * 1.1f,
                            center = center,
                            style = Stroke(width = 4.dp.toPx())
                        )
                    }
                }
            }

            // Animated peg
            jumpState?.let { state ->
                drawNeonPeg(this, state.progress.value, pegRadius * 1.1f, neonColor)
            }

            // Particles
            particles.forEach { p ->
                drawCircle(
                    color = p.color.copy(alpha = p.lifetime.coerceIn(0f, 1f)),
                    radius = pegRadius * 0.12f,
                    center = p.offset
                )
            }
        }
    }
}

/* ---------------- Helpers ---------------- */

private fun calculateCoords(
    board: Array<IntArray>,
    centerX: Float,
    topY: Float,
    rowHeight: Float,
    minSize: Float
): Array<Array<Offset?>> {
    val spacing = minSize * 0.18f
    return Array(NUM_ROWS) { r ->
        val cols = (0 until NUM_ROWS).filter { c -> board[r][c] != INVALID }
        val count = cols.size
        Array(NUM_ROWS) { c ->
            if (c !in cols) return@Array null
            val idx = cols.indexOf(c)
            val x = centerX + (idx - (count - 1) / 2f) * spacing
            val y = topY + r * rowHeight
            Offset(x, y)
        }
    }
}

private fun drawScanlines(scope: DrawScope) {
    val step = 10f
    var y = 0f
    while (y < scope.size.height) {
        scope.drawLine(
            color = Color.White.copy(alpha = 0.03f),
            start = Offset(0f, y),
            end = Offset(scope.size.width, y),
            strokeWidth = 1f
        )
        y += step
    }
}

private fun drawBoardFrame(scope: DrawScope, frameColor: Color, top: Offset, left: Offset, right: Offset) {
    val path = Path().apply {
        moveTo(top.x, top.y)
        lineTo(left.x, left.y)
        lineTo(right.x, right.y)
        close()
    }

    scope.drawPath(
        path = path,
        brush = Brush.linearGradient(
            colors = listOf(frameColor.copy(alpha = 0.15f), frameColor.copy(alpha = 0.45f)),
            start = top,
            end = right
        )
    )

    scope.drawPath(
        path = path,
        color = frameColor.copy(alpha = 0.65f),
        style = Stroke(width = 3.dp.toPx())
    )
}

private fun drawHole(scope: DrawScope, center: Offset, radius: Float, holeColor: Color) {
    scope.drawCircle(color = holeColor, radius = radius * 0.65f, center = center)
    scope.drawCircle(
        color = Color.White.copy(alpha = 0.05f),
        radius = radius * 0.72f,
        center = center,
        style = Stroke(width = 2.dp.toPx())
    )
}

private fun drawNeonPeg(scope: DrawScope, center: Offset, radius: Float, color: Color) {
    scope.drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(color.copy(alpha = 0.55f), Color.Transparent),
            center = center,
            radius = radius * 2.2f
        ),
        radius = radius * 2.2f,
        center = center
    )
    scope.drawCircle(color = color, radius = radius, center = center)
    scope.drawCircle(
        color = Color.White.copy(alpha = 0.12f),
        radius = radius,
        center = center,
        style = Stroke(width = 2.dp.toPx())
    )
}

private fun spawnParticles(center: Offset, pegRadius: Float, color: Color, out: MutableList<Particle>) {
    val count = 14
    repeat(count) {
        val angle = Random.nextFloat() * (Math.PI.toFloat() * 2f)
        val speed = Random.nextFloat() * (pegRadius * 0.18f) + (pegRadius * 0.06f)
        val vx = kotlin.math.cos(angle) * speed
        val vy = kotlin.math.sin(angle) * speed
        out.add(
            Particle(
                offset = center,
                velocityX = vx,
                velocityY = vy,
                lifetime = Random.nextFloat() * 0.8f + 0.6f,
                color = color
            )
        )
    }
}
