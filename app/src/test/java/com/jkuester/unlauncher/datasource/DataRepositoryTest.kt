package com.jkuester.unlauncher.datasource

import android.util.Log
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.asLiveData
import com.google.protobuf.GeneratedMessageLite
import com.google.protobuf.InvalidProtocolBufferException
import com.jkuester.unlauncher.fragment.Supplier
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import java.io.InputStream
import java.io.OutputStream
import kotlin.test.assertSame
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

class TestData : GeneratedMessageLite<TestData, TestData.Builder>() {
    class Builder(defaultInstance: TestData?) : GeneratedMessageLite.Builder<TestData, Builder>(defaultInstance)

    override fun dynamicMethod(method: MethodToInvoke?, arg0: Any?, arg1: Any?) {
    }
}

@MockKExtension.CheckUnnecessaryStub
@MockKExtension.ConfirmVerification
@ExtendWith(MockKExtension::class)
class DataRepositoryTest {
    @Nested
    inner class RepositoryTest {
        @MockK
        lateinit var dataStore: DataStore<String>
        @MockK
        lateinit var getDefaultInstance: () -> String
        @MockK
        lateinit var lifecycleOwner: LifecycleOwner

        private val lifecycleOwnerSupplier = object : Supplier<LifecycleOwner> {
            override fun get() = lifecycleOwner
        }

        @AfterEach
        fun afterEach() = verify(exactly = 1) { dataStore.data }

        @Test
        fun observe() = runTest {
            every { dataStore.data } returns emptyFlow()
            mockkStatic("androidx.lifecycle.FlowLiveDataConversions")
            val liveData = mockk<LiveData<String>>()
            every { any<Flow<String>>().asLiveData() } returns liveData
            justRun { liveData.observe(any(), any()) }
            val observer = mockk<Observer<String>>()

            val dataRepo = DataRepositoryImpl(dataStore, backgroundScope, lifecycleOwnerSupplier, getDefaultInstance)
            dataRepo.observe(observer)

            verify(exactly = 1) { liveData.observe(lifecycleOwner, observer) }
        }

        @Test
        fun get_firstFromDataFlow() = runTest {
            val expectedData = "first"
            every { dataStore.data } returns flowOf(expectedData, "second")

            val dataRepo = DataRepositoryImpl(dataStore, backgroundScope, lifecycleOwnerSupplier, getDefaultInstance)
            val data = dataRepo.get()

            data shouldBe expectedData
        }

        @Test
        fun get_Exception() = runTest {
            val expectedException = Exception("Problem getting test data")
            every { dataStore.data } returns flow { throw expectedException }
            val dataRepo = DataRepositoryImpl(dataStore, backgroundScope, lifecycleOwnerSupplier, getDefaultInstance)

            val actualException = shouldThrow<Exception> { dataRepo.get() }

            actualException shouldBe expectedException
        }

        @Test
        fun get_IOException() = runTest {
            mockkStatic(Log::class)
            every { Log.e(any(), any(), any()) } returns 0
            val defaultData = "hello world"
            every { getDefaultInstance.invoke() } returns defaultData
            val expectedException = IOException("Problem getting test data")
            every { dataStore.data } returns flow { throw expectedException }

            val dataRepo = DataRepositoryImpl(dataStore, backgroundScope, lifecycleOwnerSupplier, getDefaultInstance)
            val data = dataRepo.get()

            data shouldBe defaultData
            verify(exactly = 1) {
                Log.e("AbstractDataRepository", "Error reading data store.", expectedException)
            }
            verify(exactly = 1) { getDefaultInstance.invoke() }
        }

        @Test
        fun updateAsync() = runTest {
            every { dataStore.data } returns emptyFlow()
            coJustRun { dataStore.updateData(any()) }

            val dataRepo = DataRepositoryImpl(dataStore, backgroundScope, lifecycleOwnerSupplier, getDefaultInstance)
            dataRepo.updateAsync(mockk()).join()

            coVerify(exactly = 1) { dataStore.updateData(any<suspend (t: String) -> String>()) }
        }
    }

    @Nested
    inner class AbstractDataSerializerTest {
        @MockK
        lateinit var testData: TestData

        @MockK
        lateinit var getDefaultInstance: () -> TestData

        @MockK
        lateinit var parseFrom: (InputStream) -> TestData

        private lateinit var serializer: DataSerializer<TestData>

        @BeforeEach
        fun beforeEach() {
            every { getDefaultInstance.invoke() } returns testData
            serializer = DataSerializer(getDefaultInstance, parseFrom)
        }

        @AfterEach
        fun afterEach() = verify(exactly = 1) { getDefaultInstance.invoke() }

        @Test
        fun defaultValue() = assertSame(testData, serializer.defaultValue)

        @Test
        fun readFrom() = runTest {
            val inputStream = mockk<InputStream>()
            every { parseFrom.invoke(any()) } returns testData

            val result = serializer.readFrom(inputStream)

            result shouldBe testData
            verify(exactly = 1) { parseFrom.invoke(inputStream) }
        }

        @Test
        fun readFrom_InvalidProtocolBufferException() = runTest {
            val inputStream = mockk<InputStream>()
            val expectedException = InvalidProtocolBufferException("Problem parsing test data")
            every { parseFrom.invoke(any()) } throws expectedException

            val actualException =
                shouldThrow<CorruptionException> {
                    serializer.readFrom(inputStream)
                }

            actualException.message shouldBe "Cannot read proto."
            actualException.cause shouldBe expectedException
            verify(exactly = 1) { parseFrom.invoke(inputStream) }
        }

        @Test
        fun writeTo() = runTest {
            val outputStream = mockk<OutputStream>()
            justRun { testData.writeTo(outputStream) }

            serializer.writeTo(testData, outputStream)

            verify(exactly = 1) { testData.writeTo(outputStream) }
        }
    }
}
