package dev.nycode.regenbogenice.sync

import kotlinx.atomicfu.atomic
import kotlinx.coroutines.sync.Mutex

/**
 * A read write mutex, which allows multiple readers OR one writer to lock the mutex.
 * Although the interface provides [lockRead], [lockWrite], [unlockRead] and [unlockWrite],
 * it's recommended to use the [withReadLock] and [withWriteLock] functions.
 */
interface ReadWriteMutex {
    /**
     * Returns true if the mutex is read-locked.
     */
    val isReadLocked: Boolean

    /**
     * Returns true if the mutex is write-locked.
     */
    val isWriteLocked: Boolean

    /**
     * The number of readers
     */
    val readerCount: Int

    /**
     * Locks the mutex as a reader.
     * Suspends until the mutex is available.
     */
    suspend fun lockRead()

    /**
     * Unlocks the mutex as a reader.
     */
    fun unlockRead()

    /**
     * Locks the mutex as a writer.
     * Suspends until the mutex is available.
     */
    suspend fun lockWrite()

    /**
     * Unlocks the mutex as a writer.
     */
    fun unlockWrite()
}

/**
 * Locks the mutex as a reader, executes the given [block] and unlocks the mutex again.
 * Could suspend when the mutex is not available yet.
 * This function is recommended over regular [ReadWriteMutex.lockRead] calls
 * because you can't forget to unlock the mutex again.
 */
suspend inline fun <T> ReadWriteMutex.withReadLock(block: () -> T): T {
    lockRead()
    return try {
        block()
    } finally {
        unlockRead()
    }
}

/**
 * Locks the mutex as a writer, executes the given [block] and unlocks the mutex again.
 * Could suspend when the mutex not available yet.
 * This function is recommended over regular [ReadWriteMutex.lockWrite] calls
 * because you can't forget to unlock the mutex again.
 */
suspend inline fun <T> ReadWriteMutex.withWriteLock(block: () -> T): T {
    lockWrite()
    return try {
        block()
    } finally {
        unlockWrite()
    }
}

/**
 * Creates a new [ReadWriteMutex].
 */
fun ReadWriteMutex(): ReadWriteMutex {
    return ReadWriteMutexImpl()
}

internal class ReadWriteMutexImpl : ReadWriteMutex {

    private val read = Mutex()
    private val write = Mutex()
    private val readers = atomic(0)

    override val readerCount: Int by readers

    override val isReadLocked: Boolean
        get() = read.isLocked
    override val isWriteLocked: Boolean
        get() = read.isLocked && write.isLocked

    override suspend fun lockRead() {
        if (readers.getAndIncrement() == 0) {
            read.lock()
        }
    }

    override fun unlockRead() {
        if (readers.decrementAndGet() == 0) {
            read.unlock()
        }
    }

    override suspend fun lockWrite() {
        read.lock()
        write.lock()
    }

    override fun unlockWrite() {
        write.unlock()
        read.unlock()
    }
}
