package com.example.editecho.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * EditEcho shapes that provide consistent component shapes throughout the app.
 */
val Shapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

/**
 * Extension function to get a specific shape from the Shapes object.
 */
fun Shapes.getShape(size: ShapeSize): CornerBasedShape {
    return when (size) {
        ShapeSize.ExtraSmall -> extraSmall
        ShapeSize.Small -> small
        ShapeSize.Medium -> medium
        ShapeSize.Large -> large
        ShapeSize.ExtraLarge -> extraLarge
        ShapeSize.Circular -> CircleShape
    }
}

/**
 * Enum class representing the different shape sizes available in the app.
 */
enum class ShapeSize {
    ExtraSmall,
    Small,
    Medium,
    Large,
    ExtraLarge,
    Circular
} 