package com.jkuester.unlauncher

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@MockKExtension.CheckUnnecessaryStub
@MockKExtension.ConfirmVerification
@ExtendWith(MockKExtension::class)
class ExtensionsTest {
    @Test
    fun swap() {
        val originalList = listOf('a', 'b', 'c')
        val newList = originalList.swap(0, 2)
        newList shouldBe listOf('c', 'b', 'a')
    }

    @Test
    fun swap_withItself() {
        val originalList = listOf('a', 'b', 'c')
        val newList = originalList.swap(1, 1)
        newList shouldBe originalList
    }

    @Test
    fun swap_ToIndexOutOfBounds() {
        val originalList = listOf('a', 'b')
        val exception = shouldThrow<IndexOutOfBoundsException> { originalList.swap(0, 2) }
        exception.message shouldBe "Index out of bounds"
    }

    @Test
    fun swap_FromIndexOutOfBounds() {
        val originalList = listOf('a', 'b', 'c')
        val exception = shouldThrow<IndexOutOfBoundsException> { originalList.swap(-1, 2) }
        exception.message shouldBe "Index out of bounds"
    }
}
