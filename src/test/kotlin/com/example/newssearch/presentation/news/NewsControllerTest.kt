package com.example.newssearch.presentation.news

import com.example.newssearch.application.service.NewsSearchService
import com.example.newssearch.config.GlobalExceptionHandler
import com.example.newssearch.domain.exception.NewsSearchException
import com.example.newssearch.fixture.NewsSearchResponseFixture
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@WebMvcTest(NewsController::class)
@Import(GlobalExceptionHandler::class)
class NewsControllerTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @MockkBean
    lateinit var newsSearchService: NewsSearchService

    @Test
    fun `GET api_news - 검색어로 뉴스 검색 시 200과 결과를 반환해야 한다`() {
        // given
        val response = NewsSearchResponseFixture.create(query = "테스트")
        every { newsSearchService.search(any()) } returns response

        // when & then
        mockMvc.get("/api/news") {
            param("query", "테스트")
        }.andExpect {
            status { isOk() }
            jsonPath("$.query") { value("테스트") }
            jsonPath("$.items") { isArray() }
            jsonPath("$.items[0].title") { value("테스트 뉴스 제목") }
        }
    }

    @Test
    fun `GET api_news - 검색어 없이 요청 시 400을 반환해야 한다`() {
        // when & then
        mockMvc.get("/api/news")
            .andExpect {
                status { isBadRequest() }
            }
    }

    @Test
    fun `GET api_news - 외부 API 오류 시 502를 반환해야 한다`() {
        // given
        every { newsSearchService.search(any()) } throws NewsSearchException("네이버 API 오류")

        // when & then
        mockMvc.get("/api/news") {
            param("query", "테스트")
        }.andExpect {
            status { isBadGateway() }
            jsonPath("$.code") { value("NEWS_SEARCH_ERROR") }
        }
    }
}
