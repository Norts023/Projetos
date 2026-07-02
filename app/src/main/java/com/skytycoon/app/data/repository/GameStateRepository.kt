package com.skytycoon.app.data.repository

import com.skytycoon.app.data.local.dao.GameStateDao
import com.skytycoon.app.data.local.entity.GameStateEntity
import com.skytycoon.app.domain.model.GameState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

interface GameStateRepository {
    fun get(): Flow<GameState?>
    suspend fun insert(gameState: GameState)
    suspend fun update(gameState: GameState)
}

@Singleton
class GameStateRepositoryImpl @Inject constructor(
    private val gameStateDao: GameStateDao
) : GameStateRepository {

    override fun get(): Flow<GameState?> =
        gameStateDao.get().map { entity -> entity?.toDomain() }

    override suspend fun insert(gameState: GameState) = withContext(Dispatchers.IO) {
        gameStateDao.insert(GameStateEntity.fromDomain(gameState))
    }

    override suspend fun update(gameState: GameState) = withContext(Dispatchers.IO) {
        gameStateDao.update(GameStateEntity.fromDomain(gameState))
    }
}
