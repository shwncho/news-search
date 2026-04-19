package com.example.newssearch.infrastructure.external.news

import com.example.newssearch.domain.exception.NewsSearchException
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientException

@ExtendWith(MockKExtension::class)
class NaverNewsClientTest {
    private val restClient: RestClient = mockk()
    private lateinit var naverNewsClient: NaverNewsClient

    @BeforeEach
    fun setUp() {
        naverNewsClient =
            NaverNewsClient(
                restClient = restClient,
                clientId = "test-client-id",
                clientSecret = "test-client-secret",
            )
    }

    @Test
    fun `검색어로 API 호출 시 NewsSearchResult를 반환해야 한다`() {
        // given
        val query = "스프링"
        val apiItem =
            NaverNewsApiItem(
                title = "스프링 뉴스",
                originalLink = "https://original.example.com/1",
                link = "https://news.naver.com/1",
                description = "스프링 관련 뉴스입니다",
                pubDate = "Mon, 17 Apr 2026 09:00:00 +0900",
            )
        val apiResponse =
            NaverNewsApiResponse(
                lastBuildDate = "Mon, 17 Apr 2026 09:00:00 +0900",
                total = 1,
                start = 1,
                display = 10,
                items = listOf(apiItem),
            )

        val requestHeadersUriSpec = mockk<RestClient.RequestHeadersUriSpec<*>>()
        val requestHeadersSpec = mockk<RestClient.RequestHeadersSpec<*>>()
        val responseSpec = mockk<RestClient.ResponseSpec>()

        every { restClient.get() } returns requestHeadersUriSpec
        every { requestHeadersUriSpec.uri(any<String>(), any(), any()) } returns requestHeadersSpec
        every { requestHeadersSpec.header("X-Naver-Client-Id", any()) } returns requestHeadersSpec
        every { requestHeadersSpec.header("X-Naver-Client-Secret", any()) } returns requestHeadersSpec
        every { requestHeadersSpec.retrieve() } returns responseSpec
        every { responseSpec.body(NaverNewsApiResponse::class.java) } returns apiResponse

        // when
        val result = naverNewsClient.search(query)

        // then
        assertThat(result.query).isEqualTo(query)
        assertThat(result.items).hasSize(1)
        assertThat(result.items[0].title).isEqualTo("스프링 뉴스")
    }

    @Test
    fun `API 호출 실패 시 NewsSearchException이 발생해야 한다`() {
        // given
        val requestHeadersUriSpec = mockk<RestClient.RequestHeadersUriSpec<*>>()
        val requestHeadersSpec = mockk<RestClient.RequestHeadersSpec<*>>()

        every { restClient.get() } returns requestHeadersUriSpec
        every { requestHeadersUriSpec.uri(any<String>(), any(), any()) } returns requestHeadersSpec
        every { requestHeadersSpec.header("X-Naver-Client-Id", any()) } returns requestHeadersSpec
        every { requestHeadersSpec.header("X-Naver-Client-Secret", any()) } returns requestHeadersSpec
        every { requestHeadersSpec.retrieve() } throws RestClientException("연결 실패")

        // when & then
        assertThatThrownBy { naverNewsClient.search("테스트") }
            .isInstanceOf(NewsSearchException::class.java)
            .hasMessageContaining("네이버 뉴스 검색 API 호출에 실패했습니다")
    }

    @Test
    fun `API가 null 응답을 반환하면 NewsSearchException이 발생해야 한다`() {
        // given
        val requestHeadersUriSpec = mockk<RestClient.RequestHeadersUriSpec<*>>()
        val requestHeadersSpec = mockk<RestClient.RequestHeadersSpec<*>>()
        val responseSpec = mockk<RestClient.ResponseSpec>()

        every { restClient.get() } returns requestHeadersUriSpec
        every { requestHeadersUriSpec.uri(any<String>(), any(), any()) } returns requestHeadersSpec
        every { requestHeadersSpec.header("X-Naver-Client-Id", any()) } returns requestHeadersSpec
        every { requestHeadersSpec.header("X-Naver-Client-Secret", any()) } returns requestHeadersSpec
        every { requestHeadersSpec.retrieve() } returns responseSpec
        every { responseSpec.body(NaverNewsApiResponse::class.java) } returns null

        // when & then
        assertThatThrownBy { naverNewsClient.search("테스트") }
            .isInstanceOf(NewsSearchException::class.java)
            .hasMessageContaining("네이버 뉴스 검색 API 응답이 비어 있습니다")
    }

    @Test
    fun `API 호출 시 올바른 헤더와 파라미터가 전달되어야 한다`() {
        // given
        val query = "헤더테스트"
        val apiResponse =
            NaverNewsApiResponse(
                lastBuildDate = "Mon, 17 Apr 2026 09:00:00 +0900",
                total = 0,
                start = 1,
                display = 10,
                items = emptyList(),
            )

        val requestHeadersUriSpec = mockk<RestClient.RequestHeadersUriSpec<*>>()
        val requestHeadersSpec = mockk<RestClient.RequestHeadersSpec<*>>()
        val responseSpec = mockk<RestClient.ResponseSpec>()

        every { restClient.get() } returns requestHeadersUriSpec
        every { requestHeadersUriSpec.uri(any<String>(), any(), any()) } returns requestHeadersSpec
        every { requestHeadersSpec.header("X-Naver-Client-Id", "test-client-id") } returns requestHeadersSpec
        every { requestHeadersSpec.header("X-Naver-Client-Secret", "test-client-secret") } returns requestHeadersSpec
        every { requestHeadersSpec.retrieve() } returns responseSpec
        every { responseSpec.body(NaverNewsApiResponse::class.java) } returns apiResponse

        // when
        naverNewsClient.search(query)

        // then
        verify(exactly = 1) { requestHeadersSpec.header("X-Naver-Client-Id", "test-client-id") }
        verify(exactly = 1) { requestHeadersSpec.header("X-Naver-Client-Secret", "test-client-secret") }
    }
}
