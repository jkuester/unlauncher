package com.jkuester.unlauncher

import androidx.activity.ComponentActivity
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockkStatic
import io.mockk.verify
import kotlin.test.assertSame
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@MockKExtension.CheckUnnecessaryStub
@MockKExtension.ConfirmVerification
@ExtendWith(MockKExtension::class)
class ActivityModuleTest {
    private val activityModule = ActivityModule()

    @MockK
    lateinit var activity: ComponentActivity

    @MockK
    lateinit var lifecycleScope: LifecycleCoroutineScope

    @Test
    fun provideLifecycleCoroutineScope() {
        mockkStatic(LifecycleOwner::lifecycleScope)
        every { any<LifecycleOwner>().lifecycleScope } returns lifecycleScope

        val actualLifecycleScope = activityModule.provideLifecycleCoroutineScope(activity)

        assertSame(lifecycleScope, actualLifecycleScope)
        verify(exactly = 1) { activity.lifecycleScope }
    }
}
