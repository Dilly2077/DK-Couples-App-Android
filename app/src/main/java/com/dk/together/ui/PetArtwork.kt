package com.dk.together.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.dk.together.pets.engine.PetSpecies

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

/**
 * Runtime storybook environments.
 *
 * The old v0.6 implementation tried to decode a JPEG that had been embedded in an SVG asset.
 * That embedded payload in the repository is not a valid JPEG, so BitmapFactory returned null
 * and the app silently displayed a flat pink fallback. These scenes are now rendered directly
 * by Compose and therefore cannot fail because of a missing/corrupt bitmap asset.
 */
@Composable
fun ApprovedEnvironmentArtwork(
    environment: PetEnvironmentArtwork,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
) {
    @Suppress("UNUSED_VARIABLE")
    val ignoredContentScale = contentScale
    Canvas(modifier) {
        when (environment) {
            PetEnvironmentArtwork.PLAYROOM -> drawPlayroomScene()
            PetEnvironmentArtwork.KITCHEN -> drawKitchenScene()
            PetEnvironmentArtwork.GARDEN -> drawGardenScene()
            PetEnvironmentArtwork.SPA -> drawSpaScene()
        }
    }
}

private fun DrawScope.drawPlayroomScene() {
    val w = size.width
    val h = size.height
    drawRect(Brush.verticalGradient(listOf(Color(0xFFF8E7F3), Color(0xFFE9DDF8), Color(0xFFFFF5EF))))
    drawRect(Color(0xFFF2D6C3), Offset(0f, h * .73f), Size(w, h * .27f))

    // Heart-shaped window impression.
    val windowCenter = Offset(w * .50f, h * .28f)
    drawCircle(Color(0xFFFFFCFA), w * .115f, Offset(windowCenter.x - w * .065f, windowCenter.y))
    drawCircle(Color(0xFFFFFCFA), w * .115f, Offset(windowCenter.x + w * .065f, windowCenter.y))
    val heartPoint = Path().apply {
        moveTo(w * .31f, h * .27f)
        lineTo(w * .69f, h * .27f)
        lineTo(w * .50f, h * .55f)
        close()
    }
    drawPath(heartPoint, Color(0xFFFFFCFA))
    drawCircle(Color(0xFFDDEBF6), w * .092f, Offset(windowCenter.x - w * .052f, windowCenter.y))
    drawCircle(Color(0xFFDDEBF6), w * .092f, Offset(windowCenter.x + w * .052f, windowCenter.y))
    val skyPoint = Path().apply {
        moveTo(w * .35f, h * .28f)
        lineTo(w * .65f, h * .28f)
        lineTo(w * .50f, h * .49f)
        close()
    }
    drawPath(skyPoint, Color(0xFFDDEBF6))

    // Shelves and plants.
    drawRoundRect(Color(0xFFFFF9F5), Offset(w * .05f, h * .15f), Size(w * .22f, h * .18f), CornerRadius(18.dp.toPx()))
    drawRect(Color(0xFFC69272), Offset(w * .07f, h * .25f), Size(w * .18f, h * .018f))
    repeat(3) { i ->
        drawCircle(Color(0xFFF1B9CF), w * .018f, Offset(w * (.10f + i * .05f), h * .21f))
        drawLine(Color(0xFF7FAE7B), Offset(w * (.10f + i * .05f), h * .23f), Offset(w * (.10f + i * .05f), h * .255f), 2.dp.toPx())
    }

    // Bed.
    drawOval(Color(0xFFCFB7E9), Offset(w * .06f, h * .66f), Size(w * .25f, h * .17f))
    drawOval(Color(0xFFFFF8F3), Offset(w * .085f, h * .68f), Size(w * .20f, h * .11f))

    // Tunnel.
    drawRoundRect(Color(0xFFE8AFCB), Offset(w * .71f, h * .62f), Size(w * .23f, h * .18f), CornerRadius(34.dp.toPx()))
    drawOval(Color(0xFFB58BC1), Offset(w * .72f, h * .625f), Size(w * .085f, h * .17f))
    drawOval(Color(0xFFF4DDE9), Offset(w * .735f, h * .64f), Size(w * .055f, h * .14f))

    // Toy basket and toys.
    drawRoundRect(Color(0xFFC89573), Offset(w * .72f, h * .78f), Size(w * .18f, h * .12f), CornerRadius(12.dp.toPx()))
    drawCircle(Color(0xFF9BD2C8), w * .028f, Offset(w * .75f, h * .77f))
    drawCircle(Color(0xFFF4C06F), w * .025f, Offset(w * .82f, h * .76f))
    drawLine(Color(0xFFB48A7A), Offset(w * .87f, h * .72f), Offset(w * .84f, h * .79f), 6.dp.toPx())

    // Central rug.
    drawOval(Color(0xFFF8DCE8), Offset(w * .28f, h * .67f), Size(w * .44f, h * .22f))
    drawOval(Color(0xFFFFF7F2), Offset(w * .33f, h * .70f), Size(w * .34f, h * .15f))

    // Decorative flowers.
    repeat(7) { i ->
        val x = w * (.08f + i * .14f)
        val y = h * (.91f - (i % 2) * .03f)
        drawCircle(if (i % 2 == 0) Color(0xFFF3B6CB) else Color.White, w * .010f, Offset(x, y))
        drawCircle(Color(0xFFF0D07A), w * .0045f, Offset(x, y))
    }
}

