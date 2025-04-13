package com.zeroonesekai.replismart.viewmodel


class ApiResource<out T>(val status: Status, val data: T?, val message: String?) {
    companion object {
        fun <T> success(data: T, message: String? = null): ApiResource<T> =
            ApiResource(status = Status.SUCCESS, data = data, message = message)

        fun <T> error(message: String): ApiResource<T> =
            ApiResource(status = Status.ERROR, data = null, message = message)

        fun <T> loading(): ApiResource<T> =
            ApiResource(status = Status.LOADING, data = null, message = null)

        fun <T> idle(): ApiResource<T> =
            ApiResource(status = Status.IDLE, data = null, message = null)
    }


    enum class Status {
        SUCCESS, ERROR, LOADING, IDLE
    }
}