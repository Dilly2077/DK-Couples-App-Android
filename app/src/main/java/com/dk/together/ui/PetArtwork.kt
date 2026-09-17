package com.dk.together.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.dk.together.pets.engine.PetSpecies
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Production-art bridge for the v0.3 pet workstream.
 *
 * The approved four-environment contact sheet is bundled in app/src/main/assets/pets.
 * It is intentionally decoded at runtime so the original approved artwork can ship without
 * re-encoding or redrawing it. The source sheet is a 2x2 contact sheet in this order:
 * playroom, kitchen, garden, outdoor spa.
 */
enum class PetEnvironmentArtwork {
    PLAYROOM,
    KITCHEN,
    GARDEN,
    SPA,
}

enum class PetVisualState {
    IDLE,
    HUNGRY,
    DIRTY,
    HAPPY,
    CARRIED,
    SEATED,
    EATING,
    BATHING,
    PLAYING,
}

private fun loadEmbeddedJpegFromSvg(text: String): Bitmap? {
    val marker = "base64,"
    val start = text.indexOf(marker)
    if (start < 0) return null
    val end = text.indexOf('"', start + marker.length).let { if (it < 0) text.length else it }
    val base64 = text.substring(start + marker.length, end)
    val bytes = Base64.decode(base64, Base64.DEFAULT)
    return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
}

private fun cropEnvironment(sheet: Bitmap, environment: PetEnvironmentArtwork): Bitmap {
    val halfW = sheet.width / 2
    val halfH = sheet.height / 2
    val index = environment.ordinal
    val col = index % 2
    val row = index / 2
    return Bitmap.createBitmap(sheet, col * halfW, row * halfH, halfW, halfH)
}

@Composable
fun ApprovedEnvironmentArtwork(
    environment: PetEnvironmentArtwork,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
) {
    val context = LocalContext.current
    val bitmap = remember(environment) {
        runCatching {
            context.assets.open("pets/environment_contact_sheet.svg").use { stream ->
                val text = BufferedReader(InputStreamReader(stream)).readText()
                val sheet = loadEmbeddedJpegFromSvg(text) ?: return@runCatching null
                cropEnvironment(sheet, environment)
            }
        }.getOrNull()
    }

    if (bitmap != null) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = null,
            modifier = modifier,
            contentScale = contentScale,
        )
    } else {
        Canvas(modifier) {
            drawRect(Color(0xFFF7EAF4))
        }
    }
}

@Composable
fun StorybookPetAvatar(
    species: PetSpecies,
    state: PetVisualState = PetVisualState.IDLE,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier) {
        drawPet(species, state)
    }
}

private data class PetPalette(
    val body: Color,
    val accent: Color,
    val inner: Color,
    val ink: Color = Color(0xFF4A3543),
)

private fun palette(species: PetSpecies): PetPalette = when (species) {
    PetSpecies.PUPPY -> PetPalette(Color(0xFFFFF5E8), Color(0xFFC98E67), Color(0xFFF4C8B4))
    PetSpecies.KITTEN -> PetPalette(Color(0xFFFFF8EE), Color(0xFFD8B8A9), Color(0xFFF4C7CE))
    PetSpecies.BUNNY -> PetPalette(Color(0xFFFFF8F5), Color(0xFFE7C5D0), Color(0xFFF6BFD0))
    PetSpecies.DUCKLING -> PetPalette(Color(0xFFFFE890), Color(0xFFF0A95F), Color(0xFFFFD1A0))
    PetSpecies.HEDGEHOG -> PetPalette(Color(0xFFF6D9B7), Color(0xFF9E6F59), Color(0xFFD7A785))
    PetSpecies.FERRET -> PetPalette(Color(0xFFF4E8D8), Color(0xFF9F806A), Color(0xFFE9BEAE))
    PetSpecies.OTTER -> PetPalette(Color(0xFFD9B596), Color(0xFF8D644F), Color(0xFFF2C6B6))
    PetSpecies.FOX -> PetPalette(Color(0xFFF2A06D), Color(0xFF8D5A4E), Color(0xFFFFE0D2))
    PetSpecies.RED_PANDA -> PetPalette(Color(0xFFC96F57), Color(0xFF6B4A46), Color(0xFFFFD7C9))
    PetSpecies.AXOLOTL -> PetPalette(Color(0xFFF7C2D6), Color(0xFFE28FB5), Color(0xFFFFE2EC))
    PetSpecies.TIGER -> PetPalette(Color(0xFFF2B36D), Color(0xFF6F5148), Color(0xFFFFDBB8))
    PetSpecies.DRAGON -> PetPalette(Color(0xFFC9B8EB), Color(0xFF8B79B9), Color(0xFFF2D6E7))
}

