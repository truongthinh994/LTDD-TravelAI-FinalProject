package com.travelai.ui.theme

import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class TypographyScaleTest {
    @Test
    fun scaledBy_returnsSameInstanceForDefaultScale() {
        assertSame(TravelAITypography, TravelAITypography.scaledBy(1.0f))
    }

    @Test
    fun scaledBy_scalesFontSizeAndLineHeight() {
        val scaled = TravelAITypography.scaledBy(1.3f)

        assertEquals(
            TravelAITypography.bodyLarge.fontSize.value * 1.3f,
            scaled.bodyLarge.fontSize.value,
            0.01f
        )
        assertEquals(
            TravelAITypography.bodyLarge.lineHeight.value * 1.3f,
            scaled.bodyLarge.lineHeight.value,
            0.01f
        )
    }

    @Test
    fun scaledBy_clampsOutsideAllowedRange() {
        val tooSmall = TravelAITypography.scaledBy(0.2f)
        val tooLarge = TravelAITypography.scaledBy(2.0f)

        assertEquals(
            TravelAITypography.bodyMedium.fontSize.value * MIN_APP_FONT_SCALE,
            tooSmall.bodyMedium.fontSize.value,
            0.01f
        )
        assertEquals(
            TravelAITypography.bodyMedium.fontSize.value * MAX_APP_FONT_SCALE,
            tooLarge.bodyMedium.fontSize.value,
            0.01f
        )
    }
}
