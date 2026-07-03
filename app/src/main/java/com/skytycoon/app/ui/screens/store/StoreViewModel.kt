package com.skytycoon.app.ui.screens.store

import androidx.lifecycle.ViewModel
import com.skytycoon.app.domain.model.BillingProduct
import com.skytycoon.app.domain.model.BillingProductType
import com.skytycoon.app.domain.repository.AchievementRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class StoreUiState(
    val products: List<BillingProduct> = StoreViewModel.mockProducts
)

@HiltViewModel
class StoreViewModel @Inject constructor(
    @Suppress("UnusedPrivateProperty")
    private val achievementRepository: AchievementRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StoreUiState())
    val uiState: StateFlow<StoreUiState> = _uiState.asStateFlow()

    companion object {
        val mockProducts: List<BillingProduct> = listOf(
            BillingProduct(
                sku = "coins_1000",
                type = BillingProductType.COIN_PACK,
                name = "1.000 Moedas",
                description = "Pacote inicial",
                priceDisplay = "R$ 4,99",
                coinReward = 1_000L,
                isLimitedTime = false
            ),
            BillingProduct(
                sku = "coins_5000",
                type = BillingProductType.COIN_PACK,
                name = "5.000 Moedas",
                description = "Melhor custo-benefício",
                priceDisplay = "R$ 19,99",
                coinReward = 5_000L,
                isLimitedTime = false
            ),
            BillingProduct(
                sku = "welcome_pack",
                type = BillingProductType.WELCOME_PACK,
                name = "Pacote Boas-Vindas",
                description = "Exclusivo para novos jogadores",
                priceDisplay = "R$ 9,99",
                coinReward = 2_000L,
                isLimitedTime = true
            )
        )
    }
}
