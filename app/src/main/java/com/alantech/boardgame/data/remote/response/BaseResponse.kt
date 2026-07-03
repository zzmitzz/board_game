package com.alantech.boardgame.data.remote.response

sealed class BaseResponse<T> {
    data class Success<T>(val data: T) : BaseResponse<T>()
    data class Error(val message: String) : BaseResponse<Nothing>()
}
