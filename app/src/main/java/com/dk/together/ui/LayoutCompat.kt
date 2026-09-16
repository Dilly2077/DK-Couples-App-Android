package com.dk.together.ui

import androidx.compose.foundation.layout.widthIn as composeWidthIn
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

fun Modifier.widthIn(min: Dp = Dp.Unspecified, max: Dp = Dp.Unspecified): Modifier =
    this.composeWidthIn(min = min, max = max)
