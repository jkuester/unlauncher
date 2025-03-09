package com.jkuester.unlauncher.datasource

import android.app.Application
import com.jkuester.unlauncher.datastore.proto.UnlauncherApp
import com.jkuester.unlauncher.datastore.proto.UnlauncherApps
import com.sduduzog.slimlauncher.data.BaseDao
import com.sduduzog.slimlauncher.data.BaseDatabase
import com.sduduzog.slimlauncher.di.AppModule
import com.sduduzog.slimlauncher.models.HomeApp
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.matchers.Matcher
import io.kotest.matchers.MatcherResult
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkStatic
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
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

    @Nested
    inner class HomeAppToIndexMigrationTest {
        private val homeApp0 = HomeApp("appName0", "packageName0", "activityName0", 0, null, 0)
        private val homeApp1 = HomeApp("appName1", "packageName1", "activityName1", 1, null, 1)
        private val homeApp2 = HomeApp("appName2", "packageName2", "activityName2", 2, null, 2)

        private fun match(expected: HomeApp) = Matcher<UnlauncherApp> { actual ->
            MatcherResult(
                actual.packageName == expected.packageName &&
                    actual.className == expected.activityName &&
                    actual.displayName == expected.appName &&
                    actual.homeAppIndex == expected.sortingIndex,
                { "App $actual should match $expected" },
                { "App $actual should not match $expected" },
            )
        }

        @MockK
        lateinit var context: Application
        @MockK
        lateinit var baseDatabase: BaseDatabase
        @MockK
        lateinit var homeAppsDao: BaseDao

        private lateinit var migration: HomeAppToIndexMigration

        @BeforeEach
        fun beforeEach() {
            mockkConstructor(AppModule::class)
            every { anyConstructed<AppModule>().provideBaseDatabase(any()) } returns baseDatabase
            every { baseDatabase.baseDao() } returns homeAppsDao
            migration = HomeAppToIndexMigration(context)
        }

        @AfterEach
        fun afterEach() {
            verify(exactly = 1) { anyConstructed<AppModule>().provideBaseDatabase(context) }
            verify(exactly = 1) { baseDatabase.baseDao() }
        }

        @Test
        fun shouldMigrate_emptyHomeApps() = runTest {
            coEvery { homeAppsDao.getAll() } returns emptyList()

            val result = migration.shouldMigrate(mockk())

            result shouldBe false
            coVerify(exactly = 1) { homeAppsDao.getAll() }
        }

        @Test
        fun shouldMigrate_hasHomeApps() = runTest {
            coEvery { homeAppsDao.getAll() } returns listOf(homeApp0)

            val result = migration.shouldMigrate(mockk())

            result shouldBe true
            coVerify(exactly = 1) { homeAppsDao.getAll() }
        }

        @Test
        fun migrate() = runTest {
            coEvery { homeAppsDao.getAll() } returns listOf(homeApp0, homeApp1, homeApp2)
            val currentApps = UnlauncherApps.newBuilder().addAllApps(
                listOf(homeApp0, homeApp1, homeApp2).map {
                    UnlauncherApp
                        .newBuilder()
                        .setPackageName(it.packageName)
                        .setClassName(it.activityName)
                        .setDisplayName(it.appName)
                        .build()
                }
            ).build()

            val result = migration.migrate(currentApps)

            coVerify(exactly = 1) { homeAppsDao.getAll() }
            result.appsCount shouldBe 3
            val (app0, app1, app2) = result.appsList
            app0 should match(homeApp0)
            app1 should match(homeApp1)
            app2 should match(homeApp2)
        }

        @Test
        fun testCleanUp() = runTest {
            justRun { homeAppsDao.clearTable() }

            migration.cleanUp()

            verify(exactly = 1) { homeAppsDao.clearTable() }
        }
    }
}
