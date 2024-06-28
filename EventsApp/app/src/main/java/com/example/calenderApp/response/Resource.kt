package com.example.calenderApp.response

/**
 * @author Abhishek Mishra
 * Created 19/06/24
 */
class Resource<out T> private constructor(
    val status: Status,
    val data: T?
) {

    enum class Status {
        SUCCESS, ERROR, LOADING
    }

    companion object {
        fun <T> success(data: T): Resource<T> {
            return Resource(Status.SUCCESS, data)
        }

        fun <T> error(data: T? = null): Resource<T> {
            return Resource(Status.ERROR, data)
        }

        fun <T> loading(data: T? = null): Resource<T> {
            return Resource(Status.LOADING, data)
        }
    }
}