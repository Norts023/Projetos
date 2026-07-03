package com.skytycoon.app.domain.usecase

import com.skytycoon.app.domain.model.AppSettings
import com.skytycoon.app.domain.repository.AppSettingsRepository
import javax.inject.Inject

class UpdateAppSettingsUseCase @Inject constructor(
    private val repository: AppSettingsRepository
) {
    suspend operator fun invoke(settings: AppSettings) = repository.update(settings)
}
