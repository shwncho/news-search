package com.example.newssearch

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class NewsSearchApplication

fun main(args: Array<String>) {
    runApplication<NewsSearchApplication>(*args)
}
