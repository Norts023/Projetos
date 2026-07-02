package com.skytycoon.app.di

import android.content.Context
import androidx.room.Room
import com.skytycoon.app.data.local.AppDatabase
import com.skytycoon.app.data.local.dao.AircraftModelDao
import com.skytycoon.app.data.local.dao.AirportDao
import com.skytycoon.app.data.local.dao.ContractDao
import com.skytycoon.app.data.local.dao.EmployeeDao
import com.skytycoon.app.data.local.dao.FlightDao
import com.skytycoon.app.data.local.dao.GameStateDao
import com.skytycoon.app.data.local.dao.MissionDao
import com.skytycoon.app.data.local.dao.OwnedAircraftDao
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
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "skytycoon.db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideAirportDao(db: AppDatabase): AirportDao = db.airportDao()

    @Provides
    fun provideAircraftModelDao(db: AppDatabase): AircraftModelDao = db.aircraftModelDao()

    @Provides
    fun provideOwnedAircraftDao(db: AppDatabase): OwnedAircraftDao = db.ownedAircraftDao()

    @Provides
    fun provideFlightDao(db: AppDatabase): FlightDao = db.flightDao()

    @Provides
    fun provideEmployeeDao(db: AppDatabase): EmployeeDao = db.employeeDao()

    @Provides
    fun provideMissionDao(db: AppDatabase): MissionDao = db.missionDao()

    @Provides
    fun provideContractDao(db: AppDatabase): ContractDao = db.contractDao()

    @Provides
    fun provideGameStateDao(db: AppDatabase): GameStateDao = db.gameStateDao()
}
