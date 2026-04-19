package com.example.newssearch.fixture

import com.example.newssearch.application.dto.NewsItemResponse
import com.example.newssearch.application.dto.NewsSearchResponse
import com.example.newssearch.domain.model.news.NewsItem
import com.example.newssearch.domain.model.news.NewsSearchResult

object NewsItemFixture {
    fun create(
        title: String = "테스트 뉴스 제목",
        originalLink: String = "https://original.example.com/news/1",
        link: String = "https://news.naver.com/1",
        description: String = "테스트 뉴스 요약입니다",
        pubDate: String = "Mon, 17 Apr 2026 09:00:00 +0900",
    ): NewsItem =
        NewsItem.create(
            title = title,
            originalLink = originalLink,
            link = link,
            description = description,
            pubDate = pubDate,
        )
}

object NewsSearchResultFixture {
    fun create(
        query: String = "테스트",
        items: List<NewsItem> = listOf(NewsItemFixture.create()),
    ): NewsSearchResult = NewsSearchResult.of(query = query, items = items)
}

object NewsSearchResponseFixture {
    fun create(
        query: String = "테스트",
        items: List<NewsItemResponse> =
            listOf(
                NewsItemResponse(
                    title = "테스트 뉴스 제목",
                    originalLink = "https://original.example.com/news/1",
                    link = "https://news.naver.com/1",
                    description = "테스트 뉴스 요약입니다",
                    pubDate = "Mon, 17 Apr 2026 09:00:00 +0900",
                ),
            ),
    ): NewsSearchResponse = NewsSearchResponse(query = query, items = items)
}
