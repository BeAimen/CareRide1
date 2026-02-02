package com.shjprofessionals.careride1.core.util

/**
 * Typed application errors for consistent handling.
 */
sealed class AppError(
    open val message: String,
    open val cause: Throwable? = null
) {
    // Network errors
    data class Network(
        override val message: String = "Network error. Please check your connection.",
        override val cause: Throwable? = null
    ) : AppError(message, cause)

    // Not found errors
    data class NotFound(
        val resource: String,
        override val message: String = "$resource not found",
        override val cause: Throwable? = null
    ) : AppError(message, cause)

    // Validation errors
    data class Validation(
        override val message: String,
        val field: String? = null
    ) : AppError(message)

    // Auth/subscription errors
    data class Unauthorized(
        override val message: String = "Please subscribe to access this feature"
    ) : AppError(message)

    // Payment errors
    data class Payment(
        override val message: String = "Payment failed. Please try again.",
        override val cause: Throwable? = null
    ) : AppError(message, cause)

    // Generic/unknown errors
    data class Unknown(
        override val message: String = "Something went wrong. Please try again.",
        override val cause: Throwable? = null
    ) : AppError(message, cause)

    val userMessage: String get() = message

    val isRetryable: Boolean get() = when (this) {
        is Network, is Payment, is Unknown -> true
        is NotFound, is Validation, is Unauthorized -> false
    }
}

/**
 * Convert Throwable to AppError
 */
fun Throwable.toAppError(): AppError = when (this) {
    is IllegalArgumentException -> AppError.Validation(message ?: "Invalid input")
    is IllegalStateException -> AppError.Unknown(message ?: "Invalid state")
    is NoSuchElementException -> AppError.NotFound("Resource", message ?: "Not found")
    else -> AppError.Unknown(message ?: "Something went wrong", this)
}