private fun DrawScope.drawKitchenScene() {
    val w = size.width
    val h = size.height
    drawRect(Brush.verticalGradient(listOf(Color(0xFFFFE8F1), Color(0xFFFFF8F1), Color(0xFFF2D9C7))))
    drawRect(Color(0xFFFFFCF8), Offset(0f, h * .12f), Size(w, h * .49f))
    drawRect(Color(0xFFD6A27A), Offset(0f, h * .60f), Size(w, h * .04f))
    drawRect(Color(0xFFF0D3BE), Offset(0f, h * .64f), Size(w, h * .36f))

    // Cabinets + worktop.
    drawRoundRect(Color(0xFFFFFDF9), Offset(w * .04f, h * .27f), Size(w * .50f, h * .31f), CornerRadius(18.dp.toPx()))
    drawRect(Color(0xFFC78F69), Offset(w * .035f, h * .25f), Size(w * .53f, h * .045f))
    repeat(3) { i ->
        drawLine(Color(0xFFE8D9CF), Offset(w * (.20f + i * .17f), h * .31f), Offset(w * (.20f + i * .17f), h * .56f), 2.dp.toPx())
    }
    repeat(3) { i ->
        drawCircle(Color(0xFFC9A98E), w * .008f, Offset(w * (.12f + i * .17f), h * .43f))
    }

    // Pink retro fridge.
    drawRoundRect(Color(0xFFF3AFC4), Offset(w * .72f, h * .17f), Size(w * .22f, h * .42f), CornerRadius(26.dp.toPx()))
    drawLine(Color(0xFFFFE8EF), Offset(w * .73f, h * .38f), Offset(w * .93f, h * .38f), 3.dp.toPx())
    drawRoundRect(Color(0xFFC58D9F), Offset(w * .75f, h * .27f), Size(w * .018f, h * .07f), CornerRadius(8.dp.toPx()))
    drawRoundRect(Color(0xFFC58D9F), Offset(w * .75f, h * .43f), Size(w * .018f, h * .08f), CornerRadius(8.dp.toPx()))

    // Window and trailing plant.
    drawRoundRect(Color(0xFFE4F1F4), Offset(w * .34f, h * .08f), Size(w * .25f, h * .14f), CornerRadius(18.dp.toPx()))
    drawLine(Color.White, Offset(w * .465f, h * .085f), Offset(w * .465f, h * .215f), 3.dp.toPx())
    drawLine(Color.White, Offset(w * .345f, h * .15f), Offset(w * .585f, h * .15f), 3.dp.toPx())
    repeat(5) { i ->
        drawCircle(Color(0xFF8FBE83), w * .018f, Offset(w * (.60f + (i % 2) * .025f), h * (.13f + i * .035f)))
    }

    // Feeding station and seats.
    drawRoundRect(Color(0xFFD2A17B), Offset(w * .08f, h * .72f), Size(w * .23f, h * .17f), CornerRadius(20.dp.toPx()))
    drawRoundRect(Color(0xFFD2A17B), Offset(w * .69f, h * .72f), Size(w * .23f, h * .17f), CornerRadius(20.dp.toPx()))
    drawOval(Color(0xFFE8AFC0), Offset(w * .38f, h * .77f), Size(w * .24f, h * .09f))
    drawOval(Color(0xFFFFF7EE), Offset(w * .405f, h * .78f), Size(w * .19f, h * .055f))

    // Flowers on counter.
    drawRoundRect(Color(0xFFF4D3E0), Offset(w * .11f, h * .18f), Size(w * .07f, h * .08f), CornerRadius(10.dp.toPx()))
    repeat(4) { i ->
        drawCircle(if (i % 2 == 0) Color(0xFFF4AFC5) else Color.White, w * .014f, Offset(w * (.12f + i * .015f), h * (.17f - (i % 2) * .02f)))
    }
}

