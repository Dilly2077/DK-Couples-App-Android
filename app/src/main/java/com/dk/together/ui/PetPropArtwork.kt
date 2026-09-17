package com.dk.together.ui

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import com.dk.together.pets.feeding.FoodId
import com.dk.together.pets.play.PlayToy

@Composable
fun PetToyArtwork(toy: PlayToy, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        when (toy) {
            PlayToy.BALL -> {
                drawCircle(Color(0xFF9BD6C8), w * .34f, Offset(w * .50f, h * .50f))
                drawArc(Color(0xFFFFF8F4), 20f, 145f, false, Offset(w * .18f, h * .20f), Size(w * .64f, h * .64f), style = Stroke(w * .06f))
                drawArc(Color(0xFFFFF8F4), 200f, 145f, false, Offset(w * .18f, h * .20f), Size(w * .64f, h * .64f), style = Stroke(w * .06f))
            }
            PlayToy.ROPE -> {
                val rope = Path().apply {
                    moveTo(w * .16f, h * .60f)
                    cubicTo(w * .30f, h * .22f, w * .52f, h * .78f, w * .84f, h * .38f)
                }
                drawPath(rope, Color(0xFFD8A56F), style = Stroke(w * .11f))
                drawCircle(Color(0xFFF4B9C8), w * .10f, Offset(w * .16f, h * .60f))
                drawCircle(Color(0xFFBCA9E1), w * .10f, Offset(w * .84f, h * .38f))
            }
            PlayToy.PLUSH -> {
                drawCircle(Color(0xFFF2C7A5), w * .27f, Offset(w * .50f, h * .42f))
                drawCircle(Color(0xFFE0A97D), w * .12f, Offset(w * .30f, h * .25f))
                drawCircle(Color(0xFFE0A97D), w * .12f, Offset(w * .70f, h * .25f))
                drawOval(Color(0xFFF2C7A5), Offset(w * .30f, h * .51f), Size(w * .40f, h * .34f))
                drawCircle(Color(0xFF4A3543), w * .025f, Offset(w * .42f, h * .40f))
                drawCircle(Color(0xFF4A3543), w * .025f, Offset(w * .58f, h * .40f))
                drawCircle(Color(0xFFC8837B), w * .035f, Offset(w * .50f, h * .50f))
            }
            PlayToy.FRISBEE -> {
                drawOval(Color(0xFFF0AFC8), Offset(w * .12f, h * .28f), Size(w * .76f, h * .44f))
                drawOval(Color(0xFFFFDCE8), Offset(w * .25f, h * .36f), Size(w * .50f, h * .27f))
                drawOval(Color.White.copy(alpha = .60f), Offset(w * .36f, h * .39f), Size(w * .28f, h * .12f))
            }
        }
    }
}

@Composable
fun PetFoodArtwork(food: FoodId, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        when (food) {
            FoodId.KIBBLE -> {
                drawOval(Color(0xFFBC7F81), Offset(w * .08f, h * .52f), Size(w * .84f, h * .34f))
                drawOval(Color(0xFFFFF6EC), Offset(w * .17f, h * .48f), Size(w * .66f, h * .22f))
                repeat(5) { i -> drawCircle(Color(0xFFA76D4F), w * .055f, Offset(w * (.28f + (i % 3) * .21f), h * (.54f + (i / 3) * .09f))) }
            }
            FoodId.FISH -> {
                drawOval(Color(0xFF8DCBD8), Offset(w * .18f, h * .34f), Size(w * .54f, h * .34f))
                val tail = Path().apply {
                    moveTo(w * .70f, h * .50f); lineTo(w * .90f, h * .30f); lineTo(w * .90f, h * .70f); close()
                }
                drawPath(tail, Color(0xFF75B5C4))
                drawCircle(Color(0xFF4A3543), w * .025f, Offset(w * .30f, h * .46f))
            }
            FoodId.CARROT -> {
                val body = Path().apply {
                    moveTo(w * .38f, h * .22f); lineTo(w * .70f, h * .26f); lineTo(w * .48f, h * .86f); close()
                }
                drawPath(body, Color(0xFFF2A15D))
                repeat(3) { i -> drawLine(Color(0xFF7FB879), Offset(w * (.44f + i * .06f), h * .24f), Offset(w * (.34f + i * .08f), h * .05f), w * .055f) }
            }
            FoodId.BERRY_BOWL -> {
                drawOval(Color(0xFFD09AB3), Offset(w * .12f, h * .55f), Size(w * .76f, h * .28f))
                repeat(6) { i -> drawCircle(if (i % 2 == 0) Color(0xFF8A6BB1) else Color(0xFFC46A8D), w * .075f, Offset(w * (.28f + (i % 3) * .22f), h * (.45f + (i / 3) * .11f))) }
            }
            FoodId.APPLE_SLICES -> {
                repeat(3) { i ->
                    drawArc(Color(0xFFF4B1A2), 195f, 150f, true, Offset(w * (.18f + i * .18f), h * (.25f + i * .05f)), Size(w * .32f, h * .48f))
                }
            }
            FoodId.MILK_BOWL -> {
                drawOval(Color(0xFFBFA5D8), Offset(w * .10f, h * .52f), Size(w * .80f, h * .30f))
                drawOval(Color(0xFFFFFCF6), Offset(w * .18f, h * .49f), Size(w * .64f, h * .18f))
            }
            FoodId.BISCUIT -> {
                drawRoundRect(Color(0xFFD6A06F), Offset(w * .22f, h * .28f), Size(w * .56f, h * .44f), androidx.compose.ui.geometry.CornerRadius(w * .12f))
                repeat(4) { i -> drawCircle(Color(0xFF9D724F), w * .035f, Offset(w * (.36f + (i % 2) * .28f), h * (.42f + (i / 2) * .18f))) }
            }
            FoodId.SALAD_BOWL -> {
                drawOval(Color(0xFFCE9FB4), Offset(w * .10f, h * .56f), Size(w * .80f, h * .28f))
                drawCircle(Color(0xFF8FC58A), w * .15f, Offset(w * .36f, h * .46f))
                drawCircle(Color(0xFFA7D597), w * .14f, Offset(w * .56f, h * .41f))
                drawCircle(Color(0xFFF0A98D), w * .07f, Offset(w * .65f, h * .52f))
            }
            FoodId.CUPCAKE_TREAT -> {
                drawRoundRect(Color(0xFFE0A97D), Offset(w * .29f, h * .48f), Size(w * .42f, h * .34f), androidx.compose.ui.geometry.CornerRadius(w * .05f))
                drawCircle(Color(0xFFF5B6D0), w * .22f, Offset(w * .50f, h * .39f))
                drawCircle(Color(0xFFF7DB79), w * .055f, Offset(w * .50f, h * .16f))
            }
            FoodId.PREMIUM_FEAST_TRAY -> {
                drawRoundRect(Color(0xFFC68B72), Offset(w * .10f, h * .48f), Size(w * .80f, h * .34f), androidx.compose.ui.geometry.CornerRadius(w * .08f))
                drawCircle(Color(0xFF8FC58A), w * .09f, Offset(w * .30f, h * .58f))
                drawCircle(Color(0xFFF0B071), w * .09f, Offset(w * .50f, h * .58f))
                drawCircle(Color(0xFFCE8CA6), w * .09f, Offset(w * .70f, h * .58f))
            }
        }
    }
}
