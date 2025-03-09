package com.jkuester.unlauncher

import android.os.Build
import android.widget.PopupMenu
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkStatic
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@MockKExtension.CheckUnnecessaryStub
// @MockKExtension.ConfirmVerification
@ExtendWith(MockKExtension::class)
class WidgetsTest {
    @Test
    fun createPopupMenuWithIcons_androidAtLeastQ() {
        mockkConstructor(PopupMenu::class)
        justRun { anyConstructed<PopupMenu>().setForceShowIcon(any()) }
        mockkStatic(::androidSdkAtLeast)
        every { androidSdkAtLeast(any()) } returns true

        createPopupMenuWithIcons(mockk(), mockk())

        verify(exactly = 1) { androidSdkAtLeast(Build.VERSION_CODES.Q) }
        verify(exactly = 1) { anyConstructed<PopupMenu>().setForceShowIcon(true) }
    }

    @Test
    fun createPopupMenuWithIcons_androidLessThanQ() {
        mockkStatic(::androidSdkAtLeast)
        every { androidSdkAtLeast(any()) } returns false

        createPopupMenuWithIcons(mockk(), mockk())

        verify(exactly = 1) { androidSdkAtLeast(Build.VERSION_CODES.Q) }
    }
}