private fun DrawScope.drawPet(species: PetSpecies, state: PetVisualState) {
    val p = palette(species)
    val w = size.width
    val h = size.height
    val cx = w * 0.5f
    val bodyCenter = Offset(cx, h * 0.67f)
    val headCenter = Offset(cx, h * 0.38f)
    val bodyRx = w * 0.25f
    val bodyRy = h * 0.22f
    val headR = minOf(w, h) * 0.235f

    // Soft illustrated shadow.
    drawOval(
        color = Color(0xFF6D5262).copy(alpha = 0.10f),
        topLeft = Offset(w * 0.20f, h * 0.84f),
        size = Size(w * 0.60f, h * 0.09f),
    )

    // Tail / silhouette accents.
    when (species) {
        PetSpecies.FOX, PetSpecies.RED_PANDA, PetSpecies.FERRET, PetSpecies.OTTER -> {
            drawOval(
                color = p.accent,
                topLeft = Offset(w * 0.63f, h * 0.59f),
                size = Size(w * 0.25f, h * 0.14f),
            )
        }
        PetSpecies.DRAGON -> {
            val wing = Path().apply {
                moveTo(w * .30f, h * .55f)
                lineTo(w * .08f, h * .43f)
                lineTo(w * .20f, h * .71f)
                close()
            }
            drawPath(wing, p.accent.copy(alpha = .80f))
            val wing2 = Path().apply {
                moveTo(w * .70f, h * .55f)
                lineTo(w * .92f, h * .43f)
                lineTo(w * .80f, h * .71f)
                close()
            }
            drawPath(wing2, p.accent.copy(alpha = .80f))
        }
        else -> Unit
    }

    // Body.
    drawOval(
        color = p.body,
        topLeft = Offset(bodyCenter.x - bodyRx, bodyCenter.y - bodyRy),
        size = Size(bodyRx * 2f, bodyRy * 2f),
    )
    drawOval(
        color = p.inner.copy(alpha = .75f),
        topLeft = Offset(cx - w * .14f, h * .61f),
        size = Size(w * .28f, h * .20f),
    )

    // Ears / species silhouette.
    when (species) {
        PetSpecies.PUPPY -> {
            drawOval(p.accent, Offset(cx - headR * 1.18f, headCenter.y - headR * .65f), Size(headR * .62f, headR * 1.25f))
            drawOval(p.accent, Offset(cx + headR * .56f, headCenter.y - headR * .65f), Size(headR * .62f, headR * 1.25f))
        }
        PetSpecies.KITTEN, PetSpecies.FOX, PetSpecies.RED_PANDA, PetSpecies.TIGER -> {
            fun ear(left: Boolean) {
                val sign = if (left) -1f else 1f
                val path = Path().apply {
                    moveTo(cx + sign * headR * .72f, headCenter.y - headR * .45f)
                    lineTo(cx + sign * headR * .52f, headCenter.y - headR * 1.08f)
                    lineTo(cx + sign * headR * .12f, headCenter.y - headR * .60f)
                    close()
                }
                drawPath(path, p.accent)
            }
            ear(true); ear(false)
        }
        PetSpecies.BUNNY -> {
            drawOval(p.body, Offset(cx - headR * .74f, headCenter.y - headR * 1.70f), Size(headR * .56f, headR * 1.38f))
            drawOval(p.body, Offset(cx + headR * .18f, headCenter.y - headR * 1.70f), Size(headR * .56f, headR * 1.38f))
            drawOval(p.inner, Offset(cx - headR * .61f, headCenter.y - headR * 1.52f), Size(headR * .28f, headR * .95f))
            drawOval(p.inner, Offset(cx + headR * .31f, headCenter.y - headR * 1.52f), Size(headR * .28f, headR * .95f))
        }
        PetSpecies.AXOLOTL -> {
            repeat(3) { i ->
                val yy = headCenter.y - headR * .55f + i * headR * .48f
                drawLine(p.accent, Offset(cx - headR * .72f, yy), Offset(cx - headR * 1.08f, yy - headR * .20f), headR * .12f)
                drawLine(p.accent, Offset(cx + headR * .72f, yy), Offset(cx + headR * 1.08f, yy - headR * .20f), headR * .12f)
            }
        }
        PetSpecies.DRAGON -> {
            val hornL = Path().apply {
                moveTo(cx - headR * .45f, headCenter.y - headR * .65f)
                lineTo(cx - headR * .62f, headCenter.y - headR * 1.18f)
                lineTo(cx - headR * .16f, headCenter.y - headR * .74f)
                close()
            }
            val hornR = Path().apply {
                moveTo(cx + headR * .45f, headCenter.y - headR * .65f)
                lineTo(cx + headR * .62f, headCenter.y - headR * 1.18f)
                lineTo(cx + headR * .16f, headCenter.y - headR * .74f)
                close()
            }
            drawPath(hornL, p.inner)
            drawPath(hornR, p.inner)
        }
        PetSpecies.HEDGEHOG -> {
            repeat(7) { i ->
                val x = cx - headR * .80f + i * headR * .27f
                val spike = Path().apply {
                    moveTo(x, headCenter.y - headR * .50f)
                    lineTo(x + headR * .12f, headCenter.y - headR * 1.0f)
                    lineTo(x + headR * .25f, headCenter.y - headR * .48f)
                    close()
                }
                drawPath(spike, p.accent)
            }
        }
        PetSpecies.DUCKLING, PetSpecies.FERRET, PetSpecies.OTTER -> Unit
    }

    // Head.
    drawCircle(p.body, headR, headCenter)

    // Species face accents.
    if (species == PetSpecies.TIGER) {
        repeat(3) { i ->
            val dx = (i - 1) * headR * .42f
            drawLine(p.accent, Offset(cx + dx, headCenter.y - headR * .80f), Offset(cx + dx * .70f, headCenter.y - headR * .46f), headR * .08f)
        }
    }
    if (species == PetSpecies.RED_PANDA) {
        drawOval(Color(0xFFFFEFE7), Offset(cx - headR * .83f, headCenter.y - headR * .30f), Size(headR * .65f, headR * .62f))
        drawOval(Color(0xFFFFEFE7), Offset(cx + headR * .18f, headCenter.y - headR * .30f), Size(headR * .65f, headR * .62f))
    }

    // Eyes and expression.
    val eyeY = headCenter.y - headR * .08f
    val eyeDx = headR * .34f
    when (state) {
        PetVisualState.HAPPY, PetVisualState.PLAYING -> {
            drawArc(p.ink, 205f, 130f, false, Offset(cx - eyeDx - headR * .14f, eyeY - headR * .10f), Size(headR * .28f, headR * .20f), style = Stroke(headR * .055f))
            drawArc(p.ink, 205f, 130f, false, Offset(cx + eyeDx - headR * .14f, eyeY - headR * .10f), Size(headR * .28f, headR * .20f), style = Stroke(headR * .055f))
        }
        else -> {
            drawCircle(p.ink, headR * .065f, Offset(cx - eyeDx, eyeY))
            drawCircle(p.ink, headR * .065f, Offset(cx + eyeDx, eyeY))
            drawCircle(Color.White.copy(alpha = .9f), headR * .018f, Offset(cx - eyeDx - headR * .015f, eyeY - headR * .015f))
            drawCircle(Color.White.copy(alpha = .9f), headR * .018f, Offset(cx + eyeDx - headR * .015f, eyeY - headR * .015f))
        }
    }

    // Nose/beak.
    when (species) {
        PetSpecies.DUCKLING -> {
            drawOval(Color(0xFFF09A5A), Offset(cx - headR * .27f, headCenter.y + headR * .14f), Size(headR * .54f, headR * .24f))
        }
        else -> {
            drawCircle(p.accent.copy(alpha = .88f), headR * .085f, Offset(cx, headCenter.y + headR * .16f))
        }
    }

    // Mouth.
    when (state) {
        PetVisualState.HUNGRY -> {
            drawArc(p.ink, 20f, 140f, false, Offset(cx - headR * .22f, headCenter.y + headR * .18f), Size(headR * .44f, headR * .32f), style = Stroke(headR * .045f))
        }
        PetVisualState.HAPPY, PetVisualState.EATING, PetVisualState.PLAYING -> {
            drawArc(p.ink, 200f, 140f, false, Offset(cx - headR * .22f, headCenter.y + headR * .13f), Size(headR * .44f, headR * .35f), style = Stroke(headR * .045f))
        }
        else -> {
            drawLine(p.ink, Offset(cx - headR * .15f, headCenter.y + headR * .33f), Offset(cx, headCenter.y + headR * .28f), headR * .04f)
            drawLine(p.ink, Offset(cx, headCenter.y + headR * .28f), Offset(cx + headR * .15f, headCenter.y + headR * .33f), headR * .04f)
        }
    }

    // Blush.
    drawCircle(Color(0xFFF4AFC2).copy(alpha = .46f), headR * .10f, Offset(cx - headR * .62f, headCenter.y + headR * .22f))
    drawCircle(Color(0xFFF4AFC2).copy(alpha = .46f), headR * .10f, Offset(cx + headR * .62f, headCenter.y + headR * .22f))

    // State overlays.
    if (state == PetVisualState.DIRTY) {
        drawCircle(Color(0xFF8F7A68).copy(alpha = .42f), headR * .15f, Offset(cx + headR * .55f, headCenter.y - headR * .42f))
        drawCircle(Color(0xFF8F7A68).copy(alpha = .32f), headR * .11f, Offset(cx - headR * .50f, headCenter.y + headR * .46f))
        drawCircle(Color(0xFF8F7A68).copy(alpha = .30f), headR * .10f, Offset(cx + headR * .18f, bodyCenter.y + bodyRy * .10f))
    }
    if (state == PetVisualState.BATHING) {
        repeat(5) { i ->
            drawCircle(Color.White.copy(alpha = .86f), headR * (.06f + i * .008f), Offset(cx - headR * .72f + i * headR * .38f, headCenter.y - headR * .70f + (i % 2) * headR * .20f))
        }
    }
    if (state == PetVisualState.EATING) {
        drawOval(Color(0xFFF3C2D0), Offset(cx - w * .20f, h * .83f), Size(w * .40f, h * .10f))
        drawOval(Color(0xFFFFF7EE), Offset(cx - w * .16f, h * .835f), Size(w * .32f, h * .055f))
    }
    if (state == PetVisualState.CARRIED) {
        drawCircle(Color.White.copy(alpha = .72f), minOf(w, h) * .46f, Offset(cx, h * .48f), style = Stroke(minOf(w, h) * .018f))
    }
}

