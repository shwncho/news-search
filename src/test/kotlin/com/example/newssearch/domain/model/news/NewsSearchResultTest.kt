package com.example.newssearch.domain.model.news

import com.example.newssearch.fixture.NewsItemFixture
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class NewsSearchResultTest {
    @Test
    fun `유효한 검색어와 결과 목록으로 NewsSearchResult를 생성할 수 있다`() {
        // given
        val query = "스프링"
        val items = listOf(NewsItemFixture.create())

        // when
        val result = NewsSearchResult.of(query = query, items = items)

        // then
        assertThat(result.query).isEqualTo(query)
        assertThat(result.items).hasSize(1)
    }

    @Test
    fun `빈 검색어로 NewsSearchResult 생성 시 IllegalArgumentException이 발생해야 한다`() {
        // when & then
        assertThatThrownBy {
            NewsSearchResult.of(query = "", items = emptyList())
        }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("검색어는 비어 있을 수 없습니다")
    }

    @Test
    fun `검색 결과가 없을 때 items는 빈 리스트여야 한다`() {
        // given
        val query = "결과없음"

        // when
        val result = NewsSearchResult.of(query = query, items = emptyList())

        // then
        assertThat(result.items).isEmpty()
    }
}
