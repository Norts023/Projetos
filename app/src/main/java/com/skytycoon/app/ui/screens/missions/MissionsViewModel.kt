package com.skytycoon.app.ui.screens.missions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skytycoon.app.data.repository.MissionRepository
import com.skytycoon.app.domain.model.Mission
import com.skytycoon.app.domain.model.MissionType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

data class MissionsUiState(
    val primaryMissions: List<Mission> = emptyList(),
    val secondaryMissions: List<Mission> = emptyList(),
    val dailyMissions: List<Mission> = emptyList()
)

@HiltViewModel
class MissionsViewModel @Inject constructor(
    private val missionRepository: MissionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MissionsUiState())
    val uiState: StateFlow<MissionsUiState> = _uiState.asStateFlow()

    init {
        combine(
            missionRepository.getByType(MissionType.PRIMARY),
            missionRepository.getByType(MissionType.SECONDARY),
            missionRepository.getByType(MissionType.DAILY)
        ) { primary, secondary, daily ->
            MissionsUiState(
                primaryMissions = primary,
                secondaryMissions = secondary,
                dailyMissions = daily
            )
        }.onEach { state ->
            _uiState.value = state
        }.launchIn(viewModelScope)
    }
}
