package com.jkuester.unlauncher

import android.content.Context
import android.content.res.Resources
import android.graphics.Paint
import android.util.TypedValue
import com.jkuester.unlauncher.android.createPaint
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

private const val TEST_COLOR = 0xFF00FF00.toInt()
private const val TEST_ATTRIBUTE = 123

@MockKExtension.CheckUnnecessaryStub
@MockKExtension.ConfirmVerification
@ExtendWith(MockKExtension::class)
class GraphicsTest {
    @MockK
    lateinit var context: Context

    @Nested
    inner class GetThemeAttributeTest {
        @MockK
        lateinit var theme: Resources.Theme

        @BeforeEach
        fun beforeEach() {
            every { context.theme } returns theme
        }

        @Test
        fun resolvesAttributeAndReturnsData() {
            every { theme.resolveAttribute(TEST_ATTRIBUTE, any(), true) } answers {
                secondArg<TypedValue>().data = TEST_COLOR
                true
            }

            val result = getThemeAttribute(context, TEST_ATTRIBUTE)

            result shouldBe TEST_COLOR
            verify(exactly = 1) { context.theme }
            verify(exactly = 1) { theme.resolveAttribute(TEST_ATTRIBUTE, any(), true) }
        }

        @Test
        fun whenAttributeNotResolved_returnsDefaultData() {
            every { theme.resolveAttribute(TEST_ATTRIBUTE, any(), true) } returns false

            val result = getThemeAttribute(context, TEST_ATTRIBUTE)

            // When attribute is not resolved, data will be the default value (0)
            result shouldBe 0
            verify(exactly = 1) { context.theme }
            verify(exactly = 1) { theme.resolveAttribute(TEST_ATTRIBUTE, any(), true) }
        }
    }

    @Test
    fun getColorPaint() {
        val paint = mockk<Paint>()
        every { paint.isAntiAlias = true } returns Unit
        every { paint.style = Paint.Style.FILL } returns Unit
        every { paint.color = TEST_COLOR } returns Unit
        mockkStatic(::createPaint)
        every { createPaint() } returns paint
        mockkStatic(::getThemeAttribute)
        every { getThemeAttribute(context, TEST_ATTRIBUTE) } returns TEST_COLOR

        val result = getColorPaint(context, TEST_ATTRIBUTE)

        result shouldBe paint
        verify(exactly = 1) { paint.isAntiAlias = true }
        verify(exactly = 1) { paint.style = Paint.Style.FILL }
        verify(exactly = 1) { paint.color = TEST_COLOR }
        verify(exactly = 1) { getThemeAttribute(context, TEST_ATTRIBUTE) }
    }
}
