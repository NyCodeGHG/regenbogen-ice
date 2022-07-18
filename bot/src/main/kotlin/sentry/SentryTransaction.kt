package dev.nycode.regenbogenice.sentry

import io.sentry.ISpan
import io.sentry.Sentry
import io.sentry.SpanStatus

inline fun <T> sentryTransaction(
    name: String,
    operation: String,
    block: TransactionContext.() -> T
): T = transaction({ Sentry.startTransaction(name, operation) }, block)

@PublishedApi
internal inline fun <T> transaction(
    createTransaction: () -> ISpan,
    block: TransactionContext.() -> T
): T {
    val transaction = createTransaction()
    try {
        return TransactionContext(transaction).block()
    } catch (e: Exception) {
        transaction.throwable = e
        transaction.status = SpanStatus.INTERNAL_ERROR
        throw e
    } finally {
        transaction.finish()
    }
}

@JvmInline
value class TransactionContext(val transaction: ISpan) {
    inline fun <T> childTransaction(
        operation: String,
        description: String? = null,
        block: TransactionContext.() -> T
    ): T =
        transaction({ transaction.startChild(operation, description) }, block)
}
