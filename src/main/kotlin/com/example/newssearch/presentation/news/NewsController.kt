package com.example.newssearch.presentation.news

import com.example.newssearch.application.dto.NewsSearchResponse
import com.example.newssearch.application.service.NewsSearchService
import jakarta.validation.constraints.NotBlank
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("/api/news")
class NewsController(
    private val newsSearchService: NewsSearchService,
) {
    @GetMapping
    fun search(
        @RequestParam @NotBlank(message = "검색어는 비어 있을 수 없습니다") query: String,
    ): ResponseEntity<NewsSearchResponse> = ResponseEntity.ok(newsSearchService.search(query))
}
