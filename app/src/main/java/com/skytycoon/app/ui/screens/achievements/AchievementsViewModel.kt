package com.skytycoon.app.ui.screens.achievements

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skytycoon.app.domain.model.Achievement
import com.skytycoon.app.domain.model.UseCaseResult
import com.skytycoon.app.domain.repository.AchievementRepository
import com.skytycoon.app.domain.usecase.ClaimMissionRewardUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AchievementsUiState(
    val achievements: List<Achievement> = emptyList(),
    val isLoading: Boolean = false,
    val claimMessage: String? = null
)

@HiltViewModel
class AchievementsViewModel @Inject constructor(
    private val achievementRepository: AchievementRepository,
    private val claimMissionRewardUseCase: ClaimMissionRewardUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AchievementsUiState())
    val uiState: StateFlow<AchievementsUiState> = _uiState.asStateFlow()

    init {
        achievementRepository.getAll()
            .onEach { achievements ->
                _uiState.update { it.copy(achievements = achievements) }
            }
            .launchIn(viewModelScope)
    }

    fun onClaim(achievementId: Long) {
        viewModelScope.launch {
            val result = claimMissionRewardUseCase(achievementId)
            val message = when (result) {
                is UseCaseResult.Success -> "Reward claimed! +${result.data} coins"
                is UseCaseResult.Failure -> result.message
            }
            _uiState.update { it.copy(claimMessage = message) }
        }
    }

    fun onClaimMessageShown() {
        _uiState.update { it.copy(claimMessage = null) }
    }
}
