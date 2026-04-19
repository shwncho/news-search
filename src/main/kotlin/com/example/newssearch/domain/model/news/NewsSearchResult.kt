package com.example.newssearch.domain.model.news

data class NewsSearchResult(
    val query: String,
    val items: List<NewsItem>,
) {
    companion object {
        fun of(
            query: String,
            items: List<NewsItem>,
        ): NewsSearchResult {
            require(query.isNotBlank()) { "검색어는 비어 있을 수 없습니다" }
            return NewsSearchResult(
                query = query,
                items = items,
            )
        }
    }
}
