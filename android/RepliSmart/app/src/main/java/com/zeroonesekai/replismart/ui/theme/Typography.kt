package com.zeroonesekai.replismart.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.zeroonesekai.replismart.R


/**
 * Author: Ramachandrarao Chikkala
 * Created on: 12/04/25
 */
val winkyRoughRegular = FontFamily(
    Font(R.font.winkyrough_regular, FontWeight.Normal)
)

val winkyRoughBold = FontFamily(
    Font(R.font.winkyrough_bold, FontWeight.Bold)
)
val winkyRoughMedium = FontFamily(
    Font(R.font.winkyrough_medium, FontWeight.Light)
)

val winkyRoughSemibold = FontFamily(
    Font(R.font.winkyrough_semibold, FontWeight.SemiBold)
)


//val interFont = FontFamily(
//    Font(googleFont = GoogleFont("Inter"), fontProvider = provider)
//)

val AppTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = winkyRoughBold,
        fontWeight = FontWeight.Bold,
        fontSize = 34.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    displayMedium = TextStyle(
        fontFamily = winkyRoughBold,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 34.sp,
        letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
        fontFamily = winkyRoughMedium,
        fontWeight = FontWeight.Medium,
        fontSize = 20.sp,
        lineHeight = 28.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = winkyRoughRegular,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = winkyRoughRegular,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    labelLarge = TextStyle(
        fontFamily = winkyRoughMedium,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 18.sp
    )
)
