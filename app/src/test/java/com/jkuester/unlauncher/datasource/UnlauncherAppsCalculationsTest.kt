package com.jkuester.unlauncher.datasource

import com.jkuester.unlauncher.datastore.proto.UnlauncherApp
import com.jkuester.unlauncher.datastore.proto.UnlauncherApps
import com.sduduzog.slimlauncher.data.model.App
import com.sduduzog.slimlauncher.models.HomeApp
import io.kotest.matchers.Matcher
import io.kotest.matchers.MatcherResult
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

private fun match(expected: App) = Matcher<UnlauncherApp> { actual ->
    MatcherResult(
        actual.packageName == expected.packageName &&
            actual.className == expected.activityName &&
            actual.displayName == expected.appName,
        { "App $actual should match $expected" },
        { "App $actual should not match $expected" },
    )
}

private val app0 = App("appName0", "packageName0", "activityName0", 0)
private val app1 = App("appName1", "packageName1", "activityName1", 0)
private val app2 = App("appName2", "packageName2", "activityName2", 0)
private val unlauncherApp0 = UnlauncherApp
    .newBuilder()
    .setPackageName(app0.packageName)
    .setClassName(app0.activityName)
    .setDisplayName(app0.appName)
    .build()
private val unlauncherApp1 = UnlauncherApp
    .newBuilder()
    .setPackageName(app1.packageName)
    .setClassName(app1.activityName)
    .setDisplayName(app1.appName)
    .build()
private val unlauncherApp2 = UnlauncherApp
    .newBuilder()
    .setPackageName(app2.packageName)
    .setClassName(app2.activityName)
    .setDisplayName(app2.appName)
    .build()

@MockKExtension.CheckUnnecessaryStub
@MockKExtension.ConfirmVerification
@ExtendWith(MockKExtension::class)
class UnlauncherAppsCalculationsTest {
    @Test
    fun setApps_currentAppsEmpty() {
        val originalApps = UnlauncherApps.newBuilder().build()
        val newApps = listOf(app2, app0, app1)

        val updatedApps = setApps(newApps)(originalApps)

        updatedApps.appsList shouldHaveSize 3
        updatedApps.appsList[0] should match(app0)
        updatedApps.appsList[1] should match(app1)
        updatedApps.appsList[2] should match(app2)
    }

    @Test
    fun setApps_currentAppsMatch() {
        val originalApps = UnlauncherApps
            .newBuilder()
            .addAllApps(listOf(unlauncherApp0, unlauncherApp1, unlauncherApp2))
            .build()
        val newApps = listOf(app0, app1, app2)

        val updatedApps = setApps(newApps)(originalApps)

        updatedApps shouldBe originalApps
    }

    @Test
    fun setApps_someCurrentAppsDoNotMatch() {
        val originalApps = UnlauncherApps
            .newBuilder()
            .addAllApps(listOf(unlauncherApp0, unlauncherApp1))
            .build()
        val newApps = listOf(app1, app2)

        val updatedApps = setApps(newApps)(originalApps)

        updatedApps.appsList shouldHaveSize 2
        updatedApps.appsList[0] should match(app1)
        updatedApps.appsList[1] should match(app2)
    }

    @Test
    fun setApps_somePartialMatches() {
        val originalApps = UnlauncherApps
            .newBuilder()
            .addAllApps(listOf(unlauncherApp0, unlauncherApp1))
            .build()
        val newApps = listOf(
            app0.copy(packageName = "different"),
            app1.copy(activityName = "different")
        )

        val updatedApps = setApps(newApps)(originalApps)

        updatedApps.appsList shouldHaveSize 2
        updatedApps.appsList[0] should match(newApps[0])
        updatedApps.appsList[1] should match(newApps[1])
    }

    @Test
    fun setApps_newAppsEmpty() {
        val originalApps = UnlauncherApps
            .newBuilder()
            .addAllApps(listOf(unlauncherApp0, unlauncherApp1))
            .build()
        val newApps = emptyList<App>()

        val updatedApps = setApps(newApps)(originalApps)

        updatedApps.appsList.shouldBeEmpty()
    }

