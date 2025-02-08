package com.jkuester.unlauncher.datasource

import android.content.Context
import android.content.SharedPreferences
import com.jkuester.unlauncher.datastore.proto.QuickButtonPreferences
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import java.util.stream.Stream
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@MockKExtension.CheckUnnecessaryStub
@MockKExtension.ConfirmVerification
@ExtendWith(MockKExtension::class)
class QuickButtonPreferencesMigrationsTest {
    @Nested
    inner class SharedPreferencesMigration {
        @MockK
        lateinit var sharedPrefs: SharedPreferences
        @MockK
        lateinit var context: Context

        @BeforeEach
        fun beforeEach() {
            every { context.getSharedPreferences(any(), any()) } returns sharedPrefs
        }
        @AfterEach
        fun afterEach() = verify(exactly = 1) {
            context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        }

        @Test
        fun sharedPrefsMigration_noData() = runTest {
            every { sharedPrefs.getInt("quick_button_left", any()) } returns
                QuickButtonIcon.IC_CALL.prefId
            every { sharedPrefs.getInt("quick_button_center", any()) } returns
                QuickButtonIcon.IC_COG.prefId
            every { sharedPrefs.getInt("quick_button_right", any()) } returns
                QuickButtonIcon.IC_PHOTO_CAMERA.prefId
            val initialPrefs = QuickButtonPreferences
                .newBuilder()
                .build()

            val migration = sharedPrefsMigration(context)
            val prefs = migration.migrate(initialPrefs)

            prefs.leftButton.iconId shouldBe QuickButtonIcon.IC_CALL.prefId
            prefs.centerButton.iconId shouldBe QuickButtonIcon.IC_COG.prefId
            prefs.rightButton.iconId shouldBe QuickButtonIcon.IC_PHOTO_CAMERA.prefId
            verify(exactly = 1) {
                sharedPrefs.getInt("quick_button_left", QuickButtonIcon.IC_CALL.prefId)
            }
            verify(exactly = 1) {
                sharedPrefs.getInt("quick_button_center", QuickButtonIcon.IC_COG.prefId)
            }
            verify(exactly = 1) {
                sharedPrefs.getInt("quick_button_right", QuickButtonIcon.IC_PHOTO_CAMERA.prefId)
            }
        }

        @Test
        fun sharedPrefsMigration_existingData() = runTest {
            val initialPrefs = QuickButtonPreferences
                .newBuilder()
                .build()
                .let(setLeftIconId(QuickButtonIcon.IC_EMPTY.prefId))
                .let(setCenterIconId(QuickButtonIcon.IC_EMPTY.prefId))
                .let(setRightIconId(QuickButtonIcon.IC_EMPTY.prefId))

            val migration = sharedPrefsMigration(context)
            val prefs = migration.migrate(initialPrefs)

            prefs.leftButton.iconId shouldBe QuickButtonIcon.IC_EMPTY.prefId
            prefs.centerButton.iconId shouldBe QuickButtonIcon.IC_EMPTY.prefId
            prefs.rightButton.iconId shouldBe QuickButtonIcon.IC_EMPTY.prefId
        }
    }

    @ParameterizedTest
    @MethodSource("getQuickButtonPrefsWithShouldMigrate")
    fun toThreeQuickButtonsMigration_shouldMigrate(
        initialPrefs: QuickButtonPreferences,
        expectedShouldMigrate: Boolean
    ) = runTest {
        val shouldMigrate = ToThreeQuickButtonsMigration.shouldMigrate(initialPrefs)
        assertEquals(expectedShouldMigrate, shouldMigrate)
    }

    @Test
    fun toThreeQuickButtonsMigration_migrate_noneExisting() = runTest {
        val initialPrefs = QuickButtonPreferences
            .newBuilder()
            .build()

        val migratedPrefs = ToThreeQuickButtonsMigration.migrate(initialPrefs)

        migratedPrefs.leftButton.iconId shouldBe QuickButtonIcon.IC_CALL.prefId
        migratedPrefs.centerButton.iconId shouldBe QuickButtonIcon.IC_COG.prefId
        migratedPrefs.rightButton.iconId shouldBe QuickButtonIcon.IC_PHOTO_CAMERA.prefId
    }

    @Test
    fun toThreeQuickButtonsMigration_migrate_allExisting() = runTest {
        val initialPrefs = QuickButtonPreferences
            .newBuilder()
            .build()
            .let(setLeftIconId(QuickButtonIcon.IC_EMPTY.prefId))
            .let(setCenterIconId(QuickButtonIcon.IC_EMPTY.prefId))
            .let(setRightIconId(QuickButtonIcon.IC_EMPTY.prefId))

        val migratedPrefs = ToThreeQuickButtonsMigration.migrate(initialPrefs)

        migratedPrefs.leftButton.iconId shouldBe QuickButtonIcon.IC_EMPTY.prefId
        migratedPrefs.centerButton.iconId shouldBe QuickButtonIcon.IC_EMPTY.prefId
        migratedPrefs.rightButton.iconId shouldBe QuickButtonIcon.IC_EMPTY.prefId
    }

    @Test
    fun toThreeQuickButtonsMigration_cleanUp() = runTest {
        shouldNotThrowAny { ToThreeQuickButtonsMigration.cleanUp() }
    }

    private fun getQuickButtonPrefsWithShouldMigrate(): Stream<Arguments> = Stream.of(
        Arguments.of(
            QuickButtonPreferences
                .newBuilder()
                .build()
                .let(setLeftIconId(QuickButtonIcon.IC_CALL.prefId))
                .let(setCenterIconId(QuickButtonIcon.IC_COG.prefId))
                .let(setRightIconId(QuickButtonIcon.IC_PHOTO_CAMERA.prefId)),
            false
        ),
        Arguments.of(QuickButtonPreferences.newBuilder().build(), true),
        Arguments.of(
            QuickButtonPreferences
                .newBuilder()
                .build()
                .let(setLeftIconId(QuickButtonIcon.IC_CALL.prefId)),
            true
        ),
    )
}
