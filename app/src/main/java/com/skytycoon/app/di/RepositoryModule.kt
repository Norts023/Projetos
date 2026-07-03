package com.skytycoon.app.di

import com.skytycoon.app.data.repository.AircraftModelRepository
import com.skytycoon.app.data.repository.AircraftModelRepositoryImpl
import com.skytycoon.app.data.repository.AchievementRepositoryImpl
import com.skytycoon.app.data.repository.AirportRepository
import com.skytycoon.app.data.repository.AirportRepositoryImpl
import com.skytycoon.app.data.repository.AppSettingsRepositoryImpl
import com.skytycoon.app.data.repository.BoosterRepositoryImpl
import com.skytycoon.app.data.repository.ContractRepository
import com.skytycoon.app.data.repository.ContractRepositoryImpl
import com.skytycoon.app.data.repository.EmployeeRepository
import com.skytycoon.app.data.repository.EmployeeRepositoryImpl
import com.skytycoon.app.data.repository.FlightRepository
import com.skytycoon.app.data.repository.FlightRepositoryImpl
import com.skytycoon.app.data.repository.GameStateRepository
import com.skytycoon.app.data.repository.GameStateRepositoryImpl
import com.skytycoon.app.data.repository.MapSettingsRepositoryImpl
import com.skytycoon.app.data.repository.MissionRepository
import com.skytycoon.app.data.repository.MissionRepositoryImpl
import com.skytycoon.app.data.repository.OwnedAircraftRepository
import com.skytycoon.app.data.repository.OwnedAircraftRepositoryImpl
import com.skytycoon.app.data.repository.UnlockRepositoryImpl
import com.skytycoon.app.domain.repository.AchievementRepository
import com.skytycoon.app.domain.repository.AppSettingsRepository
import com.skytycoon.app.domain.repository.BoosterRepository
import com.skytycoon.app.domain.repository.MapSettingsRepository
import com.skytycoon.app.domain.repository.UnlockRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds abstract fun bindAirportRepository(impl: AirportRepositoryImpl): AirportRepository
    @Binds abstract fun bindAircraftModelRepository(impl: AircraftModelRepositoryImpl): AircraftModelRepository
    @Binds abstract fun bindOwnedAircraftRepository(impl: OwnedAircraftRepositoryImpl): OwnedAircraftRepository
    @Binds abstract fun bindFlightRepository(impl: FlightRepositoryImpl): FlightRepository
    @Binds abstract fun bindEmployeeRepository(impl: EmployeeRepositoryImpl): EmployeeRepository
    @Binds abstract fun bindMissionRepository(impl: MissionRepositoryImpl): MissionRepository
    @Binds abstract fun bindContractRepository(impl: ContractRepositoryImpl): ContractRepository
    @Binds abstract fun bindGameStateRepository(impl: GameStateRepositoryImpl): GameStateRepository
    @Binds abstract fun bindBoosterRepository(impl: BoosterRepositoryImpl): BoosterRepository
    @Binds abstract fun bindAchievementRepository(impl: AchievementRepositoryImpl): AchievementRepository
    @Binds abstract fun bindUnlockRepository(impl: UnlockRepositoryImpl): UnlockRepository
    @Binds abstract fun bindMapSettingsRepository(impl: MapSettingsRepositoryImpl): MapSettingsRepository
    @Binds abstract fun bindAppSettingsRepository(impl: AppSettingsRepositoryImpl): AppSettingsRepository
}
