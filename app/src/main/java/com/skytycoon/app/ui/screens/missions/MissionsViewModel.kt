package com.skytycoon.app.ui.screens.missions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skytycoon.app.data.repository.GameStateRepository
import com.skytycoon.app.data.repository.MissionRepository
import com.skytycoon.app.domain.model.Mission
import com.skytycoon.app.domain.model.MissionType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class MissionsUiState(
    val primaryMissions: List<Mission> = emptyList(),
    val secondaryMissions: List<Mission> = emptyList(),
    val dailyMissions: List<Mission> = emptyList(),
    val dayNumber: Int = 1
)

@HiltViewModel
class MissionsViewModel @Inject constructor(
    private val missionRepository: MissionRepository,
    private val gameStateRepository: GameStateRepository
) : ViewModel() {

    val uiState: StateFlow<MissionsUiState> = combine(
        missionRepository.getAll(),
        gameStateRepository.get()
    ) { missions, gameState ->
        MissionsUiState(
            primaryMissions = missions.filter { it.type == MissionType.PRIMARY },
            secondaryMissions = missions.filter { it.type == MissionType.SECONDARY },
            dailyMissions = missions.filter { it.type == MissionType.DAILY },
            dayNumber = gameState?.dayNumber ?: 1
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MissionsUiState()
    )
}
