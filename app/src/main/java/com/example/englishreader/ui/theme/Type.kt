package com.example.englishreader.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val AppTypography = Typography(
    headlineLarge = TextStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold),
    headlineMedium = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold),
    bodyLarge = TextStyle(fontSize = 20.sp, lineHeight = 32.sp),
    bodyMedium = TextStyle(fontSize = 18.sp, lineHeight = 28.sp),
    labelLarge = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium)
)