    @Test
    fun setHomeApps_currentAppsEmpty() {
        val originalApps = UnlauncherApps.newBuilder().build()
        val homeApps = listOf(app2, app0, app1)
            .mapIndexed { index, app -> HomeApp.from(app, index) }

        val updatedApps = setHomeApps(homeApps)(originalApps)

        updatedApps.appsList.shouldBeEmpty()
    }

    @Test
    fun setHomeApps_allAlreadyHome() {
        val homeApps = listOf(app2, app0, app1)
            .mapIndexed { index, app -> HomeApp.from(app, index) }
        val originalApps = UnlauncherApps
            .newBuilder()
            .addAllApps(
                listOf(unlauncherApp0, unlauncherApp1, unlauncherApp2)
                    .map { it.toBuilder().setHomeApp(true).build() }
            )
            .build()

        val updatedApps = setHomeApps(homeApps)(originalApps)

        updatedApps shouldBe originalApps
    }

    @Test
    fun setHomeApps_someAlreadyHome() {
        val homeApps = listOf(app0, app1)
            .mapIndexed { index, app -> HomeApp.from(app, index) }
        val originalApps = UnlauncherApps
            .newBuilder()
            .addAllApps(
                listOf(unlauncherApp1, unlauncherApp2)
                    .map { it.toBuilder().setHomeApp(true).build() }
                    .plus(unlauncherApp0)
            )
            .build()

        val updatedApps = setHomeApps(homeApps)(originalApps)

        updatedApps.appsList shouldHaveSize 3
        updatedApps.appsList[0].homeApp shouldBe true
        updatedApps.appsList[1].homeApp shouldBe true
        updatedApps.appsList[2].homeApp shouldBe false
    }

    @Test
    fun sortApps() {
        val originalApps = UnlauncherApps
            .newBuilder()
            .addAllApps(listOf(unlauncherApp2, unlauncherApp0, unlauncherApp1))
            .build()

        val updatedApps = sortApps(originalApps)

        updatedApps.appsList shouldContainExactly listOf(unlauncherApp0, unlauncherApp1, unlauncherApp2)
    }

    @Test
    fun setDisplayName() {
        val originalApps = UnlauncherApps
            .newBuilder()
            .addAllApps(listOf(unlauncherApp0, unlauncherApp1, unlauncherApp2))
            .build()
        val newName = "Zello"

        val updatedApps = setDisplayName(unlauncherApp1, newName)(originalApps)

        updatedApps.appsList shouldHaveSize 3
        updatedApps.appsList[0] should match(app0)
        updatedApps.appsList[1] should match(app2)
        updatedApps.appsList[2].displayName shouldBe newName
        updatedApps.appsList[2].packageName shouldBe unlauncherApp1.packageName
        updatedApps.appsList[2].className shouldBe unlauncherApp1.className
    }

    @Test
    fun setDisplayName_appNotFound() {
        val originalApps = UnlauncherApps
            .newBuilder()
            .addAllApps(listOf(unlauncherApp0, unlauncherApp2))
            .build()
        val newName = "Zello"

        val updatedApps = setDisplayName(unlauncherApp1, newName)(originalApps)

        updatedApps shouldBe originalApps
    }

    @Test
    fun setDisplayInDrawer() {
        val originalApps = UnlauncherApps
            .newBuilder()
            .addAllApps(listOf(unlauncherApp0, unlauncherApp1, unlauncherApp2))
            .build()

        val updatedApps = setDisplayInDrawer(unlauncherApp1, true)(originalApps)

        updatedApps.appsList shouldHaveSize 3
        updatedApps.appsList[0] should match(app0)
        updatedApps.appsList[1].displayInDrawer shouldBe true
        updatedApps.appsList[1].packageName shouldBe unlauncherApp1.packageName
        updatedApps.appsList[1].className shouldBe unlauncherApp1.className
        updatedApps.appsList[2] should match(app2)
    }

    @Test
    fun setVersion() {
        val originalApps = UnlauncherApps
            .newBuilder()
            .setVersion(1)
            .build()
        val version = 2

        val updatedApps = setVersion(version)(originalApps)

        updatedApps.version shouldBe version
    }
}
