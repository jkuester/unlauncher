package com.jkuester.unlauncher

import android.app.WallpaperManager
import android.content.res.Configuration
import android.content.res.Resources
import android.content.res.TypedArray
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.os.Build
import android.util.DisplayMetrics
import android.view.Display
import android.view.WindowManager
import android.view.WindowMetrics
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.core.DataStore
import com.jkuester.unlauncher.datasource.getThemeStyleResource
import com.jkuester.unlauncher.datasource.setTheme
import com.jkuester.unlauncher.datastore.proto.CorePreferences
import com.jkuester.unlauncher.datastore.proto.Theme
import com.jkuester.unlauncher.util.TestDataRepository
import com.sduduzog.slimlauncher.utils.isDefaultLauncher
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkStatic
import io.mockk.verify
import kotlin.reflect.KFunction
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

private const val RES_ID = 1234
private const val BACKGROUND_COLOR = 0xf06292
typealias CreateBitmapFunction = (Int, Int, Bitmap.Config) -> Bitmap
private val CREATE_BITMAP_FN: CreateBitmapFunction = Bitmap::createBitmap

@MockKExtension.CheckUnnecessaryStub
@MockKExtension.ConfirmVerification
@ExtendWith(MockKExtension::class)
class ThemeManagerTest {
    @MockK
    lateinit var activity: AppCompatActivity

    lateinit var themeManager: ThemeManager

    @BeforeEach
    fun beforeEach() {
        themeManager = ThemeManager(activity)
    }

    @Test
    fun listenForThemeChanges() {
        justRun { activity.setTheme(any<Int>()) }
        justRun { activity.recreate() }
        val corePrefsRepo = TestDataRepository(CorePreferences.newBuilder().setTheme(Theme.midnight).build())

        themeManager.listenForThemeChanges(corePrefsRepo, Theme.midnight)

        // Nothing happens on first run
        verify(exactly = 0) { activity.setTheme(any<Int>()) }
        verify(exactly = 0) { activity.recreate() }

        corePrefsRepo.updateAsync(setTheme(Theme.noon))

        verify(exactly = 1) { activity.setTheme(getThemeStyleResource(Theme.noon)) }
        verify(exactly = 1) { activity.recreate() }
    }

