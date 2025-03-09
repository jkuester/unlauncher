package com.jkuester.unlauncher

import io.kotest.matchers.shouldBe
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

@MockKExtension.CheckUnnecessaryStub
@MockKExtension.ConfirmVerification
@ExtendWith(MockKExtension::class)
class AndroidSdkVersionTest {
    // The Build.VERSION.SDK_INT == 0 when running these unit tests

    @ParameterizedTest
    @CsvSource(
        "-1, true",
        "0, true",
        "1, false",
    )
    fun androidSdkAtLeast(version: Int, expected: Boolean) {
        androidSdkAtLeast(version) shouldBe expected
    }
}
