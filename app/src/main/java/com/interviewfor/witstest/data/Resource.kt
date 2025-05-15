package com.interviewfor.witstest.data

sealed class Resource<out T> {
    data object Loading : Resource<Nothing>()
    data class Success<out T>(val data: T) : Resource<T>()
    data class Error(val message: String) : Resource<Nothing>()
}