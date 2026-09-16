package com.dk.together.ui

import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

fun Modifier.widthIn(min: Dp = Dp.Unspecified, max: Dp = Dp.Unspecified): Modifier =
    androidx.compose.foundation.layout.widthIn(this, min = min, max = max)
