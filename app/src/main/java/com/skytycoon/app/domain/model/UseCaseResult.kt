package com.skytycoon.app.domain.model

sealed class UseCaseResult<out T> {
    data class Success<T>(val data: T) : UseCaseResult<T>()
    data class Failure(val message: String) : UseCaseResult<Nothing>()
}
