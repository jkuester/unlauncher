package com.jkuester.unlauncher.dialog

import android.app.AlertDialog
import android.content.DialogInterface
import com.jkuester.unlauncher.datastore.proto.CorePreferences
import com.jkuester.unlauncher.util.TestDataRepository
import com.sduduzog.slimlauncher.R
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.justRun
import io.mockk.mockkConstructor
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@MockKExtension.CheckUnnecessaryStub
// @MockKExtension.ConfirmVerification Weird bug between mockk/kotlin/Java is causing this to fail
@ExtendWith(MockKExtension::class)
class TimeFormatDialogTest {
    @MockK
    lateinit var alertDialog: AlertDialog

    private val corePrefsRepo = TestDataRepository(CorePreferences.getDefaultInstance())

    @Test
    fun onCreateDialog() {
        mockkConstructor(AlertDialog.Builder::class)
        val selectAlignmentSlot = slot<DialogInterface.OnClickListener>()
        every { anyConstructed<AlertDialog.Builder>().setTitle(any(Int::class)) } answers
            { self as AlertDialog.Builder }
        every {
            anyConstructed<AlertDialog.Builder>().setSingleChoiceItems(
                any(Int::class),
                any(Int::class),
                capture(selectAlignmentSlot)
            )
        } answers { self as AlertDialog.Builder }
        every { anyConstructed<AlertDialog.Builder>().create() } returns alertDialog
        justRun { alertDialog.dismiss() }

        val dialogFragment = TimeFormatDialog()
            .apply { corePreferencesRepo = corePrefsRepo }
        val result = dialogFragment.onCreateDialog(null)

        result shouldBe alertDialog
        verify(exactly = 1) { anyConstructed<AlertDialog.Builder>().setTitle(R.string.choose_time_format_dialog_title) }
        verify(exactly = 1) {
            anyConstructed<AlertDialog.Builder>().setSingleChoiceItems(
                R.array.time_format_array,
                0,
                selectAlignmentSlot.captured
            )
        }
        verify(exactly = 1) { anyConstructed<AlertDialog.Builder>().create() }

        // Trigger the listener
        selectAlignmentSlot.captured.onClick(alertDialog, 1)

        verify(exactly = 1) { alertDialog.dismiss() }
        corePrefsRepo.get().timeFormat.number shouldBe 1
    }
}
