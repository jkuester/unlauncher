package com.sduduzog.slimlauncher.ui.main

import android.app.Application
import android.os.Build
import android.os.Bundle
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.room.Room
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sduduzog.slimlauncher.R
import com.sduduzog.slimlauncher.data.BaseDao
import com.sduduzog.slimlauncher.data.BaseDatabase
import com.sduduzog.slimlauncher.di.AppModule
import com.sduduzog.slimlauncher.di.MainFragmentFactory
import com.sduduzog.slimlauncher.launchFragmentInHiltContainer
import com.sduduzog.slimlauncher.models.HomeApp
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import dagger.hilt.android.testing.UninstallModules
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.robolectric.annotation.Config
import javax.inject.Inject
import javax.inject.Singleton

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.P], application = HiltTestApplication::class)
@UninstallModules(AppModule::class)
class HomeFragmentTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Module
    @InstallIn(ApplicationComponent::class)
    inner class TestModule {

        @Provides
        @Singleton
        internal fun provideBaseDatabase(application: Application): BaseDatabase {
            return Room.inMemoryDatabaseBuilder(application,
                    BaseDatabase::class.java )
                    .allowMainThreadQueries()
                    .build()
        }
        @Provides
        @Singleton
        internal fun provideBaseDao(database: BaseDatabase): BaseDao {
            return database.baseDao()
        }
    }

    @Inject
    lateinit var fragmentFactory: MainFragmentFactory

    @Inject
    lateinit var baseDao: BaseDao

    @Before
    fun init() {
        hiltRule.inject()
        val navController = mock(NavController::class.java)
        launchFragmentInHiltContainer<HomeFragment>(Bundle(), R.style.AppTheme, fragmentFactory) {
            Navigation.setViewNavController(this.view!!, navController)
        }
        baseDao.add(HomeApp("appName", "packageName", "activityName", 0, null, 1))
    }

    @Test
    fun `should display time and date`() {
        onView(withId(R.id.home_fragment_time)).check(matches(isDisplayed()))
        onView(withId(R.id.home_fragment_date)).check(matches(isDisplayed()))
    }

    @Test
    fun `should display added apps to home screen`() {

        onView(withText("appName")).check(matches(isDisplayed()))
    }

    @Test
    fun `when nickname changed should display added apps to home screen via nickname`() {
        baseDao.update(HomeApp("appName", "packageName", "activityName", 0, "appNickname", 1))

        onView(withText("appNickname")).check(matches(isDisplayed()))
    }
}