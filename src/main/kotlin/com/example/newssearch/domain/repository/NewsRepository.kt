package com.example.newssearch.domain.repository

import com.example.newssearch.domain.model.news.NewsSearchResult

interface NewsRepository {
    fun search(query: String): NewsSearchResult
}
