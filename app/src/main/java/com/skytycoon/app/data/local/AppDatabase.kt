package com.skytycoon.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.skytycoon.app.data.local.dao.AircraftModelDao
import com.skytycoon.app.data.local.dao.AirportDao
import com.skytycoon.app.data.local.dao.ContractDao
import com.skytycoon.app.data.local.dao.EmployeeDao
import com.skytycoon.app.data.local.dao.FlightDao
import com.skytycoon.app.data.local.dao.GameStateDao
import com.skytycoon.app.data.local.dao.MissionDao
import com.skytycoon.app.data.local.dao.OwnedAircraftDao
import com.skytycoon.app.data.local.entity.AircraftModelEntity
import com.skytycoon.app.data.local.entity.AirportEntity
import com.skytycoon.app.data.local.entity.ContractEntity
import com.skytycoon.app.data.local.entity.EmployeeEntity
import com.skytycoon.app.data.local.entity.FlightEntity
import com.skytycoon.app.data.local.entity.GameStateEntity
import com.skytycoon.app.data.local.entity.MissionEntity
import com.skytycoon.app.data.local.entity.OwnedAircraftEntity

@Database(
    entities = [
        AirportEntity::class,
        AircraftModelEntity::class,
        OwnedAircraftEntity::class,
        FlightEntity::class,
        EmployeeEntity::class,
        MissionEntity::class,
        ContractEntity::class,
        GameStateEntity::class
    ],
    version = 1,
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
                    .addCallback(DatabaseCallback())
                    .build()
                    .also { INSTANCE = it }
            }
    }

    private class DatabaseCallback : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            // Seeding is handled by repositories via seedIfEmpty()
        }
    }
}
