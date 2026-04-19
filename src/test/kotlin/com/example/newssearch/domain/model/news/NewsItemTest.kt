package com.example.newssearch.domain.model.news

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class NewsItemTest {
    @Test
    fun `유효한 값으로 NewsItem을 생성하면 모든 필드가 올바르게 설정된다`() {
        // given
        val title = "뉴스 제목"
        val originalLink = "https://original.example.com/1"
        val link = "https://news.naver.com/1"
        val description = "뉴스 요약"
        val pubDate = "Mon, 17 Apr 2026 09:00:00 +0900"

        // when
        val newsItem =
            NewsItem.create(
                title = title,
                originalLink = originalLink,
                link = link,
                description = description,
                pubDate = pubDate,
            )

        // then
        assertThat(newsItem.title).isEqualTo(title)
        assertThat(newsItem.originalLink).isEqualTo(originalLink)
        assertThat(newsItem.link).isEqualTo(link)
        assertThat(newsItem.description).isEqualTo(description)
        assertThat(newsItem.pubDate).isEqualTo(pubDate)
    }

    @Test
    fun `빈 제목으로 NewsItem 생성 시 IllegalArgumentException이 발생해야 한다`() {
        // when & then
        assertThatThrownBy {
            NewsItem.create(
                title = "",
                originalLink = "https://original.example.com/1",
                link = "https://news.naver.com/1",
                description = "설명",
                pubDate = "Mon, 17 Apr 2026 09:00:00 +0900",
            )
        }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("뉴스 제목은 비어 있을 수 없습니다")
    }

    @Test
    fun `빈 링크로 NewsItem 생성 시 IllegalArgumentException이 발생해야 한다`() {
        // when & then
        assertThatThrownBy {
            NewsItem.create(
                title = "제목",
                originalLink = "https://original.example.com/1",
                link = "",
                description = "설명",
                pubDate = "Mon, 17 Apr 2026 09:00:00 +0900",
            )
        }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("뉴스 링크는 비어 있을 수 없습니다")
    }
}
