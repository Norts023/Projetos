package com.skytycoon.app.ui.screens.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skytycoon.app.data.repository.GameStateRepository
import com.skytycoon.app.ui.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val gameStateRepository: GameStateRepository
) : ViewModel() {

    private val _navDestination = MutableStateFlow<String?>(null)
    val navDestination: StateFlow<String?> = _navDestination.asStateFlow()

    init {
        viewModelScope.launch {
            delay(1500L)
            val gameState = gameStateRepository.get().first()
            _navDestination.value = if (gameState != null) {
                Screen.Dashboard.route
            } else {
                Screen.NewGame.route
            }
        }
    }

    fun clearNavDestination() {
        _navDestination.value = null
    }
}
