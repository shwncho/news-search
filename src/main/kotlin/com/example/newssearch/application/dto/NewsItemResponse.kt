package com.example.newssearch.application.dto

import com.example.newssearch.domain.model.news.NewsItem

data class NewsItemResponse(
    val title: String,
    val originalLink: String,
    val link: String,
    val description: String,
    val pubDate: String,
) {
    companion object {
        fun from(newsItem: NewsItem): NewsItemResponse =
            NewsItemResponse(
                title = newsItem.title,
                originalLink = newsItem.originalLink,
                link = newsItem.link,
                description = newsItem.description,
                pubDate = newsItem.pubDate,
            )
    }
}
