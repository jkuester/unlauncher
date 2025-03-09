package com.jkuester.unlauncher.fragment

import android.view.LayoutInflater
import androidx.datastore.core.DataStore
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.jkuester.unlauncher.datasource.DataRepository
import com.jkuester.unlauncher.datastore.proto.CorePreferences
import com.jkuester.unlauncher.datastore.proto.QuickButtonPreferences
import com.jkuester.unlauncher.datastore.proto.UnlauncherApps
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import kotlinx.coroutines.flow.emptyFlow
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@MockKExtension.CheckUnnecessaryStub
@MockKExtension.ConfirmVerification
@ExtendWith(MockKExtension::class)
class FragmentModuleTest {
    private val fragmentModule = FragmentModule()

    @Test
    fun provideFragmentManager() {
        val mFragment = mockk<Fragment>()
        val mFragmentManager = mockk<FragmentManager>()
        every { mFragment.childFragmentManager } returns mFragmentManager

        val fragmentManager = fragmentModule.provideFragmentManager(mFragment)

        fragmentManager shouldBe mFragmentManager
        verify(exactly = 1) { mFragment.childFragmentManager }
    }

    @Test
    fun provideLayoutInflaterSupplier() {
        val mFragment = mockk<Fragment>()
        val mLayoutInflater = mockk<LayoutInflater>()
        every { mFragment.layoutInflater } returns mLayoutInflater

        val lifecycleOwnerSupplier = fragmentModule.provideLayoutInflaterSupplier(mFragment)

        lifecycleOwnerSupplier.get() shouldBe mLayoutInflater
        verify(exactly = 1) { mFragment.layoutInflater }
    }

    @Test
    fun provideLifecycleScope() {
        val mFragment = mockk<Fragment>()
        val mLifecycleScope = mockk<LifecycleCoroutineScope>()
        mockkStatic(LifecycleOwner::lifecycleScope)
        every { any<LifecycleOwner>().lifecycleScope } returns mLifecycleScope

        val lifecycleScope = fragmentModule.provideLifecycleScope(mFragment)

        lifecycleScope shouldBe mLifecycleScope
        verify(exactly = 1) { mFragment.lifecycleScope }
    }

    @Test
    fun provideLifecycleOwnerSupplier() {
        val mFragment = mockk<Fragment>()
        val mLifecycleOwner = mockk<LifecycleOwner>()
        every { mFragment.viewLifecycleOwner } returns mLifecycleOwner

        val lifecycleOwnerSupplier = fragmentModule.provideLifecycleOwnerSupplier(mFragment)

        lifecycleOwnerSupplier.get() shouldBe mLifecycleOwner
        verify(exactly = 1) { mFragment.viewLifecycleOwner }
    }

    @Test
    fun provideCorePreferencesRepo() {
        val prefsStore = mockk<DataStore<CorePreferences>>()
        every { prefsStore.data } returns emptyFlow()

        val prefsRepo = fragmentModule.provideCorePreferencesRepo(prefsStore, mockk(), mockk())

        prefsRepo.shouldBeInstanceOf<DataRepository<CorePreferences>>()
        verify(exactly = 1) { prefsStore.data }
    }

    @Test
    fun provideQuickButtonPreferencesRepo() {
        val prefsStore = mockk<DataStore<QuickButtonPreferences>>()
        every { prefsStore.data } returns emptyFlow()

        val prefsRepo = fragmentModule.provideQuickButtonPreferencesRepo(prefsStore, mockk(), mockk())

        prefsRepo.shouldBeInstanceOf<DataRepository<CorePreferences>>()
        verify(exactly = 1) { prefsStore.data }
    }

    @Test
    fun provideUnlauncherAppsRepo() {
        val prefsStore = mockk<DataStore<UnlauncherApps>>()
        every { prefsStore.data } returns emptyFlow()

        val prefsRepo = fragmentModule.provideUnlauncherAppsRepo(prefsStore, mockk(), mockk())

        prefsRepo.shouldBeInstanceOf<DataRepository<CorePreferences>>()
        verify(exactly = 1) { prefsStore.data }
    }
}
