package com.skytycoon.app.domain.model

data class BillingProduct(
    val sku: String,
    val type: BillingProductType,
    val name: String,
    val description: String,
    val priceDisplay: String,
    val coinReward: Long = 0,
    val isLimitedTime: Boolean = false
)