private fun DrawScope.drawGardenScene() {
    val w = size.width
    val h = size.height
    drawRect(Brush.verticalGradient(listOf(Color(0xFFFFE7F1), Color(0xFFE8E3F8), Color(0xFFD2E9C4))))

    // Distant lavender hills.
    val hills = Path().apply {
        moveTo(0f, h * .37f)
        quadraticBezierTo(w * .19f, h * .22f, w * .39f, h * .38f)
        quadraticBezierTo(w * .65f, h * .17f, w, h * .36f)
        lineTo(w, h * .57f)
        lineTo(0f, h * .57f)
        close()
    }
    drawPath(hills, Color(0xFFC4B7E3))

    val lawn = Path().apply {
        moveTo(0f, h * .44f)
        quadraticBezierTo(w * .22f, h * .34f, w * .48f, h * .49f)
        quadraticBezierTo(w * .76f, h * .35f, w, h * .47f)
        lineTo(w, h)
        lineTo(0f, h)
        close()
    }
    drawPath(lawn, Color(0xFFCDE8BB))

    // Blossom tree.
    drawRect(Color(0xFF9A6C55), Offset(w * .10f, h * .31f), Size(w * .065f, h * .39f))
    drawCircle(Color(0xFF8CC77F), w * .14f, Offset(w * .13f, h * .25f))
    drawCircle(Color(0xFFA4D58C), w * .12f, Offset(w * .22f, h * .27f))
    repeat(8) { i ->
        drawCircle(Color(0xFFF3B7CD), w * .012f, Offset(w * (.07f + (i % 4) * .055f), h * (.18f + (i / 4) * .09f)))
    }

    // Pet cottage.
    drawRoundRect(Color(0xFFFFF4F3), Offset(w * .73f, h * .33f), Size(w * .20f, h * .23f), CornerRadius(22.dp.toPx()))
    val roof = Path().apply {
        moveTo(w * .69f, h * .37f)
        lineTo(w * .83f, h * .25f)
        lineTo(w * .97f, h * .37f)
        close()
    }
    drawPath(roof, Color(0xFFEFA8C8))
    drawRoundRect(Color(0xFFC78FB3), Offset(w * .80f, h * .44f), Size(w * .065f, h * .12f), CornerRadius(15.dp.toPx()))

    // Pond at edge.
    drawOval(Color(0xFF86D1DD), Offset(w * .59f, h * .63f), Size(w * .34f, h * .15f))
    drawOval(Color(0xFFBCE7EA), Offset(w * .63f, h * .65f), Size(w * .26f, h * .09f))
    drawCircle(Color(0xFFF3B8CF), w * .013f, Offset(w * .73f, h * .68f))

    // Fence.
    repeat(7) { i ->
        val x = w * (.03f + i * .155f)
        drawRoundRect(Color(0xFFD0A17D), Offset(x, h * .82f), Size(w * .035f, h * .12f), CornerRadius(6.dp.toPx()))
    }
    drawRect(Color(0xFFD0A17D), Offset(0f, h * .86f), Size(w, h * .025f))

    // Flowers and toys around the perimeter.
    repeat(12) { i ->
        val x = w * (.05f + (i % 6) * .18f)
        val y = h * (.74f + (i / 6) * .16f)
        drawCircle(if (i % 3 == 0) Color.White else Color(0xFFF0ACC7), w * .010f, Offset(x, y))
        drawCircle(Color(0xFFF0D16E), w * .004f, Offset(x, y))
    }
    drawCircle(Color(0xFFF2C96A), w * .025f, Offset(w * .40f, h * .83f))
    drawLine(Color(0xFF9F7B67), Offset(w * .49f, h * .82f), Offset(w * .55f, h * .88f), 7.dp.toPx())
}

