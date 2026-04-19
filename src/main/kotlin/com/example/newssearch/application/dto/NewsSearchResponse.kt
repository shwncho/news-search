package com.example.newssearch.application.dto

import com.example.newssearch.domain.model.news.NewsSearchResult

data class NewsSearchResponse(
    val query: String,
    val items: List<NewsItemResponse>,
) {
    companion object {
        fun from(result: NewsSearchResult): NewsSearchResponse =
            NewsSearchResponse(
                query = result.query,
                items = result.items.map { NewsItemResponse.from(it) },
            )
    }
}
