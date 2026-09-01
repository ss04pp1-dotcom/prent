package com.parentalcare.core.common.result

/**
 * Unified Result wrapper used across all repositories and use cases.
 *
 * - Success: carries payload of type [T]
 * - Failure: carries a typed [AppError]
 *
 * Design notes:
 *   - Never carry raw exceptions across module boundaries (avoids leaking
 *     sensitive stack traces to logs/UI).
 *   - [AppError] is a sealed hierarchy so the UI can branch on type without
 *     string parsing.
 */
sealed interface Result<out T> {
    data class Success<T>(val data: T) : Result<T>
    data class Failure(val error: AppError) : Result<Nothing>

    /** Inline map over Success. Failures propagate untouched. */
    fun <R> map(transform: (T) -> R): Result<R> = when (this) {
        is Success -> Success(transform(data))
        is Failure -> this
    }

    /** Inline flatMap. */
    fun <R> flatMap(transform: (T) -> Result<R>): Result<R> = when (this) {
        is Success -> transform(data)
        is Failure -> this
    }

    /** Returns payload on Success, or null on Failure. */
    fun getOrNull(): T? = (this as? Success)?.data

    /** Returns error on Failure, or null on Success. */
    fun errorOrNull(): AppError? = (this as? Failure)?.error

    val isSuccess: Boolean get() = this is Success
    val isFailure: Boolean get() = this is Failure
}

inline fun <T> resultOf(block: () -> T): Result<T> = try {
    Result.Success(block())
} catch (e: Throwable) {
    Result.Failure(AppError.from(e))
}

fun <T> Result<T>.successOr(fallback: T): T = getOrNull() ?: fallback

fun <T> Result<T>.getOrThrow(): T = when (this) {
    is Result.Success -> data
    is Result.Failure -> throw RuntimeException(error.toString())
}
