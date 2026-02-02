package com.shjprofessionals.careride1.core.util

/**
 * Standard UI state wrapper for screens.
 */
data class UiState<T>(
    val data: T? = null,
    val isLoading: Boolean = false,
    val error: AppError? = null
) {
    val isSuccess: Boolean get() = data != null && !isLoading && error == null
    val isError: Boolean get() = error != null && !isLoading
    val isEmpty: Boolean get() = data == null && !isLoading && error == null

    companion object {
        fun <T> loading(): UiState<T> = UiState(isLoading = true)
        fun <T> success(data: T): UiState<T> = UiState(data = data)
        fun <T> error(error: AppError): UiState<T> = UiState(error = error)
        fun <T> empty(): UiState<T> = UiState()
    }
}

/**
 * Extension to update UiState from AppResult
 */
fun <T> UiState<T>.fromResult(result: AppResult<T>): UiState<T> = when (result) {
    is AppResult.Loading -> copy(isLoading = true, error = null)
    is AppResult.Success -> UiState(data = result.data, isLoading = false, error = null)
    is AppResult.Error -> copy(isLoading = false, error = result.error)
}
