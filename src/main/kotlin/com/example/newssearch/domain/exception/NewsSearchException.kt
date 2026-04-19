package com.example.newssearch.domain.exception

class NewsSearchException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)