    @Nested
    inner class SetDeviceWallpaper {
        @MockK
        lateinit var corePrefsStore: DataStore<CorePreferences>
        @MockK
        lateinit var resources: Resources
        @MockK
        lateinit var windowManager: WindowManager
        @MockK
        lateinit var windowMetrics: WindowMetrics
        @MockK
        lateinit var theme: Resources.Theme
        @MockK
        lateinit var themeAttributes: TypedArray
        @MockK
        lateinit var wallpaperManager: WallpaperManager
        @MockK
        lateinit var bounds: Rect
        @MockK
        lateinit var bitmap: Bitmap

        private lateinit var configuration: Configuration

        private val prefsDoNotKeepWallpaper = CorePreferences.newBuilder().setKeepDeviceWallpaper(false).build()

        @BeforeEach
        fun beforeEach() {
            configuration = Configuration()
            every { activity.resources } returns resources
            every { resources.configuration } returns configuration
            every { activity.windowManager } returns windowManager
            every { windowManager.currentWindowMetrics } returns windowMetrics
            every { windowMetrics.bounds } returns bounds
            every { theme.obtainStyledAttributes(any(), any()) } returns themeAttributes
            justRun { themeAttributes.recycle() }
            mockkStatic(::isDefaultLauncher)
            mockkStatic(WallpaperManager::getInstance)
            every { WallpaperManager.getInstance(any()) } returns wallpaperManager
            justRun { wallpaperManager.setBitmap(any()) }
            mockkStatic(::androidSdkAtLeast)
            mockkStatic(CREATE_BITMAP_FN as KFunction<*>)
            mockkConstructor(Canvas::class)
        }

        @Test
        fun nullTheme() = runTest {
            themeManager.setDeviceWallpaper(corePrefsStore, null, RES_ID, true)

            verify(exactly = 0) { activity.resources }
            verify(exactly = 0) { resources.configuration }
            verify(exactly = 0) { corePrefsStore.data }
            verify(exactly = 0) { theme.obtainStyledAttributes(any(), any()) }
            verify(exactly = 0) { themeAttributes.recycle() }
            verify(exactly = 0) { WallpaperManager.getInstance(any()) }
            verify(exactly = 0) { windowMetrics.bounds }
            verify(exactly = 0) { wallpaperManager.setBitmap(any()) }
        }

        @Test
        fun keepDeviceWallpaper() = runTest {
            val corePrefs = CorePreferences.newBuilder().setKeepDeviceWallpaper(true).build()
            every { corePrefsStore.data } returns flowOf(corePrefs)

            themeManager.setDeviceWallpaper(corePrefsStore, theme, RES_ID, false)

            verify(exactly = 0) { activity.resources }
            verify(exactly = 0) { resources.configuration }
            verify(exactly = 1) { corePrefsStore.data }
            verify(exactly = 0) { theme.obtainStyledAttributes(any(), any()) }
            verify(exactly = 0) { themeAttributes.recycle() }
            verify(exactly = 0) { WallpaperManager.getInstance(any()) }
            verify(exactly = 0) { windowMetrics.bounds }
            verify(exactly = 0) { wallpaperManager.setBitmap(any()) }
        }

        @Test
        fun notDefaultLauncher() = runTest {
            every { corePrefsStore.data } returns flowOf(prefsDoNotKeepWallpaper)
            every { isDefaultLauncher(any()) } returns false

            themeManager.setDeviceWallpaper(corePrefsStore, theme, RES_ID, false)

            verify(exactly = 0) { activity.resources }
            verify(exactly = 0) { resources.configuration }
            verify(exactly = 1) { corePrefsStore.data }
            verify(exactly = 1) { isDefaultLauncher(activity) }
            verify(exactly = 0) { theme.obtainStyledAttributes(any(), any()) }
            verify(exactly = 0) { themeAttributes.recycle() }
            verify(exactly = 0) { WallpaperManager.getInstance(any()) }
            verify(exactly = 0) { windowMetrics.bounds }
            verify(exactly = 0) { wallpaperManager.setBitmap(any()) }
        }

        @Test
        fun backgroundColorNotFound() = runTest {
            every { corePrefsStore.data } returns flowOf(prefsDoNotKeepWallpaper)
            every { isDefaultLauncher(any()) } returns true
            every { themeAttributes.getColor(any(), any()) } returns Int.MIN_VALUE

            themeManager.setDeviceWallpaper(corePrefsStore, theme, RES_ID, false)

            verify(exactly = 0) { activity.resources }
            verify(exactly = 0) { resources.configuration }
            verify(exactly = 1) { corePrefsStore.data }
            verify(exactly = 1) { isDefaultLauncher(activity) }
            verify(exactly = 1) { theme.obtainStyledAttributes(RES_ID, intArrayOf(android.R.attr.colorBackground)) }
            verify(exactly = 1) { themeAttributes.getColor(0, Int.MIN_VALUE) }
            verify(exactly = 1) { themeAttributes.recycle() }
            verify(exactly = 0) { WallpaperManager.getInstance(any()) }
            verify(exactly = 0) { windowMetrics.bounds }
            verify(exactly = 0) { wallpaperManager.setBitmap(any()) }
        }

        @Test
        fun setWallpaper_toScreenResolution_AndroidLowerThanR() = runTest {
            every { corePrefsStore.data } returns flowOf(prefsDoNotKeepWallpaper)
            every { isDefaultLauncher(any()) } returns true
            every { themeAttributes.getColor(any(), any()) } returns BACKGROUND_COLOR
            every { androidSdkAtLeast(any()) } returns false
            val display = mockk<Display>()
            every { windowManager.defaultDisplay } returns display
            every { display.getMetrics(any()) } answers {
                firstArg<DisplayMetrics>().widthPixels = 600
                firstArg<DisplayMetrics>().heightPixels = 400
            }
            every { wallpaperManager.desiredMinimumWidth } returns 0
            every { wallpaperManager.desiredMinimumHeight } returns 0
            every { CREATE_BITMAP_FN(any(), any(), any()) } returns bitmap
            justRun { anyConstructed<Canvas>().drawColor(any()) }

            themeManager.setDeviceWallpaper(corePrefsStore, theme, RES_ID, false)

            verify(exactly = 0) { activity.resources }
            verify(exactly = 0) { resources.configuration }
            verify(exactly = 1) { corePrefsStore.data }
            verify(exactly = 1) { isDefaultLauncher(activity) }
            verify(exactly = 1) { theme.obtainStyledAttributes(RES_ID, intArrayOf(android.R.attr.colorBackground)) }
            verify(exactly = 1) { themeAttributes.getColor(0, Int.MIN_VALUE) }
            verify(exactly = 1) { themeAttributes.recycle() }
            verify(exactly = 1) { WallpaperManager.getInstance(activity) }
            verify(exactly = 1) { androidSdkAtLeast(Build.VERSION_CODES.R) }
            verify(exactly = 1) { windowManager.defaultDisplay }
            verify(exactly = 1) { display.getMetrics(any()) }
            verify(exactly = 1) { wallpaperManager.desiredMinimumWidth }
            verify(exactly = 1) { wallpaperManager.desiredMinimumHeight }
            verify(exactly = 1) { wallpaperManager.setBitmap(bitmap) }
            verify(exactly = 1) { CREATE_BITMAP_FN(600, 400, Bitmap.Config.ARGB_8888) }
            verify(exactly = 1) { anyConstructed<Canvas>().drawColor(BACKGROUND_COLOR) }
        }

        @Test
        fun setWallpaper_toScreenResolution() = runTest {
            every { corePrefsStore.data } returns flowOf(prefsDoNotKeepWallpaper)
            every { isDefaultLauncher(any()) } returns true
            every { themeAttributes.getColor(any(), any()) } returns BACKGROUND_COLOR
            every { androidSdkAtLeast(any()) } returns true
            every { bounds.width() } returns 600
            every { bounds.height() } returns 400
            every { wallpaperManager.desiredMinimumWidth } returns 0
            every { wallpaperManager.desiredMinimumHeight } returns 0
            every { CREATE_BITMAP_FN(any(), any(), any()) } returns bitmap
            justRun { anyConstructed<Canvas>().drawColor(any()) }

            themeManager.setDeviceWallpaper(corePrefsStore, theme, RES_ID, false)

            verify(exactly = 0) { activity.resources }
            verify(exactly = 0) { resources.configuration }
            verify(exactly = 1) { corePrefsStore.data }
            verify(exactly = 1) { isDefaultLauncher(activity) }
            verify(exactly = 1) { theme.obtainStyledAttributes(RES_ID, intArrayOf(android.R.attr.colorBackground)) }
            verify(exactly = 1) { themeAttributes.getColor(0, Int.MIN_VALUE) }
            verify(exactly = 1) { themeAttributes.recycle() }
            verify(exactly = 1) { WallpaperManager.getInstance(activity) }
            verify(exactly = 1) { androidSdkAtLeast(Build.VERSION_CODES.R) }
            verify(exactly = 1) { activity.windowManager }
            verify(exactly = 1) { windowManager.currentWindowMetrics }
            verify(exactly = 1) { windowMetrics.bounds }
            verify(exactly = 1) { bounds.width() }
            verify(exactly = 1) { bounds.height() }
            verify(exactly = 1) { wallpaperManager.desiredMinimumWidth }
            verify(exactly = 1) { wallpaperManager.desiredMinimumHeight }
            verify(exactly = 1) { wallpaperManager.setBitmap(bitmap) }
            verify(exactly = 1) { CREATE_BITMAP_FN(600, 400, Bitmap.Config.ARGB_8888) }
            verify(exactly = 1) { anyConstructed<Canvas>().drawColor(BACKGROUND_COLOR) }
        }

        @Test
        fun setWallpaper_toDesiredMinWidthHeight() = runTest {
            every { corePrefsStore.data } returns flowOf(prefsDoNotKeepWallpaper)
            every { isDefaultLauncher(any()) } returns true
            every { themeAttributes.getColor(any(), any()) } returns BACKGROUND_COLOR
            every { androidSdkAtLeast(any()) } returns true
            every { bounds.width() } returns 600
            every { bounds.height() } returns 400
            every { wallpaperManager.desiredMinimumWidth } returns 1200
            every { wallpaperManager.desiredMinimumHeight } returns 800
            every { CREATE_BITMAP_FN(any(), any(), any()) } returns bitmap
            justRun { anyConstructed<Canvas>().drawColor(any()) }

            themeManager.setDeviceWallpaper(corePrefsStore, theme, RES_ID, false)

            verify(exactly = 0) { activity.resources }
            verify(exactly = 0) { resources.configuration }
            verify(exactly = 1) { corePrefsStore.data }
            verify(exactly = 1) { isDefaultLauncher(activity) }
            verify(exactly = 1) { theme.obtainStyledAttributes(RES_ID, intArrayOf(android.R.attr.colorBackground)) }
            verify(exactly = 1) { themeAttributes.getColor(0, Int.MIN_VALUE) }
            verify(exactly = 1) { themeAttributes.recycle() }
            verify(exactly = 1) { WallpaperManager.getInstance(activity) }
            verify(exactly = 1) { androidSdkAtLeast(Build.VERSION_CODES.R) }
            verify(exactly = 1) { activity.windowManager }
            verify(exactly = 1) { windowManager.currentWindowMetrics }
            verify(exactly = 1) { windowMetrics.bounds }
            verify(exactly = 1) { bounds.width() }
            verify(exactly = 1) { bounds.height() }
            verify(exactly = 1) { wallpaperManager.desiredMinimumWidth }
            verify(exactly = 1) { wallpaperManager.desiredMinimumHeight }
            verify(exactly = 1) { wallpaperManager.setBitmap(bitmap) }
            verify(exactly = 1) { CREATE_BITMAP_FN(1200, 800, Bitmap.Config.ARGB_8888) }
            verify(exactly = 1) { anyConstructed<Canvas>().drawColor(BACKGROUND_COLOR) }
        }

        @Test
        fun firstAndDarkModeChanged() = runTest {
            every { corePrefsStore.data } returns flowOf(prefsDoNotKeepWallpaper)
            every { isDefaultLauncher(any()) } returns true
            every { themeAttributes.getColor(any(), any()) } returns BACKGROUND_COLOR
            every { androidSdkAtLeast(any()) } returns true
            every { bounds.width() } returns 600
            every { bounds.height() } returns 400
            every { wallpaperManager.desiredMinimumWidth } returns 0
            every { wallpaperManager.desiredMinimumHeight } returns 0
            every { CREATE_BITMAP_FN(any(), any(), any()) } returns bitmap
            justRun { anyConstructed<Canvas>().drawColor(any()) }
            configuration.uiMode = Configuration.UI_MODE_NIGHT_NO

            themeManager.setDeviceWallpaper(corePrefsStore, theme, RES_ID, true)

            // Nothing happens the first time since the original status was null
            verify(exactly = 1) { activity.resources }
            verify(exactly = 1) { resources.configuration }
            verify(exactly = 0) { corePrefsStore.data }
            verify(exactly = 0) { theme.obtainStyledAttributes(any(), any()) }
            verify(exactly = 0) { themeAttributes.recycle() }
            verify(exactly = 0) { WallpaperManager.getInstance(any()) }
            verify(exactly = 0) { windowMetrics.bounds }
            verify(exactly = 0) { wallpaperManager.setBitmap(any()) }

            themeManager.setDeviceWallpaper(corePrefsStore, theme, RES_ID, true)

            // Nothing happens the second time since the original status has not changed
            verify(exactly = 2) { activity.resources }
            verify(exactly = 2) { resources.configuration }
            verify(exactly = 0) { corePrefsStore.data }
            verify(exactly = 0) { theme.obtainStyledAttributes(any(), any()) }
            verify(exactly = 0) { themeAttributes.recycle() }
            verify(exactly = 0) { WallpaperManager.getInstance(any()) }
            verify(exactly = 0) { windowMetrics.bounds }
            verify(exactly = 0) { wallpaperManager.setBitmap(any()) }

            configuration.uiMode = Configuration.UI_MODE_NIGHT_YES

            themeManager.setDeviceWallpaper(corePrefsStore, theme, RES_ID, true)

            // When the dark mode changes, the wallpaper actually gets set
            verify(exactly = 3) { activity.resources }
            verify(exactly = 3) { resources.configuration }
            verify(exactly = 1) { corePrefsStore.data }
            verify(exactly = 1) { isDefaultLauncher(activity) }
            verify(exactly = 1) { theme.obtainStyledAttributes(RES_ID, intArrayOf(android.R.attr.colorBackground)) }
            verify(exactly = 1) { themeAttributes.getColor(0, Int.MIN_VALUE) }
            verify(exactly = 1) { themeAttributes.recycle() }
            verify(exactly = 1) { WallpaperManager.getInstance(activity) }
            verify(exactly = 1) { androidSdkAtLeast(Build.VERSION_CODES.R) }
            verify(exactly = 1) { activity.windowManager }
            verify(exactly = 1) { windowManager.currentWindowMetrics }
            verify(exactly = 1) { windowMetrics.bounds }
            verify(exactly = 1) { bounds.width() }
            verify(exactly = 1) { bounds.height() }
            verify(exactly = 1) { wallpaperManager.desiredMinimumWidth }
            verify(exactly = 1) { wallpaperManager.desiredMinimumHeight }
            verify(exactly = 1) { wallpaperManager.setBitmap(bitmap) }
            verify(exactly = 1) { CREATE_BITMAP_FN(600, 400, Bitmap.Config.ARGB_8888) }
            verify(exactly = 1) { anyConstructed<Canvas>().drawColor(BACKGROUND_COLOR) }
        }
    }
}
