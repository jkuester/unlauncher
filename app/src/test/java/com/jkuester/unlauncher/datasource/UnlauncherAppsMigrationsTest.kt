package com.jkuester.unlauncher.datasource

import com.jkuester.unlauncher.datastore.proto.UnlauncherApps
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

@MockKExtension.CheckUnnecessaryStub
@MockKExtension.ConfirmVerification
@ExtendWith(MockKExtension::class)
class UnlauncherAppsMigrationsTest {
    @ParameterizedTest
    @CsvSource(
        "-1, true",
        "0, true",
        "1, false",
        "2, false"
    )
    fun sortAppsMigration_shouldMigrate(version: String, expected: String) = runTest {
        val apps = UnlauncherApps
            .newBuilder()
            .build()
            .let(setVersion(version.toInt()))

        val shouldMigrate = SortAppsMigration.shouldMigrate(apps)

        shouldMigrate shouldBe expected.toBoolean()
    }

    @Test
    fun sortAppsMigration_migrate() = runTest {
        val originalApps = mockk<UnlauncherApps>()
        val sortedApps = mockk<UnlauncherApps>()
        val migratedApps = mockk<UnlauncherApps>()
        val mockSetVersion = mockk<(t: UnlauncherApps) -> UnlauncherApps>()
        every { mockSetVersion(any()) } returns migratedApps
        mockkStatic(::setVersion, ::sortApps)
        every { setVersion(any()) } returns mockSetVersion
        every { sortApps(any()) } returns sortedApps

        val result = SortAppsMigration.migrate(originalApps)

        result shouldBe migratedApps
        verify(exactly = 1) { sortApps(originalApps) }
        verify(exactly = 1) { mockSetVersion(sortedApps) }
        verify(exactly = 1) { setVersion(1) }
    }

    @Test
    fun sortAppsMigration_cleanUp() = runTest {
        shouldNotThrowAny { SortAppsMigration.cleanUp() }
    }
}
