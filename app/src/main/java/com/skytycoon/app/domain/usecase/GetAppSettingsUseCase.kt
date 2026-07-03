package com.skytycoon.app.domain.usecase

import com.skytycoon.app.domain.model.AppSettings
import com.skytycoon.app.domain.repository.AppSettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAppSettingsUseCase @Inject constructor(
    private val repository: AppSettingsRepository
) {
    operator fun invoke(): Flow<AppSettings> = repository.getSettings()
}
