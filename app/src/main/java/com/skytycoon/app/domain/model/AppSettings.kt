package com.skytycoon.app.domain.model

data class AppSettings(
    val languageCode: String = "auto", // "auto", "pt", "en"
    val defaultAutoTime: Boolean = false,
    val defaultSpeedMultiplier: Int = 1
)
