package com.jkuester.unlauncher.dialog

import android.app.AlertDialog
import android.content.DialogInterface
import com.jkuester.unlauncher.datasource.QuickButtonIcon
import com.jkuester.unlauncher.datasource.setCenterIconId
import com.jkuester.unlauncher.datasource.setLeftIconId
import com.jkuester.unlauncher.datasource.setRightIconId
import com.jkuester.unlauncher.datastore.proto.QuickButtonPreferences
import com.jkuester.unlauncher.util.TestDataRepository
import com.sduduzog.slimlauncher.R
import io.kotest.matchers.shouldBe
import io.mockk.CapturingSlot
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.justRun
import io.mockk.mockkConstructor
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

@MockKExtension.CheckUnnecessaryStub
// @MockKExtension.ConfirmVerification Weird bug between mockk/kotlin/Java is causing this to fail
@ExtendWith(MockKExtension::class)
class QuickButtonIconDialogTest {
    @MockK
    lateinit var alertDialog: AlertDialog

    lateinit var onSelectionSlot: CapturingSlot<DialogInterface.OnClickListener>

    private val quickButtonPrefsRepo = TestDataRepository(QuickButtonPreferences.getDefaultInstance())

    @BeforeEach
    fun beforeEach() {
        mockkConstructor(AlertDialog.Builder::class)
        onSelectionSlot = slot<DialogInterface.OnClickListener>()
        every { anyConstructed<AlertDialog.Builder>().setTitle(any(Int::class)) } answers
            { self as AlertDialog.Builder }
        every {
            anyConstructed<AlertDialog.Builder>().setSingleChoiceItems(
                any(Int::class),
                any(Int::class),
                capture(onSelectionSlot)
            )
        } answers { self as AlertDialog.Builder }
        every { anyConstructed<AlertDialog.Builder>().create() } returns alertDialog
        justRun { alertDialog.dismiss() }
    }

    @AfterEach
    fun afterEach() {
        verify(exactly = 1) {
            anyConstructed<AlertDialog.Builder>().setTitle(R.string.quick_buttons)
        }
        verify(exactly = 1) { anyConstructed<AlertDialog.Builder>().create() }
        verify(exactly = 1) { alertDialog.dismiss() }
    }

    @ParameterizedTest
    @CsvSource(
        "IC_CALL, IC_EMPTY, 0, 1",
        "IC_EMPTY, IC_CALL, 1, 0"
    )
    fun onCreateDialog_IC_CALL(
        originalIcon: QuickButtonIcon,
        newIcon: QuickButtonIcon,
        originalIndex: Int,
        newIndex: Int
    ) {
        quickButtonPrefsRepo.updateAsync(setLeftIconId(originalIcon.prefId))

        val dialogFragment = QuickButtonIconDialog(QuickButtonIcon.IC_CALL.prefId)
            .apply { repo = quickButtonPrefsRepo }
        val result = dialogFragment.onCreateDialog(null)

        result shouldBe alertDialog
        verify(exactly = 1) {
            anyConstructed<AlertDialog.Builder>().setSingleChoiceItems(
                R.array.quick_button_array,
                originalIndex,
                onSelectionSlot.captured
            )
        }

        // Trigger the listener
        onSelectionSlot.captured.onClick(alertDialog, newIndex)

        quickButtonPrefsRepo.get().leftButton.iconId shouldBe newIcon.prefId
    }

    @ParameterizedTest
    @CsvSource(
        "IC_COG, IC_EMPTY, 0, 1",
        "IC_EMPTY, IC_COG, 1, 0"
    )
    fun onCreateDialog_IC_COG(
        originalIcon: QuickButtonIcon,
        newIcon: QuickButtonIcon,
        originalIndex: Int,
        newIndex: Int
    ) {
        quickButtonPrefsRepo.updateAsync(setCenterIconId(originalIcon.prefId))

        val dialogFragment = QuickButtonIconDialog(QuickButtonIcon.IC_COG.prefId)
            .apply { repo = quickButtonPrefsRepo }
        val result = dialogFragment.onCreateDialog(null)

        result shouldBe alertDialog
        verify(exactly = 1) {
            anyConstructed<AlertDialog.Builder>().setSingleChoiceItems(
                R.array.quick_button_array,
                originalIndex,
                onSelectionSlot.captured
            )
        }

        // Trigger the listener
        onSelectionSlot.captured.onClick(alertDialog, newIndex)

        quickButtonPrefsRepo.get().centerButton.iconId shouldBe newIcon.prefId
    }

    @ParameterizedTest
    @CsvSource(
        "IC_PHOTO_CAMERA, IC_EMPTY, 0, 1",
        "IC_EMPTY, IC_PHOTO_CAMERA, 1, 0"
    )
    fun onCreateDialog_IC_PHOTO_CAMERA(
        originalIcon: QuickButtonIcon,
        newIcon: QuickButtonIcon,
        originalIndex: Int,
        newIndex: Int
    ) {
        quickButtonPrefsRepo.updateAsync(setRightIconId(originalIcon.prefId))

        val dialogFragment = QuickButtonIconDialog(QuickButtonIcon.IC_PHOTO_CAMERA.prefId)
            .apply { repo = quickButtonPrefsRepo }
        val result = dialogFragment.onCreateDialog(null)

        result shouldBe alertDialog
        verify(exactly = 1) {
            anyConstructed<AlertDialog.Builder>().setSingleChoiceItems(
                R.array.quick_button_array,
                originalIndex,
                onSelectionSlot.captured
            )
        }

        // Trigger the listener
        onSelectionSlot.captured.onClick(alertDialog, newIndex)

        quickButtonPrefsRepo.get().rightButton.iconId shouldBe newIcon.prefId
    }
}
