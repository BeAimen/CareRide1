package com.shjprofessionals.careride1.core.util

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Centralized error handler for app-wide error events.
 * Singleton to emit errors that can be observed by UI.
 */
object ErrorHandler {

    private val _errors = MutableSharedFlow<ErrorEvent>(extraBufferCapacity = 1)
    val errors: SharedFlow<ErrorEvent> = _errors.asSharedFlow()

    fun emit(error: AppError, retryAction: (() -> Unit)? = null) {
        _errors.tryEmit(ErrorEvent(error, retryAction))
    }

    fun emit(throwable: Throwable, retryAction: (() -> Unit)? = null) {
        emit(throwable.toAppError(), retryAction)
    }
}

data class ErrorEvent(
    val error: AppError,
    val retryAction: (() -> Unit)? = null
) {
    val canRetry: Boolean get() = error.isRetryable && retryAction != null
}
