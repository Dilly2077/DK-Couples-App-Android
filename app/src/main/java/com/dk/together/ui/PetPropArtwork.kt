package com.dk.together.ui

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import com.dk.together.pets.feeding.FoodId
import com.dk.together.pets.play.PlayToy

@Composable
fun PetToyArtwork(toy: PlayToy, modifier: Modifier = Modifier) {
    val fileName = when (toy) {
        PlayToy.BALL -> "toy_ball.png"
        PlayToy.ROPE -> "toy_rope.png"
        PlayToy.PLUSH -> "toy_plush_bone.png"
        PlayToy.FRISBEE -> "toy_frisbee.png"
    }
    val loaded = PackedRuntimeArtwork(fileName, modifier, ContentScale.Fit)
    if (!loaded) {
        Canvas(modifier) {
            drawCircle(Color(0xFF9BD6C8), size.minDimension * .32f, Offset(size.width * .5f, size.height * .5f))
        }
    }
}

@Composable
fun PetFoodArtwork(food: FoodId, modifier: Modifier = Modifier) {
    val fileName = when (food) {
        FoodId.KIBBLE -> "food_kibble.png"
        FoodId.FISH -> "food_fish.png"
        FoodId.CARROT -> "food_carrot.png"
        FoodId.BERRY_BOWL -> "food_berry_bowl.png"
        FoodId.APPLE_SLICES -> "food_apple_slices.png"
        FoodId.MILK_BOWL -> "food_milk_bowl.png"
        FoodId.BISCUIT -> "food_biscuit.png"
        FoodId.SALAD_BOWL -> "food_salad_bowl.png"
        FoodId.CUPCAKE_TREAT -> "food_cupcake_treat.png"
        FoodId.PREMIUM_FEAST_TRAY -> "food_premium_feast_tray.png"
    }
    val loaded = PackedRuntimeArtwork(fileName, modifier, ContentScale.Fit)
    if (!loaded) {
        Canvas(modifier) {
            drawCircle(Color(0xFFC98B8E), size.minDimension * .30f, Offset(size.width * .5f, size.height * .5f))
        }
    }
}
