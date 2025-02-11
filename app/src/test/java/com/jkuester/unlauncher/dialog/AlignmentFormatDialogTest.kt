package com.jkuester.unlauncher.dialog

import android.app.AlertDialog
import android.content.DialogInterface
import com.jkuester.unlauncher.datasource.CorePreferencesRepository
import com.jkuester.unlauncher.datasource.setAlignmentFormat
import com.jkuester.unlauncher.datastore.proto.AlignmentFormat
import com.sduduzog.slimlauncher.R
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@MockKExtension.CheckUnnecessaryStub
// @MockKExtension.ConfirmVerification Some weird mockk/kotlin/java bug seems to be making this fail for this test.
@ExtendWith(MockKExtension::class)
class AlignmentFormatDialogTest {
    @MockK
    lateinit var corePrefsRepo: CorePreferencesRepository
    @MockK
    lateinit var alertDialog: AlertDialog

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
        every { corePrefsRepo.get().alignmentFormat.number } returns 0
        justRun { alertDialog.dismiss() }
        every { corePrefsRepo.updateAsync(any()) } returns mockk()
        mockkStatic(::setAlignmentFormat)
        every { setAlignmentFormat(any()) } returns mockk()

        val dialogFragment = AlignmentFormatDialog()
            .apply { corePreferencesRepo = corePrefsRepo }
        val result = dialogFragment.onCreateDialog(null)

        result shouldBe alertDialog
        verify(exactly = 1) { anyConstructed<AlertDialog.Builder>().setTitle(R.string.choose_alignment_dialog_title) }
        verify(exactly = 1) {
            anyConstructed<AlertDialog.Builder>().setSingleChoiceItems(
                R.array.alignment_format_array,
                0,
                selectAlignmentSlot.captured
            )
        }
        verify(exactly = 1) { anyConstructed<AlertDialog.Builder>().create() }
        verify(exactly = 1) { corePrefsRepo.get().alignmentFormat.number }

        // Trigger the listener
        selectAlignmentSlot.captured.onClick(alertDialog, 0)

        verify(exactly = 1) { alertDialog.dismiss() }
        verify(exactly = 1) { corePrefsRepo.updateAsync(any()) }
        verify(exactly = 1) { setAlignmentFormat(AlignmentFormat.forNumber(0)) }
    }
}
