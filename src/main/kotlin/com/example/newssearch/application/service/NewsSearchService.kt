package com.example.newssearch.application.service

import com.example.newssearch.application.dto.NewsSearchResponse
import com.example.newssearch.domain.repository.NewsRepository
import org.apache.coyote.BadRequestException
import org.springframework.stereotype.Service

@Service
class NewsSearchService(
    private val newsRepository: NewsRepository,
) {
    fun search(query: String): NewsSearchResponse {
        val result = newsRepository.search(query)
        return NewsSearchResponse.from(result)
    }
}
