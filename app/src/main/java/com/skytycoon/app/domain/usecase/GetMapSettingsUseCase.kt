package com.skytycoon.app.domain.usecase

import com.skytycoon.app.domain.model.MapSettings
import com.skytycoon.app.domain.repository.MapSettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetMapSettingsUseCase @Inject constructor(
    private val repository: MapSettingsRepository
) {
    operator fun invoke(): Flow<MapSettings> = repository.getSettings()
}
