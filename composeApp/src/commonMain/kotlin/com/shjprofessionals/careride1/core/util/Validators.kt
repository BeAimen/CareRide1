package com.shjprofessionals.careride1.core.util

/**
 * Input validation utilities
 */
object Validators {

    /**
     * Validate message content
     */
    fun validateMessage(content: String): ValidationResult {
        val trimmed = content.trim()

        return when {
            trimmed.isEmpty() -> ValidationResult.Invalid("Message cannot be empty")
            trimmed.length < 2 -> ValidationResult.Invalid("Message is too short")
            trimmed.length > 2000 -> ValidationResult.Invalid("Message is too long (max 2000 characters)")
            else -> ValidationResult.Valid(trimmed)
        }
    }

    /**
     * Validate search query
     */
    fun validateSearchQuery(query: String): ValidationResult {
        val trimmed = query.trim()

        return when {
            trimmed.length > 100 -> ValidationResult.Invalid("Search query is too long")
            else -> ValidationResult.Valid(trimmed)
        }
    }

    /**
     * Sanitize text input (remove dangerous characters)
     */
    fun sanitize(input: String): String {
        return input
            .trim()
            .replace(Regex("[<>\"']"), "") // Remove potential XSS characters
    }
}

sealed class ValidationResult {
    data class Valid(val sanitized: String) : ValidationResult()
    data class Invalid(val reason: String) : ValidationResult()

    fun isValid(): Boolean = this is Valid

    fun getOrNull(): String? = (this as? Valid)?.sanitized

    inline fun onValid(action: (String) -> Unit): ValidationResult {
        if (this is Valid) action(sanitized)
        return this
    }

    inline fun onInvalid(action: (String) -> Unit): ValidationResult {
        if (this is Invalid) action(reason)
        return this
    }
}