package com.shjprofessionals.careride1.core.util

/**
 * Wrapper for operation results with typed errors.
 * Provides consistent error handling across the app.
 */
sealed class AppResult<out T> {
    data class Success<T>(val data: T) : AppResult<T>()
    data class Error(val error: AppError) : AppResult<Nothing>()
    data object Loading : AppResult<Nothing>()

    val isSuccess: Boolean get() = this is Success
    val isError: Boolean get() = this is Error
    val isLoading: Boolean get() = this is Loading

    fun getOrNull(): T? = (this as? Success)?.data
    fun errorOrNull(): AppError? = (this as? Error)?.error

    inline fun <R> map(transform: (T) -> R): AppResult<R> = when (this) {
        is Success -> Success(transform(data))
        is Error -> this
        is Loading -> Loading
    }

    inline fun onSuccess(action: (T) -> Unit): AppResult<T> {
        if (this is Success) action(data)
        return this
    }

    inline fun onError(action: (AppError) -> Unit): AppResult<T> {
        if (this is Error) action(error)
        return this
    }
}

/**
 * Convert Kotlin Result to AppResult
 */
fun <T> Result<T>.toAppResult(): AppResult<T> = fold(
    onSuccess = { AppResult.Success(it) },
    onFailure = { AppResult.Error(it.toAppError()) }
)

/**
 * Execute a suspend block and wrap in AppResult
 */
suspend fun <T> runCatching(block: suspend () -> T): AppResult<T> = try {
    AppResult.Success(block())
} catch (e: Exception) {
    AppResult.Error(e.toAppError())
}
