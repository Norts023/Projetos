package com.skytycoon.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.skytycoon.app.data.local.entity.AirportEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AirportDao {

    @Query("SELECT * FROM airports")
    fun getAll(): Flow<List<AirportEntity>>

    @Query("SELECT * FROM airports WHERE iata = :iata LIMIT 1")
    suspend fun getByIata(iata: String): AirportEntity?

    @Query(
        "SELECT * FROM airports WHERE " +
        "name LIKE '%' || :q || '%' OR " +
        "city LIKE '%' || :q || '%' OR " +
        "iata LIKE '%' || :q || '%'"
    )
    fun search(q: String): Flow<List<AirportEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(airports: List<AirportEntity>)

    @Query("SELECT COUNT(*) FROM airports")
    suspend fun count(): Int
}
