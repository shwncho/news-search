package com.example.newssearch.infrastructure.external.news

import com.fasterxml.jackson.annotation.JsonProperty

data class NaverNewsApiResponse(
    val lastBuildDate: String,
    val total: Int,
    val start: Int,
    val display: Int,
    val items: List<NaverNewsApiItem>,
)

data class NaverNewsApiItem(
    val title: String,
    @JsonProperty("originallink") val originalLink: String,
    val link: String,
    val description: String,
    val pubDate: String,
)
