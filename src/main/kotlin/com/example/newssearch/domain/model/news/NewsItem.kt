package com.example.newssearch.domain.model.news

data class NewsItem(
    val title: String,
    val originalLink: String,
    val link: String,
    val description: String,
    val pubDate: String,
) {
    companion object {
        fun create(
            title: String,
            originalLink: String,
            link: String,
            description: String,
            pubDate: String,
        ): NewsItem {
            require(title.isNotBlank()) { "뉴스 제목은 비어 있을 수 없습니다" }
            require(link.isNotBlank()) { "뉴스 링크는 비어 있을 수 없습니다" }
            return NewsItem(
                title = title,
                originalLink = originalLink,
                link = link,
                description = description,
                pubDate = pubDate,
            )
        }
    }
}
