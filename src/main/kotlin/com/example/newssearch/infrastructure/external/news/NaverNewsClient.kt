package com.example.newssearch.infrastructure.external.news

import com.example.newssearch.domain.exception.NewsSearchException
import com.example.newssearch.domain.model.news.NewsItem
import com.example.newssearch.domain.model.news.NewsSearchResult
import com.example.newssearch.domain.repository.NewsRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Repository
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientException

@Repository
class NaverNewsClient(
    private val restClient: RestClient,
    @Value("\${news.client-id}") private val clientId: String,
    @Value("\${news.client-secret}") private val clientSecret: String,
) : NewsRepository {
    companion object {
        private const val NAVER_NEWS_API_URL = "https://openapi.naver.com/v1/search/news.json"
        private const val DISPLAY_COUNT = 10
    }

    override fun search(query: String): NewsSearchResult {
        val response =
            try {
                restClient.get()
                    .uri("$NAVER_NEWS_API_URL?query={query}&display={display}", query, DISPLAY_COUNT)
                    .header("X-Naver-Client-Id", clientId)
                    .header("X-Naver-Client-Secret", clientSecret)
                    .retrieve()
                    .body(NaverNewsApiResponse::class.java)
            } catch (e: RestClientException) {
                throw NewsSearchException("네이버 뉴스 검색 API 호출에 실패했습니다: ${e.message}", e)
            }

        val apiResponse = response ?: throw NewsSearchException("네이버 뉴스 검색 API 응답이 비어 있습니다")

        val newsItems =
            apiResponse.items.map { item ->
                NewsItem.create(
                    title = item.title,
                    originalLink = item.originalLink,
                    link = item.link,
                    description = item.description,
                    pubDate = item.pubDate,
                )
            }

        return NewsSearchResult.of(query = query, items = newsItems)
    }
}
