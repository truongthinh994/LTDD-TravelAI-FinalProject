package com.travelai.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// System-level Material 3 shapes
val TravelAIShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

// Bento / layout-specific shapes
data class BentoShapeSet(
    val hero: RoundedCornerShape = RoundedCornerShape(28.dp),      // bumped 24→28 for wave 2
    val feature: RoundedCornerShape = RoundedCornerShape(22.dp),    // NEW — asymmetric large tile
    val compact: RoundedCornerShape = RoundedCornerShape(18.dp),
    val action: RoundedCornerShape = RoundedCornerShape(20.dp),
    val card: RoundedCornerShape = RoundedCornerShape(20.dp),
    val fullWidth: RoundedCornerShape = RoundedCornerShape(16.dp),
    val formField: RoundedCornerShape = RoundedCornerShape(14.dp)
)

val BentoShapes = BentoShapeSet()

// Utility shapes
object TravelAIShapeTokens {
    val pill = RoundedCornerShape(50)
    val chip = RoundedCornerShape(100)
    val bubble = RoundedCornerShape(16.dp)
    val bubbleUser = RoundedCornerShape(16.dp)
    val modal = RoundedCornerShape(28.dp)
    val input = RoundedCornerShape(14.dp)
    val button = RoundedCornerShape(50)
}