@Composable
fun PetEggArtwork(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        drawOval(Color(0xFFC89163).copy(alpha = .75f), Offset(w * .18f, h * .76f), Size(w * .64f, h * .12f))
        drawOval(Color(0xFFFFF7EC), Offset(w * .24f, h * .08f), Size(w * .52f, h * .72f))
        drawOval(Color(0xFFF7D8E5), Offset(w * .30f, h * .14f), Size(w * .40f, h * .58f), style = Stroke(width = 2.dp.toPx()))
        drawCircle(Color(0xFFF5B3C6), w * .045f, Offset(w * .40f, h * .32f))
        drawCircle(Color(0xFFD7C4EE), w * .035f, Offset(w * .61f, h * .43f))
        drawCircle(Color(0xFFF2C79B), w * .030f, Offset(w * .47f, h * .57f))
        val crack = Path().apply {
            moveTo(w * .53f, h * .22f)
            lineTo(w * .47f, h * .34f)
            lineTo(w * .55f, h * .43f)
            lineTo(w * .48f, h * .54f)
            lineTo(w * .54f, h * .65f)
        }
        drawPath(crack, Color(0xFFD7A889), style = Stroke(width = 2.dp.toPx()))
    }
}

@Composable
fun PetCareArtwork(
    kind: String,
    modifier: Modifier = Modifier,
) {
    Box(modifier, contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            when (kind.lowercase()) {
                "feed" -> {
                    drawOval(Color(0xFFC98B8E), Offset(w * .10f, h * .52f), Size(w * .80f, h * .30f))
                    drawOval(Color(0xFFFFF7ED), Offset(w * .18f, h * .48f), Size(w * .64f, h * .22f))
                    repeat(6) { i ->
                        drawCircle(Color(0xFFB27A58), w * .055f, Offset(w * (.30f + (i % 3) * .20f), h * (.54f + (i / 3) * .08f)))
                    }
                }
                "clean" -> {
                    drawRoundRect(Color(0xFFFFD78B), Offset(w * .18f, h * .35f), Size(w * .46f, h * .34f), androidx.compose.ui.geometry.CornerRadius(w * .08f))
                    drawRect(Color(0xFFF4B25D), Offset(w * .18f, h * .55f), Size(w * .46f, h * .14f))
                    repeat(3) { i ->
                        drawCircle(Color.White.copy(alpha = .90f), w * (.07f + i * .015f), Offset(w * (.62f + i * .08f), h * (.26f + (i % 2) * .15f)))
                    }
                }
                else -> {
                    drawCircle(Color(0xFF9BD6C8), w * .30f, Offset(w * .50f, h * .50f))
                    drawCircle(Color(0xFFFFF8F4), w * .20f, Offset(w * .50f, h * .50f))
                    drawLine(Color(0xFF8A6B7E), Offset(w * .68f, h * .30f), Offset(w * .87f, h * .11f), w * .06f)
                    drawLine(Color(0xFF8A6B7E), Offset(w * .32f, h * .70f), Offset(w * .13f, h * .89f), w * .06f)
                }
            }
        }
    }
}
