package com.shjprofessionals.careride1.core.util

/**
 * Validation utilities for authentication fields
 */
object AuthValidators {

    private val EMAIL_REGEX = Regex(
        "[a-zA-Z0-9+._%\\-]{1,256}@[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}(\\.[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25})+"
    )

    fun validateEmail(email: String): ValidationResult {
        val trimmed = email.trim()
        return when {
            trimmed.isEmpty() -> ValidationResult.Invalid("Email is required")
            !EMAIL_REGEX.matches(trimmed) -> ValidationResult.Invalid("Please enter a valid email")
            else -> ValidationResult.Valid(trimmed)
        }
    }

    fun validatePassword(password: String): ValidationResult {
        return when {
            password.isEmpty() -> ValidationResult.Invalid("Password is required")
            password.length < 8 -> ValidationResult.Invalid("Password must be at least 8 characters")
            else -> ValidationResult.Valid(password)
        }
    }

    fun validatePasswordMatch(password: String, confirmPassword: String): ValidationResult {
        return when {
            confirmPassword.isEmpty() -> ValidationResult.Invalid("Please confirm your password")
            password != confirmPassword -> ValidationResult.Invalid("Passwords do not match")
            else -> ValidationResult.Valid(confirmPassword)
        }
    }

    fun validateName(name: String): ValidationResult {
        val trimmed = name.trim()
        return when {
            trimmed.isEmpty() -> ValidationResult.Invalid("Name is required")
            trimmed.length < 2 -> ValidationResult.Invalid("Name is too short")
            trimmed.length > 100 -> ValidationResult.Invalid("Name is too long")
            else -> ValidationResult.Valid(trimmed)
        }
    }
}