package com.jkuester.unlauncher

import android.content.res.Resources
import androidx.activity.ComponentActivity
import androidx.datastore.core.DataStore
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import com.jkuester.unlauncher.datastore.proto.CorePreferences
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@MockKExtension.CheckUnnecessaryStub
@MockKExtension.ConfirmVerification
@ExtendWith(MockKExtension::class)
class ActivityModuleTest {
    private val activityModule = ActivityModule()

    @MockK
    lateinit var activity: ComponentActivity

    @Test
    fun provideComponentActivity() {
        val componentActivity = activityModule.provideComponentActivity(activity)
        componentActivity shouldBe activity
    }

    @Test
    fun provideResources() {
        val mResources = mockk<Resources>()
        every { activity.resources } returns mResources

        val resources = activityModule.provideResources(activity)

        resources shouldBe mResources
        verify(exactly = 1) { activity.resources }
    }

    @Test
    fun provideCorePreferencesRepo() {
        mockkStatic(LifecycleOwner::lifecycleScope)
        every { any<LifecycleOwner>().lifecycleScope } returns mockk()
        val prefsStore = mockk<DataStore<CorePreferences>>()
        every { prefsStore.data } returns emptyFlow()
        mockkStatic("androidx.lifecycle.FlowLiveDataConversions")
        val liveData = mockk<LiveData<CorePreferences>>()
        every { any<Flow<CorePreferences>>().asLiveData() } returns liveData
        justRun { liveData.observe(any(), any()) }
        val observer = mockk<Observer<CorePreferences>>()

        val prefsRepo = activityModule.provideCorePreferencesRepo(activity, prefsStore)
        prefsRepo.observe(observer)

        verify(exactly = 1) { liveData.observe(activity, observer) }
        verify(exactly = 1) { activity.lifecycleScope }
        verify(exactly = 1) { prefsStore.data }
    }
}
