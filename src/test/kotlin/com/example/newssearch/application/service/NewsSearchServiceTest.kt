package com.example.newssearch.application.service

import com.example.newssearch.domain.exception.NewsSearchException
import com.example.newssearch.domain.repository.NewsRepository
import com.example.newssearch.fixture.NewsSearchResultFixture
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class NewsSearchServiceTest {
    private val newsRepository: NewsRepository = mockk()
    private lateinit var newsSearchService: NewsSearchService

    @BeforeEach
    fun setUp() {
        newsSearchService = NewsSearchService(newsRepository)
    }

    @Test
    fun `검색어로 뉴스 검색 시 NewsSearchResponse를 반환해야 한다`() {
        // given
        val query = "테스트"
        val result = NewsSearchResultFixture.create(query = "테스트")
        every { newsRepository.search("테스트") } returns result

        // when
        val response = newsSearchService.search(query)

        // then
        verify(exactly = 1) { newsRepository.search("테스트") }
        assertThat(response.query).isEqualTo("테스트")
        assertThat(response.items).hasSize(1)
    }

    @Test
    fun `검색 결과가 없을 때 빈 items 목록을 반환해야 한다`() {
        // given
        val query = "결과없음"
        val emptyResult = NewsSearchResultFixture.create(query = "결과없음", items = emptyList())
        every { newsRepository.search("결과없음") } returns emptyResult

        // when
        val response = newsSearchService.search(query)

        // then
        assertThat(response.items).isEmpty()
    }

    @Test
    fun `외부 API 호출 실패 시 NewsSearchException이 전파되어야 한다`() {
        // given
        val query = "오류"
        every { newsRepository.search("오류") } throws NewsSearchException("API 호출 실패")

        // when & then
        assertThatThrownBy { newsSearchService.search(query) }
            .isInstanceOf(NewsSearchException::class.java)
            .hasMessageContaining("API 호출 실패")
    }
}
