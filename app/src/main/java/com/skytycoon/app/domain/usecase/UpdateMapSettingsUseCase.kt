package com.skytycoon.app.domain.usecase

import com.skytycoon.app.domain.model.MapSettings
import com.skytycoon.app.domain.repository.MapSettingsRepository
import javax.inject.Inject

class UpdateMapSettingsUseCase @Inject constructor(
    private val repository: MapSettingsRepository
) {
    suspend operator fun invoke(settings: MapSettings) = repository.update(settings)
}
