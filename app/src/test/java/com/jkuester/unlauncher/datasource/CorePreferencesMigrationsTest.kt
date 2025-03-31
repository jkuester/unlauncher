package com.jkuester.unlauncher.datasource

import android.content.Context
import android.content.SharedPreferences
import com.jkuester.unlauncher.datastore.proto.ClockType
import com.jkuester.unlauncher.datastore.proto.CorePreferences
import com.jkuester.unlauncher.datastore.proto.Theme
import com.jkuester.unlauncher.datastore.proto.TimeFormat
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.EnumSource

private val EMPTY_PREFS = CorePreferences.newBuilder().build()

@MockKExtension.CheckUnnecessaryStub
@MockKExtension.ConfirmVerification
@ExtendWith(MockKExtension::class)
class CorePreferencesMigrationsTest {
    @ParameterizedTest
    @EnumSource(
        value = ClockType::class,
        names = ["UNRECOGNIZED"],
        mode = EnumSource.Mode.EXCLUDE
    )
    fun addClockTypeMigration_shouldMigrate_withClockType(clockType: ClockType) = runTest {
        val initialPrefs = CorePreferences
            .newBuilder()
            .build()
            .let(setClockType(clockType))

        val shouldMigrate = AddClockTypeMigration.shouldMigrate(initialPrefs)

        shouldMigrate shouldBe false
    }

    @Test
    fun addClockTypeMigration_shouldMigrate_noClockType() = runTest {
        val shouldMigrate = AddClockTypeMigration.shouldMigrate(EMPTY_PREFS)
        shouldMigrate shouldBe true
    }

    @Test
    fun addClockTypeMigration_migrate_noClockType() = runTest {
        val updatedPrefs = AddClockTypeMigration.migrate(EMPTY_PREFS)
        updatedPrefs.clockType shouldBe ClockType.digital
    }

    @Test
    fun addClockTypeMigration_cleanUp() = runTest {
        shouldNotThrowAny { AddClockTypeMigration.cleanUp() }
    }

    @ParameterizedTest
    @CsvSource("true", "false")
    fun addShowSearchBarMigration_shouldMigrate_withShowSearchBar(showSearchBar: String) = runTest {
        val initialPrefs = CorePreferences
            .newBuilder()
            .build()
            .let(setShowSearchBar(showSearchBar.toBoolean()))

        val shouldMigrate = AddShowSearchBarMigration.shouldMigrate(initialPrefs)

        shouldMigrate shouldBe false
    }

    @Test
    fun addShowSearchBarMigration_shouldMigrate_noShowSearchBar() = runTest {
        val shouldMigrate = AddShowSearchBarMigration.shouldMigrate(EMPTY_PREFS)
        shouldMigrate shouldBe true
    }

    @Test
    fun addShowSearchBarMigration_migrate() = runTest {
        val updatedPrefs = AddShowSearchBarMigration.migrate(EMPTY_PREFS)
        updatedPrefs.showSearchBar shouldBe true
    }

    @Test
    fun addShowSearchBarMigration_cleanUp() = runTest {
        shouldNotThrowAny { AddShowSearchBarMigration.cleanUp() }
    }

    @Nested
    inner class SlimLauncherSharedPrefsMigrationTest {
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
            every { sharedPrefs.contains(any()) } returns true
            every { sharedPrefs.getInt("time_format", any()) } returns TimeFormat.twelve_hour.number
            every { sharedPrefs.getInt("key_theme", any()) } returns Theme.jupiter.number
            val initialPrefs = CorePreferences
                .newBuilder()
                .build()

            val migration = slimLauncherSharedPrefsMigration(context)
            migration.shouldMigrate(initialPrefs) shouldBe true
            val prefs = migration.migrate(initialPrefs)

            prefs.timeFormat shouldBe TimeFormat.twelve_hour
            prefs.theme shouldBe Theme.jupiter
            verify(exactly = 1) { sharedPrefs.contains("time_format") }
            verify(exactly = 1) { sharedPrefs.getInt("time_format", 0) }
            verify(exactly = 1) { sharedPrefs.getInt("key_theme", 0) }
        }
    }
}
