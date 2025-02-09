package com.jkuester.unlauncher.datasource

import com.jkuester.unlauncher.datastore.proto.ClockType
import com.jkuester.unlauncher.datastore.proto.CorePreferences
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.matchers.shouldBe
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.test.runTest
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
}
