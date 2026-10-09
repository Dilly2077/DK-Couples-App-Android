package com.dk.together.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dk.together.games.FruitMergeEngine
import com.dk.together.games.FruitMergeSnapshot
import com.dk.together.ui.theme.EveluneBackground
import com.dk.together.ui.theme.EveluneInk
import com.dk.together.ui.theme.EveluneMuted
import com.dk.together.ui.theme.EveluneRoseDeep
import com.dk.together.ui.theme.EveluneRosePale
import kotlinx.coroutines.delay

@Composable
internal fun FruitMergeScreen(onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
    val prefs = remember { context.getSharedPreferences("duo_fruit_merge", 0) }
    var gameKey by remember { mutableIntStateOf(0) }
    val engine = remember(gameKey) { FruitMergeEngine() }
    var frame by remember(gameKey) { mutableLongStateOf(0L) }
    var aimX by remember(gameKey) { mutableFloatStateOf(0.5f) }
    var best by remember { mutableIntStateOf(prefs.getInt("best_score", 0)) }

    val snapshot: FruitMergeSnapshot = remember(frame, gameKey) { engine.snapshot() }

    LaunchedEffect(gameKey) {
        while (!engine.gameOver) {
            val merges = engine.step(1f / 60f)
            if (merges > 0) {
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }
            if (engine.score > best) {
                best = engine.score
                prefs.edit().putInt("best_score", best).apply()
            }
            frame++
            delay(16)
        }
        frame++
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    Column(
        Modifier.fillMaxSize().background(EveluneBackground).statusBarsPadding().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = EveluneRoseDeep)
            }
            Column(Modifier.weight(1f)) {
                Text(
                    "Fruit Merge",
                    fontFamily = FontFamily.Serif,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = EveluneInk,
                )
                Text("Merge matching fruit. Don't cross the line.", color = EveluneMuted, fontSize = 13.sp)
            }
            IconButton(onClick = { gameKey++ }) {
                Icon(Icons.Filled.Refresh, contentDescription = "Restart", tint = EveluneRoseDeep)
            }
        }

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            ScorePill("SCORE", snapshot.score.toString(), Modifier.weight(1f))
            ScorePill("BEST", best.toString(), Modifier.weight(1f))
            ScorePill("NEXT", FruitMergeEngine.fruitNames[snapshot.nextLevel], Modifier.weight(1.35f))
        }

        Surface(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            shape = RoundedCornerShape(30.dp),
            color = Color(0xFFFFFCF8),
            shadowElevation = 4.dp,
        ) {
            Box(Modifier.fillMaxSize().padding(10.dp)) {
                Canvas(
                    modifier = Modifier.fillMaxSize().pointerInput(gameKey, snapshot.gameOver) {
                        detectTapGestures(
                            onPress = { offset ->
                                aimX = (offset.x / size.width.toFloat()).coerceIn(0.04f, 0.96f)
                                if (!snapshot.gameOver) {
                                    engine.drop(aimX)
                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    frame++
                                }
                            },
                        )
                    },
                ) {
                    drawFruitBoard(snapshot, aimX)
                }

                if (snapshot.gameOver) {
                    Surface(
                        modifier = Modifier.align(Alignment.Center).padding(24.dp),
                        shape = RoundedCornerShape(26.dp),
                        color = Color(0xFFF9EEF2).copy(alpha = 0.97f),
                        shadowElevation = 8.dp,
                    ) {
                        Column(
                            Modifier.padding(22.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Text("Pile's full", fontFamily = FontFamily.Serif, fontSize = 27.sp, fontWeight = FontWeight.Bold, color = EveluneInk)
                            Text("Score ${snapshot.score} · Best $best", color = EveluneMuted)
                            Button(
                                onClick = { gameKey++ },
                                colors = ButtonDefaults.buttonColors(containerColor = EveluneRoseDeep),
                                shape = RoundedCornerShape(18.dp),
                            ) {
                                Icon(Icons.Filled.Refresh, contentDescription = null)
                                Spacer(Modifier.size(7.dp))
                                Text("Play again")
                            }
                        }
                    }
                }
            }
        }

        Text(
            "Tap anywhere in the container to drop the next fruit.",
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            color = EveluneMuted,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun ScorePill(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(20.dp), color = EveluneRosePale) {
        Column(Modifier.padding(horizontal = 12.dp, vertical = 9.dp)) {
            Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = EveluneRoseDeep, letterSpacing = .7.sp)
            Text(value, maxLines = 1, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = EveluneInk)
        }
    }
}

private val fruitColors = listOf(
    Color(0xFFE94E68),
    Color(0xFFF45B69),
    Color(0xFF8D68C9),
    Color(0xFFFF9A3D),
    Color(0xFFF58232),
    Color(0xFFD94C4C),
    Color(0xFFA6C86C),
    Color(0xFFF7A1A8),
    Color(0xFFF4C842),
    Color(0xFF9CCB62),
    Color(0xFF4FAF69),
)

private fun DrawScope.drawFruitBoard(snapshot: FruitMergeSnapshot, aimX: Float) {
    val w = size.width
    val h = size.height

    drawRoundRect(
        color = Color(0xFFFFF7F2),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(24.dp.toPx()),
    )

    val dangerY = h * FruitMergeEngine.DANGER_LINE
    drawLine(
        color = Color(0xFFE07C8C).copy(alpha = 0.65f),
        start = Offset(0f, dangerY),
        end = Offset(w, dangerY),
        strokeWidth = 2.dp.toPx(),
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f)),
    )

    val previewLevel = snapshot.nextLevel
    val previewR = FruitMergeEngine.radius(previewLevel) * minOf(w, h)
    val previewX = aimX * w
    drawLine(
        color = EveluneRoseDeep.copy(alpha = 0.14f),
        start = Offset(previewX, 0f),
        end = Offset(previewX, dangerY),
        strokeWidth = 2.dp.toPx(),
    )
    drawFruit(previewLevel, Offset(previewX, previewR + 4.dp.toPx()), previewR, alpha = 0.42f)

    snapshot.bodies.forEach { fruit ->
        val r = FruitMergeEngine.radius(fruit.level) * minOf(w, h)
        drawFruit(fruit.level, Offset(fruit.x * w, fruit.y * h), r)
    }

    drawRoundRect(
        color = Color(0xFFD9B8AF).copy(alpha = 0.8f),
        topLeft = Offset.Zero,
        size = Size(w, h),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(24.dp.toPx()),
        style = Stroke(width = 2.dp.toPx()),
    )
}

private fun DrawScope.drawFruit(level: Int, center: Offset, radius: Float, alpha: Float = 1f) {
    val color = fruitColors[level.coerceIn(0, fruitColors.lastIndex)].copy(alpha = alpha)
    drawCircle(color = color, radius = radius, center = center)
    drawCircle(
        color = Color.White.copy(alpha = 0.30f * alpha),
        radius = radius * 0.22f,
        center = Offset(center.x - radius * 0.30f, center.y - radius * 0.32f),
    )
    if (level >= 1) {
        val leaf = Path().apply {
            moveTo(center.x, center.y - radius * 0.90f)
            quadraticBezierTo(
                center.x + radius * 0.52f,
                center.y - radius * 1.20f,
                center.x + radius * 0.42f,
                center.y - radius * 0.72f,
            )
            quadraticBezierTo(
                center.x + radius * 0.14f,
                center.y - radius * 0.62f,
                center.x,
                center.y - radius * 0.90f,
            )
        }
        drawPath(leaf, Color(0xFF4B9B5E).copy(alpha = alpha))
    }
}
