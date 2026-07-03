package com.skytycoon.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.skytycoon.app.data.local.dao.*
import com.skytycoon.app.data.local.entity.*

@Database(
    entities = [
        AirportEntity::class,
        AircraftModelEntity::class,
        OwnedAircraftEntity::class,
        FlightEntity::class,
        EmployeeEntity::class,
        MissionEntity::class,
        ContractEntity::class,
        GameStateEntity::class,
        BoosterInventoryEntity::class,
        ActiveBoosterEntity::class,
        AchievementEntity::class,
        UnlockEntity::class,
        PurchaseRecordEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun airportDao(): AirportDao
    abstract fun aircraftModelDao(): AircraftModelDao
    abstract fun ownedAircraftDao(): OwnedAircraftDao
    abstract fun flightDao(): FlightDao
    abstract fun employeeDao(): EmployeeDao
    abstract fun missionDao(): MissionDao
    abstract fun contractDao(): ContractDao
    abstract fun gameStateDao(): GameStateDao
    abstract fun boosterDao(): BoosterDao
    abstract fun achievementDao(): AchievementDao
    abstract fun unlockDao(): UnlockDao
    abstract fun purchaseRecordDao(): PurchaseRecordDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "skytycoon.db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback())
                    .build()
                    .also { INSTANCE = it }
            }
    }

    private class DatabaseCallback : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
        }
    }
}