private fun DrawScope.drawSpaScene() {
    val w = size.width
    val h = size.height
    drawRect(Brush.verticalGradient(listOf(Color(0xFFFFE8F2), Color(0xFFE7E3F7), Color(0xFFD0E8BF))))

    // Garden background.
    drawCircle(Color(0xFF93C785), w * .18f, Offset(w * .12f, h * .30f))
    drawCircle(Color(0xFFAAD795), w * .14f, Offset(w * .88f, h * .28f))
    drawRect(Color(0xFFCFA27E), Offset(0f, h * .72f), Size(w, h * .035f))

    // Hero wooden hot tub.
    drawOval(Color(0xFF8FD7E1), Offset(w * .23f, h * .40f), Size(w * .54f, h * .27f))
    drawOval(Color(0xFFCBF0F2), Offset(w * .28f, h * .43f), Size(w * .44f, h * .18f))
    drawRoundRect(Color(0xFFB98763), Offset(w * .20f, h * .55f), Size(w * .60f, h * .25f), CornerRadius(36.dp.toPx()))
    drawOval(Color(0xFF8FD7E1), Offset(w * .23f, h * .43f), Size(w * .54f, h * .22f))
    drawOval(Color(0xFFCAF0F3), Offset(w * .28f, h * .46f), Size(w * .44f, h * .15f))
    repeat(8) { i ->
        val x = w * (.23f + i * .075f)
        drawLine(Color(0xFF9E704F), Offset(x, h * .58f), Offset(x, h * .77f), 2.dp.toPx())
    }

    // Bubbles.
    repeat(10) { i ->
        val x = w * (.31f + (i % 5) * .095f)
        val y = h * (.44f + (i / 5) * .10f)
        drawCircle(Color.White.copy(alpha = .82f), w * (.012f + (i % 3) * .004f), Offset(x, y))
    }

    // Steps.
    drawRoundRect(Color(0xFFC79A74), Offset(w * .73f, h * .70f), Size(w * .18f, h * .08f), CornerRadius(10.dp.toPx()))
    drawRoundRect(Color(0xFFB98662), Offset(w * .77f, h * .78f), Size(w * .18f, h * .07f), CornerRadius(10.dp.toPx()))

    // Cleaning station: towels, soap, sponge.
    drawRoundRect(Color(0xFFFFFCF7), Offset(w * .05f, h * .55f), Size(w * .16f, h * .28f), CornerRadius(18.dp.toPx()))
    drawRoundRect(Color(0xFFF2C4D4), Offset(w * .075f, h * .60f), Size(w * .11f, h * .045f), CornerRadius(8.dp.toPx()))
    drawRoundRect(Color(0xFFF7E1B0), Offset(w * .075f, h * .67f), Size(w * .11f, h * .07f), CornerRadius(8.dp.toPx()))
    drawRoundRect(Color(0xFFB8DCCF), Offset(w * .10f, h * .48f), Size(w * .055f, h * .11f), CornerRadius(10.dp.toPx()))
    drawRect(Color(0xFF8EB8AA), Offset(w * .117f, h * .45f), Size(w * .021f, h * .04f))

    repeat(8) { i ->
        drawCircle(if (i % 2 == 0) Color(0xFFF1B4CA) else Color.White, w * .009f, Offset(w * (.08f + i * .11f), h * .90f))
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
    val cx = w * .5f
    val bodyCenter = Offset(cx, h * .67f)
    val headCenter = Offset(cx, h * .38f)
    val bodyRx = w * .25f
    val bodyRy = h * .22f
    val headR = minOf(w, h) * .235f

    drawOval(Color(0xFF6D5262).copy(alpha = .10f), Offset(w * .20f, h * .84f), Size(w * .60f, h * .09f))

    when (species) {
        PetSpecies.FOX, PetSpecies.RED_PANDA, PetSpecies.FERRET, PetSpecies.OTTER ->
            drawOval(p.accent, Offset(w * .63f, h * .59f), Size(w * .25f, h * .14f))
        PetSpecies.DRAGON -> {
            val left = Path().apply {
                moveTo(w * .30f, h * .55f); lineTo(w * .08f, h * .43f); lineTo(w * .20f, h * .71f); close()
            }
            val right = Path().apply {
                moveTo(w * .70f, h * .55f); lineTo(w * .92f, h * .43f); lineTo(w * .80f, h * .71f); close()
            }
            drawPath(left, p.accent.copy(alpha = .80f)); drawPath(right, p.accent.copy(alpha = .80f))
        }
        else -> Unit
    }

    drawOval(p.body, Offset(bodyCenter.x - bodyRx, bodyCenter.y - bodyRy), Size(bodyRx * 2f, bodyRy * 2f))
    drawOval(p.inner.copy(alpha = .75f), Offset(cx - w * .14f, h * .61f), Size(w * .28f, h * .20f))

    when (species) {
        PetSpecies.PUPPY -> {
            drawOval(p.accent, Offset(cx - headR * 1.18f, headCenter.y - headR * .65f), Size(headR * .62f, headR * 1.25f))
            drawOval(p.accent, Offset(cx + headR * .56f, headCenter.y - headR * .65f), Size(headR * .62f, headR * 1.25f))
        }
        PetSpecies.KITTEN, PetSpecies.FOX, PetSpecies.RED_PANDA, PetSpecies.TIGER -> {
            fun ear(sign: Float) {
                val path = Path().apply {
                    moveTo(cx + sign * headR * .72f, headCenter.y - headR * .45f)
                    lineTo(cx + sign * headR * .52f, headCenter.y - headR * 1.08f)
                    lineTo(cx + sign * headR * .12f, headCenter.y - headR * .60f)
                    close()
                }
                drawPath(path, p.accent)
            }
            ear(-1f); ear(1f)
        }
        PetSpecies.BUNNY -> {
            drawOval(p.body, Offset(cx - headR * .74f, headCenter.y - headR * 1.70f), Size(headR * .56f, headR * 1.38f))
            drawOval(p.body, Offset(cx + headR * .18f, headCenter.y - headR * 1.70f), Size(headR * .56f, headR * 1.38f))
            drawOval(p.inner, Offset(cx - headR * .61f, headCenter.y - headR * 1.52f), Size(headR * .28f, headR * .95f))
            drawOval(p.inner, Offset(cx + headR * .31f, headCenter.y - headR * 1.52f), Size(headR * .28f, headR * .95f))
        }
        PetSpecies.AXOLOTL -> repeat(3) { i ->
            val y = headCenter.y - headR * .55f + i * headR * .48f
            drawLine(p.accent, Offset(cx - headR * .72f, y), Offset(cx - headR * 1.08f, y - headR * .20f), headR * .12f)
            drawLine(p.accent, Offset(cx + headR * .72f, y), Offset(cx + headR * 1.08f, y - headR * .20f), headR * .12f)
        }
        PetSpecies.HEDGEHOG -> repeat(7) { i ->
            val x = cx - headR * .80f + i * headR * .27f
            val spike = Path().apply {
                moveTo(x, headCenter.y - headR * .50f)
                lineTo(x + headR * .12f, headCenter.y - headR)
                lineTo(x + headR * .25f, headCenter.y - headR * .48f)
                close()
            }
            drawPath(spike, p.accent)
        }
        PetSpecies.DRAGON -> {
            val l = Path().apply { moveTo(cx - headR * .45f, headCenter.y - headR * .65f); lineTo(cx - headR * .62f, headCenter.y - headR * 1.18f); lineTo(cx - headR * .16f, headCenter.y - headR * .74f); close() }
            val r = Path().apply { moveTo(cx + headR * .45f, headCenter.y - headR * .65f); lineTo(cx + headR * .62f, headCenter.y - headR * 1.18f); lineTo(cx + headR * .16f, headCenter.y - headR * .74f); close() }
            drawPath(l, p.inner); drawPath(r, p.inner)
        }
        PetSpecies.DUCKLING, PetSpecies.FERRET, PetSpecies.OTTER -> Unit
    }

    drawCircle(p.body, headR, headCenter)

    if (species == PetSpecies.TIGER) repeat(3) { i ->
        val dx = (i - 1) * headR * .42f
        drawLine(p.accent, Offset(cx + dx, headCenter.y - headR * .80f), Offset(cx + dx * .70f, headCenter.y - headR * .46f), headR * .08f)
    }
    if (species == PetSpecies.RED_PANDA) {
        drawOval(Color(0xFFFFEFE7), Offset(cx - headR * .83f, headCenter.y - headR * .30f), Size(headR * .65f, headR * .62f))
        drawOval(Color(0xFFFFEFE7), Offset(cx + headR * .18f, headCenter.y - headR * .30f), Size(headR * .65f, headR * .62f))
    }

    val eyeY = headCenter.y - headR * .08f
    val eyeDx = headR * .34f
    if (state == PetVisualState.HAPPY || state == PetVisualState.PLAYING) {
        drawArc(p.ink, 205f, 130f, false, Offset(cx - eyeDx - headR * .14f, eyeY - headR * .10f), Size(headR * .28f, headR * .20f), style = Stroke(headR * .055f))
        drawArc(p.ink, 205f, 130f, false, Offset(cx + eyeDx - headR * .14f, eyeY - headR * .10f), Size(headR * .28f, headR * .20f), style = Stroke(headR * .055f))
    } else {
        drawCircle(p.ink, headR * .065f, Offset(cx - eyeDx, eyeY))
        drawCircle(p.ink, headR * .065f, Offset(cx + eyeDx, eyeY))
        drawCircle(Color.White, headR * .018f, Offset(cx - eyeDx - headR * .015f, eyeY - headR * .015f))
        drawCircle(Color.White, headR * .018f, Offset(cx + eyeDx - headR * .015f, eyeY - headR * .015f))
    }

    if (species == PetSpecies.DUCKLING) {
        drawOval(Color(0xFFF09A5A), Offset(cx - headR * .27f, headCenter.y + headR * .14f), Size(headR * .54f, headR * .24f))
    } else {
        drawCircle(p.accent.copy(alpha = .88f), headR * .085f, Offset(cx, headCenter.y + headR * .16f))
    }

    when (state) {
        PetVisualState.HUNGRY -> drawArc(p.ink, 20f, 140f, false, Offset(cx - headR * .22f, headCenter.y + headR * .18f), Size(headR * .44f, headR * .32f), style = Stroke(headR * .045f))
        PetVisualState.HAPPY, PetVisualState.EATING, PetVisualState.PLAYING -> drawArc(p.ink, 200f, 140f, false, Offset(cx - headR * .22f, headCenter.y + headR * .13f), Size(headR * .44f, headR * .35f), style = Stroke(headR * .045f))
        else -> {
            drawLine(p.ink, Offset(cx - headR * .15f, headCenter.y + headR * .33f), Offset(cx, headCenter.y + headR * .28f), headR * .04f)
            drawLine(p.ink, Offset(cx, headCenter.y + headR * .28f), Offset(cx + headR * .15f, headCenter.y + headR * .33f), headR * .04f)
        }
    }

    drawCircle(Color(0xFFF4AFC2).copy(alpha = .46f), headR * .10f, Offset(cx - headR * .62f, headCenter.y + headR * .22f))
    drawCircle(Color(0xFFF4AFC2).copy(alpha = .46f), headR * .10f, Offset(cx + headR * .62f, headCenter.y + headR * .22f))

    if (state == PetVisualState.DIRTY) {
        drawCircle(Color(0xFF8F7A68).copy(alpha = .42f), headR * .15f, Offset(cx + headR * .55f, headCenter.y - headR * .42f))
        drawCircle(Color(0xFF8F7A68).copy(alpha = .32f), headR * .11f, Offset(cx - headR * .50f, headCenter.y + headR * .46f))
        drawCircle(Color(0xFF8F7A68).copy(alpha = .30f), headR * .10f, Offset(cx + headR * .18f, bodyCenter.y + bodyRy * .10f))
    }
    if (state == PetVisualState.BATHING) repeat(5) { i ->
        drawCircle(Color.White.copy(alpha = .86f), headR * (.06f + i * .008f), Offset(cx - headR * .72f + i * headR * .38f, headCenter.y - headR * .70f + (i % 2) * headR * .20f))
    }
    if (state == PetVisualState.EATING) {
        drawOval(Color(0xFFF3C2D0), Offset(cx - w * .20f, h * .83f), Size(w * .40f, h * .10f))
        drawOval(Color(0xFFFFF7EE), Offset(cx - w * .16f, h * .835f), Size(w * .32f, h * .055f))
    }
    if (state == PetVisualState.CARRIED) drawCircle(Color.White.copy(alpha = .72f), minOf(w, h) * .46f, Offset(cx, h * .48f), style = Stroke(minOf(w, h) * .018f))
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
            moveTo(w * .53f, h * .22f); lineTo(w * .47f, h * .34f); lineTo(w * .55f, h * .43f); lineTo(w * .48f, h * .54f); lineTo(w * .54f, h * .65f)
        }
        drawPath(crack, Color(0xFFD7A889), style = Stroke(width = 2.dp.toPx()))
    }
}

@Composable
fun PetCareArtwork(kind: String, modifier: Modifier = Modifier) {
    Box(modifier, contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            when (kind.lowercase()) {
                "feed" -> {
                    drawOval(Color(0xFFC98B8E), Offset(w * .10f, h * .52f), Size(w * .80f, h * .30f))
                    drawOval(Color(0xFFFFF7ED), Offset(w * .18f, h * .48f), Size(w * .64f, h * .22f))
                    repeat(6) { i -> drawCircle(Color(0xFFB27A58), w * .055f, Offset(w * (.30f + (i % 3) * .20f), h * (.54f + (i / 3) * .08f))) }
                }
                "clean" -> {
                    drawRoundRect(Color(0xFFFFD78B), Offset(w * .18f, h * .35f), Size(w * .46f, h * .34f), CornerRadius(w * .08f))
                    drawRect(Color(0xFFF4B25D), Offset(w * .18f, h * .55f), Size(w * .46f, h * .14f))
                    repeat(3) { i -> drawCircle(Color.White.copy(alpha = .90f), w * (.07f + i * .015f), Offset(w * (.62f + i * .08f), h * (.26f + (i % 2) * .15f))) }
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
