package com.jkuester.unlauncher.dialog

import android.app.AlertDialog
import android.content.DialogInterface
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import androidx.viewbinding.ViewBindings
import com.jkuester.unlauncher.datastore.proto.UnlauncherApp
import com.jkuester.unlauncher.datastore.proto.UnlauncherApps
import com.jkuester.unlauncher.fragment.Supplier
import com.jkuester.unlauncher.util.TestDataRepository
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
import kotlin.reflect.KFunction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

private val homeApp = UnlauncherApp
    .newBuilder()
    .setDisplayName("App A")
    .setPackageName("a")
    .setHomeAppIndex(0)
    .build()

@MockKExtension.CheckUnnecessaryStub
// @MockKExtension.ConfirmVerification Weird bug between mockk/kotlin/Java is causing this to fail
@ExtendWith(MockKExtension::class)
class RenameAppDisplayNameDialogTest {
    @MockK
    lateinit var rootView: LinearLayout
    @MockK
    lateinit var renameEditText: EditText
    @MockK
    lateinit var alertDialog: AlertDialog
    @MockK
    lateinit var layoutInflater: LayoutInflater

    private val inflaterSupplier = object : Supplier<LayoutInflater> {
        override fun get() = layoutInflater
    }

    private val appsRepo = TestDataRepository(UnlauncherApps.getDefaultInstance())

    @BeforeEach
    fun beforeEach() {
        val function: KFunction<View?> = ViewBindings::findChildViewById
        mockkStatic(function)
        every { ViewBindings.findChildViewById<View>(any(), R.id.rename_editText) } returns renameEditText
    }

    @AfterEach
    fun afterEach() {
        verify(exactly = 1) { ViewBindings.findChildViewById<View>(rootView, R.id.rename_editText) }
    }

    @Test
    fun onCreateDialog() {
        every { layoutInflater.inflate(any<Int>(), any(), any()) } returns rootView
        justRun { renameEditText.setText(any<String>()) }
        mockkConstructor(AlertDialog.Builder::class)
        every { anyConstructed<AlertDialog.Builder>().setTitle(any(Int::class)) } answers
            { self as AlertDialog.Builder }
        every { anyConstructed<AlertDialog.Builder>().setView(any<View>()) } answers
            { self as AlertDialog.Builder }
        val setPositiveButtonSlot = slot<DialogInterface.OnClickListener>()
        every {
            anyConstructed<AlertDialog.Builder>().setPositiveButton(
                any<Int>(),
                capture(setPositiveButtonSlot)
            )
        } answers { self as AlertDialog.Builder }
        every { anyConstructed<AlertDialog.Builder>().create() } returns alertDialog
        justRun { alertDialog.dismiss() }
        appsRepo.updateAsync { it.toBuilder().addApps(homeApp).build() }

        val dialogFragment = RenameAppDisplayNameDialog(homeApp)
            .apply {
                unlauncherAppsRepo = appsRepo
                layoutInflaterSupplier = inflaterSupplier
            }
        val result = dialogFragment.onCreateDialog(null)

        result shouldBe alertDialog
        verify(exactly = 1) { layoutInflater.inflate(R.layout.rename_dialog_edit_text, null, false) }
        verify(exactly = 1) { renameEditText.setText(homeApp.displayName) }
        verify(exactly = 1) { anyConstructed<AlertDialog.Builder>().setTitle(R.string.rename_app) }
        verify(exactly = 1) { anyConstructed<AlertDialog.Builder>().setView(rootView) }
        verify(exactly = 1) {
            anyConstructed<AlertDialog.Builder>().setPositiveButton(
                R.string.menu_rename,
                setPositiveButtonSlot.captured
            )
        }
        verify(exactly = 1) { anyConstructed<AlertDialog.Builder>().create() }

        // Simulate entering new name
        val editable = mockk<Editable>()
        every { editable.toString() } returns "new name"
        every { renameEditText.text } returns editable
        // Trigger the listener
        setPositiveButtonSlot.captured.onClick(alertDialog, 1)

        verify(exactly = 1) { renameEditText.text }
        verify(exactly = 1) { editable.toString() }
        verify(exactly = 1) { alertDialog.dismiss() }
        appsRepo.get().appsList[0].displayName shouldBe "new name"
    }
}
