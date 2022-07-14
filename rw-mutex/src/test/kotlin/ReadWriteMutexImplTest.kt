import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isTrue
import dev.nycode.regenbogenice.sync.ReadWriteMutex
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class ReadWriteMutexImplTest {
    @Test
    fun `Locking the mutex should increase reader count`() = runTest {
        val mutex = ReadWriteMutex()
        mutex.lockRead()
        assertThat(mutex.readerCount).isEqualTo(1)
        mutex.unlockRead()
        assertThat(mutex.readerCount).isEqualTo(0)
        mutex.lockRead()
        mutex.lockRead()
        assertThat(mutex.readerCount).isEqualTo(2)
        mutex.unlockRead()
        mutex.unlockRead()
        assertThat(mutex.readerCount).isEqualTo(0)
    }

    @Test
    fun `Locking the mutex should make it locked`() = runTest {
        val mutex = ReadWriteMutex()
        mutex.lockRead()
        assertThat(mutex.isReadLocked).isTrue()
        val job = launch {
            mutex.lockWrite()
            assertThat(mutex.isWriteLocked).isTrue()
        }
        mutex.unlockRead()
        job.join()
    }
}
