package com.skytycoon.app.di

import android.content.Context
import androidx.room.Room
import com.skytycoon.app.data.local.*
import com.skytycoon.app.data.local.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "skytycoon.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun provideAirportDao(db: AppDatabase): AirportDao = db.airportDao()
    @Provides fun provideAircraftModelDao(db: AppDatabase): AircraftModelDao = db.aircraftModelDao()
    @Provides fun provideOwnedAircraftDao(db: AppDatabase): OwnedAircraftDao = db.ownedAircraftDao()
    @Provides fun provideFlightDao(db: AppDatabase): FlightDao = db.flightDao()
    @Provides fun provideEmployeeDao(db: AppDatabase): EmployeeDao = db.employeeDao()
    @Provides fun provideMissionDao(db: AppDatabase): MissionDao = db.missionDao()
    @Provides fun provideContractDao(db: AppDatabase): ContractDao = db.contractDao()
    @Provides fun provideGameStateDao(db: AppDatabase): GameStateDao = db.gameStateDao()
    @Provides fun provideBoosterDao(db: AppDatabase): BoosterDao = db.boosterDao()
    @Provides fun provideAchievementDao(db: AppDatabase): AchievementDao = db.achievementDao()
    @Provides fun provideUnlockDao(db: AppDatabase): UnlockDao = db.unlockDao()
    @Provides fun providePurchaseRecordDao(db: AppDatabase): PurchaseRecordDao = db.purchaseRecordDao()

    @Provides
    @Singleton
    fun provideMapSettingsDataStore(@ApplicationContext context: Context): MapSettingsDataStore =
        MapSettingsDataStore(context)

    @Provides
    @Singleton
    fun provideAppSettingsDataStore(@ApplicationContext context: Context): AppSettingsDataStore =
        AppSettingsDataStore(context)
}
